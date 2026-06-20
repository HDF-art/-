"""
将分割多边形标签转换为YOLO检测标签
格式: class_id x1 y1 x2 y2 ... xn yn -> class_id cx cy w h
"""

import os
import sys
from pathlib import Path


def convert_label_file(src_path, dst_path):
    """转换单个标签文件: 分割多边形 -> 检测边界框"""
    with open(src_path, 'r') as f:
        lines = f.readlines()

    det_lines = []
    for line in lines:
        parts = line.strip().split()
        if len(parts) < 5:  # 至少需要 class_id + 2个点(4个坐标)
            continue
        class_id = parts[0]
        coords = [float(x) for x in parts[1:]]

        # 提取x和y坐标
        xs = coords[0::2]
        ys = coords[1::2]

        # 计算边界框
        x_min, x_max = min(xs), max(xs)
        y_min, y_max = min(ys), max(ys)

        # 转换为中心点+宽高格式
        cx = (x_min + x_max) / 2
        cy = (y_min + y_max) / 2
        w = x_max - x_min
        h = y_max - y_min

        # 裁剪到[0,1]范围
        cx = max(0, min(1, cx))
        cy = max(0, min(1, cy))
        w = max(0, min(1, w))
        h = max(0, min(1, h))

        det_lines.append(f"{class_id} {cx:.6f} {cy:.6f} {w:.6f} {h:.6f}\n")

    with open(dst_path, 'w') as f:
        f.writelines(det_lines)


def convert_dataset(src_dir, dst_dir):
    """转换整个数据集的标签"""
    src_dir = Path(src_dir)
    dst_dir = Path(dst_dir)

    for split in ['train', 'test']:
        src_label_dir = src_dir / split / 'labels'
        dst_label_dir = dst_dir / split / 'labels'
        src_img_dir = src_dir / split / 'images'
        dst_img_dir = dst_dir / split / 'images'

        if not src_label_dir.exists():
            print(f"跳过 {split}: 标签目录不存在")
            continue

        # 创建目标目录
        dst_label_dir.mkdir(parents=True, exist_ok=True)
        dst_img_dir.mkdir(parents=True, exist_ok=True)

        # 复制图片（符号链接或复制）
        if src_img_dir.exists():
            for img_file in src_img_dir.iterdir():
                dst_img = dst_img_dir / img_file.name
                if not dst_img.exists():
                    # Windows上用复制
                    import shutil
                    shutil.copy2(str(img_file), str(dst_img))

        # 转换标签
        count = 0
        for label_file in src_label_dir.glob('*.txt'):
            dst_label = dst_label_dir / label_file.name
            convert_label_file(str(label_file), str(dst_label))
            count += 1

        print(f"  {split}: 转换了 {count} 个标签文件")

    # 创建data.yaml
    create_data_yaml(dst_dir)


def create_data_yaml(dataset_dir):
    """创建检测数据集的data.yaml"""
    dataset_dir = Path(dataset_dir)
    yaml_content = f"""path: {dataset_dir}
train: train/images
val: test/images
test: test/images

nc: 3
names: ['unripe', 'ripe', 'overripe']
"""
    yaml_path = dataset_dir / 'data.yaml'
    with open(yaml_path, 'w', encoding='utf-8') as f:
        f.write(yaml_content)
    print(f"  创建了 {yaml_path}")


if __name__ == '__main__':
    src = os.path.join(os.path.dirname(os.path.abspath(__file__)), '草莓成熟度检测数据集')
    dst = os.path.join(os.path.dirname(os.path.abspath(__file__)), '草莓成熟度检测数据集_det')
    convert_dataset(src, dst)
    print("转换完成!")
