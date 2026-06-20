"""
YOLOv8-SP 检测预测脚本
草莓成熟度分类（目标检测）

使用方法:
    python predict.py --source 图片路径
    python predict.py --source 图片目录
"""

import sys
import os

ROOT = os.path.dirname(os.path.abspath(__file__))
if ROOT not in sys.path:
    sys.path.insert(0, ROOT)

import argparse
import cv2
import numpy as np

# 注册自定义模块（必须在import YOLO之前）
from yolov8sp.modules import StarELA, BottleneckELA, C2fStarELA, CCFM, CCFMBlock
import ultralytics.nn.tasks as tasks
import ultralytics.nn.modules as nn_modules

for module_cls in [StarELA, BottleneckELA, C2fStarELA, CCFM, CCFMBlock]:
    setattr(tasks, module_cls.__name__, module_cls)
    setattr(nn_modules, module_cls.__name__, module_cls)

# 导入train中的YOLOv8SPModel（重写fuse以跳过BatchNorm1d）
from train import YOLOv8SPModel
tasks.YOLOv8SPModel = YOLOv8SPModel

from ultralytics import YOLO

# 类别名称映射
CLASS_NAMES = {0: '未成熟(unripe)', 1: '成熟(ripe)', 2: '过熟(overripe)'}

# 类别颜色 (BGR)
CLASS_COLORS = {
    0: (0, 255, 255),   # 未成熟 - 黄色
    1: (0, 255, 0),     # 成熟 - 绿色
    2: (0, 0, 255),     # 过熟 - 红色
}


def predict(source, model_path='runs/detect/yolov8sp_v5/weights/best.pt',
            conf=0.25, save_dir='runs/predict'):
    """加载模型并预测"""
    # 加载模型
    print(f"加载模型: {model_path}")
    model = YOLO(model_path)

    # 预测
    print(f"预测: {source}")
    results = model.predict(
        source=source,
        conf=conf,
        save=True,
        project=save_dir,
        name='',
        exist_ok=True,
        verbose=True,
    )

    # 打印结果
    for r in results:
        print(f"\n图片: {r.path}")
        boxes = r.boxes
        if len(boxes) == 0:
            print("  未检测到目标")
            continue
        for i, box in enumerate(boxes):
            cls_id = int(box.cls[0])
            conf_val = float(box.conf[0])
            x1, y1, x2, y2 = box.xyxy[0].tolist()
            cls_name = CLASS_NAMES.get(cls_id, f'class_{cls_id}')
            print(f"  目标 {i + 1}: {cls_name} (置信度: {conf_val:.2f})")
            print(f"    边界框: ({x1:.1f}, {y1:.1f}, {x2:.1f}, {y2:.1f})")

    return results


def main():
    parser = argparse.ArgumentParser(description='YOLOv8-SP 检测预测脚本')
    parser.add_argument('--source', type=str, required=True, help='图片路径或目录')
    parser.add_argument('--model', type=str,
                        default='runs/detect/yolov8sp_v5/weights/best.pt',
                        help='模型权重路径')
    parser.add_argument('--conf', type=float, default=0.25, help='置信度阈值')
    parser.add_argument('--save-dir', type=str, default='runs/predict', help='保存目录')
    args = parser.parse_args()

    predict(args.source, args.model, args.conf, args.save_dir)


if __name__ == '__main__':
    main()
