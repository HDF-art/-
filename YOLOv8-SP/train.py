"""
YOLOv8-SP 纯检测训练脚本
草莓成熟度分类模型训练（目标检测）

使用方法:
    python train.py --epochs 100 --batch 16
"""

import sys
import os
import argparse

# 禁用wandb
os.environ['WANDB_DISABLED'] = 'true'
os.environ['WANDB_MODE'] = 'disabled'
os.environ['ULTRALYTICS_HUB'] = 'false'

ROOT = os.path.dirname(os.path.abspath(__file__))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

import torch
import torch.nn as nn
from ultralytics import YOLO
from ultralytics.nn.modules import C2f, Concat, Conv
from ultralytics.models.yolo.detect import DetectionTrainer

# 移除wandb回调
from ultralytics.utils.callbacks import default_callbacks
for cb_name in default_callbacks:
    cb_list = default_callbacks[cb_name]
    for i in range(len(cb_list) - 1, -1, -1):
        cb = cb_list[i]
        if 'wb' in getattr(cb, '__module__', '') or 'wandb' in getattr(cb, '__module__', ''):
            cb_list.pop(i)

from yolov8sp.modules import StarELA, BottleneckELA, C2fStarELA, CCFM, CCFMBlock
from ultralytics.nn.tasks import DetectionModel


class YOLOv8SPModel(DetectionModel):
    """YOLOv8-SP检测模型: 在DetectionModel基础上替换backbone的C2f为C2fStarELA"""

    def __init__(self, cfg='yolov8n.yaml', ch=3, nc=None, verbose=True):
        super().__init__(cfg, ch, nc, verbose)
        self._replace_c2f_with_star_ela()

    def fuse(self, verbose=True):
        """禁用fuse，ELA模块中的BatchNorm1d不支持fuse"""
        return self

    def _replace_c2f_with_star_ela(self):
        """替换backbone中的C2f为C2fStarELA，迁移预训练权重"""
        backbone_end = 9  # backbone层索引0-9
        for i, layer in enumerate(self.model):
            if isinstance(layer, C2f) and i <= backbone_end:
                old_layer = layer
                c1 = _get_c1_from_c2f(old_layer)
                c2 = old_layer.cv2.conv.out_channels
                n = len(old_layer.m)
                shortcut = any(
                    hasattr(m, 'shortcut') and m.shortcut
                    for m in old_layer.m
                ) if hasattr(old_layer.m, '__iter__') else False

                new_layer = C2fStarELA(c1, c2, n=n, shortcut=shortcut,
                                       e=old_layer.e if hasattr(old_layer, 'e') else 0.5)
                _transfer_c2f_weights(old_layer, new_layer)
                _copy_layer_attrs(old_layer, new_layer)
                self.model[i] = new_layer
                print(f"  Layer {i}: C2f -> C2fStarELA (c1={c1}, c2={c2}, n={n})")


def register_custom_modules():
    """注册自定义模块到ultralytics命名空间"""
    import ultralytics.nn.tasks as tasks
    import ultralytics.nn.modules as nn_modules

    for module_cls in [StarELA, C2fStarELA, CCFM, CCFMBlock]:
        setattr(tasks, module_cls.__name__, module_cls)
        setattr(nn_modules, module_cls.__name__, module_cls)


def build_yolov8sp_inner_model(nc=3, pretrained=True):
    """
    构建YOLOv8-SP检测模型

    策略: 加载预训练YOLOv8n模型，保留backbone权重，
    然后将Backbone中的C2f替换为C2fStarELA，Head中的C2f替换为CCFM，
    最后将匹配的预训练权重迁移到新模型中

    关键改进: C2fStarELA使用BottleneckELA（Bottleneck+ELA），
    其cv1/cv2结构与原始Bottleneck完全相同，可以完整迁移预训练权重
    """
    register_custom_modules()

    # 加载预训练YOLOv8n模型
    if pretrained:
        print("  加载预训练YOLOv8n模型...")
        pretrained_model = YOLO('yolov8n.pt')
        pretrained_state = pretrained_model.model.float().state_dict()
    else:
        pretrained_state = None

    # 用YAML构建模型骨架
    model = YOLO('yolov8n.yaml')
    inner_model = model.model

    # 先将预训练权重完整加载到原始C2f模型中
    if pretrained_state is not None:
        model_dict = inner_model.state_dict()
        transferred = 0
        for key in model_dict:
            if key in pretrained_state and model_dict[key].shape == pretrained_state[key].shape:
                model_dict[key] = pretrained_state[key]
                transferred += 1
        inner_model.load_state_dict(model_dict)
        print(f"  预训练权重加载到原始模型: {transferred}/{len(model_dict)} 个参数")

    backbone_end = 9  # backbone层索引0-9

    for i, layer in enumerate(inner_model.model):
        if isinstance(layer, C2f) and i <= backbone_end:
            # 只替换backbone中的C2f为C2fStarELA
            # Head中的C2f保持不变，完整保留预训练权重
            old_layer = layer
            c1 = _get_c1_from_c2f(old_layer)
            c2 = old_layer.cv2.conv.out_channels
            n = len(old_layer.m)
            shortcut = any(
                hasattr(m, 'shortcut') and m.shortcut
                for m in old_layer.m
            ) if hasattr(old_layer.m, '__iter__') else False

            new_layer = C2fStarELA(c1, c2, n=n, shortcut=shortcut,
                                   e=old_layer.e if hasattr(old_layer, 'e') else 0.5)
            # 迁移C2f的cv1/cv2和Bottleneck的cv1/cv2权重
            _transfer_c2f_weights(old_layer, new_layer)
            _copy_layer_attrs(old_layer, new_layer)
            inner_model.model[i] = new_layer
            print(f"  Layer {i}: C2f -> C2fStarELA (c1={c1}, c2={c2}, n={n})")

    # 更新类别数
    detect_head = inner_model.model[-1]
    if hasattr(detect_head, 'nc'):
        detect_head.nc = nc
        _reinit_detect_head(detect_head, nc=nc)

    if hasattr(inner_model, 'yaml'):
        inner_model.yaml['nc'] = nc

    return inner_model


def _transfer_conv(conv_src, conv_dst):
    """安全迁移Conv模块的权重（处理bias=None的情况）"""
    conv_dst.weight.data = conv_src.weight.data.clone()
    if conv_dst.bias is not None and conv_src.bias is not None:
        conv_dst.bias.data = conv_src.bias.data.clone()


def _transfer_bn(bn_src, bn_dst):
    """迁移BatchNorm参数"""
    bn_dst.weight.data = bn_src.weight.data.clone()
    bn_dst.bias.data = bn_src.bias.data.clone()
    bn_dst.running_mean.data = bn_src.running_mean.data.clone()
    bn_dst.running_var.data = bn_src.running_var.data.clone()


def _transfer_c2f_weights(old_c2f, new_c2f_star_ela):
    """将C2f的cv1/cv2和Bottleneck的cv1/cv2权重迁移到C2fStarELA

    C2f结构: cv1(1x1, c1->2c), cv2(1x1, (2+n)*c->c2), m=[Bottleneck(cv1+cv2)]
    C2fStarELA结构: cv1(1x1, c1->2c), cv2(1x1, (2+n)*c->c2), m=[BottleneckELA(cv1+cv2+ela)]
    两者的cv1/cv2和Bottleneck的cv1/cv2结构完全相同，可以完整迁移
    """
    # 迁移C2f的cv1
    _transfer_conv(old_c2f.cv1.conv, new_c2f_star_ela.cv1.conv)
    _transfer_bn(old_c2f.cv1.bn, new_c2f_star_ela.cv1.bn)

    # 迁移C2f的cv2
    if old_c2f.cv2.conv.out_channels == new_c2f_star_ela.cv2.conv.out_channels:
        _transfer_conv(old_c2f.cv2.conv, new_c2f_star_ela.cv2.conv)
        _transfer_bn(old_c2f.cv2.bn, new_c2f_star_ela.cv2.bn)

    # 迁移Bottleneck内部的cv1/cv2到BottleneckELA
    for old_bottleneck, new_bottleneck_ela in zip(old_c2f.m, new_c2f_star_ela.m):
        _transfer_conv(old_bottleneck.cv1.conv, new_bottleneck_ela.cv1.conv)
        _transfer_bn(old_bottleneck.cv1.bn, new_bottleneck_ela.cv1.bn)
        _transfer_conv(old_bottleneck.cv2.conv, new_bottleneck_ela.cv2.conv)
        _transfer_bn(old_bottleneck.cv2.bn, new_bottleneck_ela.cv2.bn)
        # ELA模块的权重保持随机初始化（轻量模块，影响小）


def _get_c1_from_c2f(c2f_layer):
    """从C2f层推断输入通道数c1"""
    return c2f_layer.cv1.conv.in_channels


def _copy_layer_attrs(old_layer, new_layer):
    """复制ultralytics层的元数据属性"""
    for attr in ['i', 'f', 'type', 'np']:
        if hasattr(old_layer, attr):
            setattr(new_layer, attr, getattr(old_layer, attr))


def _reinit_detect_head(detect_head, nc):
    """重新初始化检测头以匹配新的类别数

    Detect头结构:
      cv2: box回归 (reg_max * 4 通道)
      cv3: 类别预测 (nc 通道)
    """
    from ultralytics.nn.modules.conv import Conv

    detect_head.nc = nc
    detect_head.no = nc + detect_head.reg_max * 4

    # 获取每个尺度的输入通道数
    ch = []
    for m in detect_head.cv2:
        input_ch = m[0].conv.in_channels
        ch.append(input_ch)

    # cv2: box回归
    c4 = max(ch[0] // 4, detect_head.reg_max * 4)
    detect_head.cv2 = nn.ModuleList(
        nn.Sequential(Conv(x, c4, 3), Conv(c4, c4, 3), nn.Conv2d(c4, detect_head.reg_max * 4, 1))
        for x in ch
    )

    # cv3: 类别预测
    c3 = max(ch[0] // 4, nc)
    detect_head.cv3 = nn.ModuleList(
        nn.Sequential(Conv(x, c3, 3), Conv(c3, c3, 3), nn.Conv2d(c3, nc, 1))
        for x in ch
    )


def convert_labels_if_needed():
    """检查并转换标签格式（分割多边形 -> 检测边界框）"""
    det_dir = os.path.join(ROOT, "草莓成熟度检测数据集_det")
    if not os.path.exists(det_dir):
        print("=" * 60)
        print("检测到尚未转换标签格式，正在转换...")
        print("=" * 60)
        from convert_seg_to_det import convert_dataset
        src_dir = os.path.join(ROOT, "草莓成熟度检测数据集")
        convert_dataset(src_dir, det_dir)
        print("标签转换完成!")
    else:
        print(f"检测格式数据集已存在: {det_dir}")


class YOLOv8SPTrainer(DetectionTrainer):
    """自定义YOLOv8-SP训练器，重写get_model方法"""

    def get_model(self, cfg=None, weights=None, verbose=True):
        """重写get_model，返回YOLOv8-SP检测模型"""
        register_custom_modules()

        # 用YOLOv8SPModel构建模型（自动处理nc变化和层替换）
        model = YOLOv8SPModel('yolov8n.yaml', nc=self.data['nc'], verbose=verbose)

        # 加载预训练权重（只加载匹配的参数）
        if weights:
            model.load(weights)
            print(f"  预训练权重加载完成")

        if verbose:
            model.info()

        return model


def train(args):
    """主训练函数"""
    # 1. 检查并转换标签格式
    convert_labels_if_needed()

    # 2. 数据配置路径
    data_yaml = os.path.join(ROOT, "草莓成熟度检测数据集_det", "data.yaml")

    # 3. 注册自定义模块
    register_custom_modules()

    # 4. 打印训练配置
    print("\n" + "=" * 60)
    print("YOLOv8-SP 检测模型训练配置")
    print("=" * 60)
    print(f"数据配置: {data_yaml}")
    print(f"训练轮数: {args.epochs}")
    print(f"批次大小: {args.batch}")
    print(f"图像尺寸: {args.imgsz}")
    print(f"设备: {args.device}")

    # 5. 创建YOLO模型（用yolov8n.pt确定任务类型为detect）
    model = YOLO('yolov8n.pt')

    # 6. 开始训练
    print("\n" + "=" * 60)
    print("开始训练 YOLOv8-SP 检测模型...")
    print("=" * 60)

    results = model.train(
        trainer=YOLOv8SPTrainer,
        data=data_yaml,
        epochs=args.epochs,
        batch=args.batch,
        imgsz=args.imgsz,
        device=args.device,
        project=args.project,
        name=args.name,
        # 优化参数
        optimizer=args.optimizer,
        lr0=args.lr0,
        lrf=args.lrf,
        weight_decay=args.weight_decay,
        warmup_epochs=args.warmup_epochs,
        # 数据增强
        augment=True,
        mosaic=1.0,
        mixup=0.1,
        # 其他
        patience=args.patience,
        save=True,
        save_period=args.save_period,
        val=True,
        verbose=True,
        amp=False,  # 禁用AMP: ELA模块在float16下梯度不稳定
    )

    print("\n" + "=" * 60)
    print("训练完成!")
    print("=" * 60)

    return results


def parse_args():
    parser = argparse.ArgumentParser(description='YOLOv8-SP 检测模型训练脚本')
    parser.add_argument('--epochs', type=int, default=100, help='训练轮数')
    parser.add_argument('--batch', type=int, default=16, help='批次大小')
    parser.add_argument('--imgsz', type=int, default=640, help='输入图像尺寸')
    parser.add_argument('--device', type=str, default='', help='训练设备 (cpu/0/1/...)')
    parser.add_argument('--project', type=str, default='runs/detect', help='项目保存目录')
    parser.add_argument('--name', type=str, default='yolov8sp', help='实验名称')
    parser.add_argument('--optimizer', type=str, default='auto', help='优化器')
    parser.add_argument('--lr0', type=float, default=0.01, help='初始学习率')
    parser.add_argument('--lrf', type=float, default=0.01, help='最终学习率系数')
    parser.add_argument('--weight_decay', type=float, default=0.0005, help='权重衰减')
    parser.add_argument('--warmup-epochs', type=float, default=3.0, help='预热轮数')
    parser.add_argument('--patience', type=int, default=50, help='早停耐心值')
    parser.add_argument('--save-period', type=int, default=10, help='模型保存周期')
    return parser.parse_args()


if __name__ == '__main__':
    args = parse_args()
    train(args)
