"""
TCN-FECAM-iTransformer 训练脚本

功能：
1. 加载和预处理数据
2. 构建模型
3. 训练模型（含早停机制）
4. 保存最佳模型
5. 输出训练日志
"""

import os
import sys
import time
import math
import numpy as np
import torch
import torch.nn as nn
from torch.optim import AdamW
from torch.optim.lr_scheduler import LambdaLR

from config import Config
from utils import get_dataloaders
from utils.metrics import evaluate
from models import TCN_FECAM_iTransformer


class WeightedHuberLoss(nn.Module):
    """加权Huber损失，对不同目标变量赋予不同权重"""

    def __init__(self, delta=1.0, weights=None):
        super().__init__()
        self.delta = delta
        self.weights = weights  # (n_targets,)

    def forward(self, pred, target):
        # Huber损失
        diff = pred - target
        abs_diff = diff.abs()
        quadratic = torch.clamp(abs_diff, max=self.delta)
        linear = abs_diff - quadratic
        huber = 0.5 * quadratic ** 2 + self.delta * linear

        # 按目标变量加权
        if self.weights is not None:
            w = self.weights.to(huber.device)
            # huber: (batch, pred_len, n_targets), w: (n_targets,)
            huber = huber * w.view(1, 1, -1)

        return huber.mean()


def get_warmup_cosine_scheduler(optimizer, warmup_steps, total_steps, eta_min=1e-6):
    """带warmup的余弦退火调度器"""
    def lr_lambda(step):
        if step < warmup_steps:
            return float(step) / float(max(1, warmup_steps))
        progress = float(step - warmup_steps) / float(max(1, total_steps - warmup_steps))
        return max(eta_min, 0.5 * (1.0 + math.cos(math.pi * progress)))
    return LambdaLR(optimizer, lr_lambda)


def train_one_epoch(model, train_loader, criterion, optimizer, device, scheduler=None, noise_std=0.0):
    """训练一个epoch"""
    model.train()
    total_loss = 0
    n_batches = 0

    for batch_x, batch_y in train_loader:
        batch_x = batch_x.to(device)
        batch_y = batch_y.to(device)

        # 数据增强：添加高斯噪声
        if noise_std > 0:
            batch_x = batch_x + torch.randn_like(batch_x) * noise_std

        optimizer.zero_grad()

        # 前向传播
        pred = model(batch_x)
        loss = criterion(pred, batch_y)

        # 反向传播
        loss.backward()

        # 梯度裁剪
        torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)

        optimizer.step()

        if scheduler is not None:
            scheduler.step()

        total_loss += loss.item()
        n_batches += 1

    return total_loss / n_batches


@torch.no_grad()
def evaluate_model(model, data_loader, criterion, device):
    """评估模型"""
    model.eval()
    total_loss = 0
    n_batches = 0
    all_preds = []
    all_targets = []

    for batch_x, batch_y in data_loader:
        batch_x = batch_x.to(device)
        batch_y = batch_y.to(device)

        pred = model(batch_x)
        loss = criterion(pred, batch_y)

        total_loss += loss.item()
        n_batches += 1

        all_preds.append(pred.cpu().numpy())
        all_targets.append(batch_y.cpu().numpy())

    all_preds = np.concatenate(all_preds, axis=0)
    all_targets = np.concatenate(all_targets, axis=0)

    return total_loss / n_batches, all_preds, all_targets


def main():
    config = Config()
    device = config.DEVICE

    # 设置随机种子
    torch.manual_seed(1)
    torch.cuda.manual_seed_all(1)
    np.random.seed(1)

    print("=" * 80)
    print("TCN-FECAM-iTransformer 温湿度预测模型训练")
    print("=" * 80)
    print(f"设备: {device}")
    print(f"输入序列长度: {config.SEQ_LEN}")
    print(f"预测长度: {config.PRED_LEN}")
    print(f"特征数: {len(config.FEATURE_COLUMNS)}")
    print(f"目标数: {len(config.TARGET_COLUMNS)}")
    print(f"模型维度: {config.D_MODEL}")
    print(f"iTransformer层数: {config.ITRANSFORMER_LAYERS}")
    print(f"TCN层数: {config.TCN_LAYERS}")
    print(f"FECAM分组数: {config.FECAM_NUM_GROUPS}")
    print(f"交叉注意力层数: {config.CROSS_ATTN_LAYERS}")
    print("=" * 80)

    # 创建保存目录
    os.makedirs(os.path.dirname(config.MODEL_SAVE_PATH), exist_ok=True)
    os.makedirs(config.RESULT_SAVE_DIR, exist_ok=True)

    # 加载数据
    print("\n加载数据...")
    train_loader, val_loader, test_loader, scaler, target_indices = get_dataloaders(config)
    print(f"训练集: {len(train_loader.dataset)} 样本")
    print(f"验证集: {len(val_loader.dataset)} 样本")
    print(f"测试集: {len(test_loader.dataset)} 样本")

    # 构建模型
    print("\n构建模型...")
    model = TCN_FECAM_iTransformer(config).to(device)

    # 统计参数量
    n_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
    print(f"模型参数量: {n_params:,}")

    # 损失函数（Huber Loss对异常值更鲁棒）
    criterion = nn.HuberLoss(delta=1.0)

    # 优化器（增大weight_decay抑制过拟合）
    optimizer = AdamW(model.parameters(), lr=config.LR, weight_decay=5e-3)

    # 带warmup的余弦退火调度器
    total_steps = config.EPOCHS * len(train_loader)
    warmup_steps = min(3 * len(train_loader), total_steps // 20)
    scheduler = get_warmup_cosine_scheduler(
        optimizer, warmup_steps, total_steps, eta_min=1e-6
    )

    # 记录最佳验证损失（用于保存最佳模型）
    best_val_loss = float("inf")

    # 训练循环
    print("\n开始训练...")
    print(f"{'Epoch':>6} | {'Train Loss':>12} | {'Val Loss':>12} | {'LR':>10} | {'Time':>8} | {'Status':>8}")
    print("-" * 80)

    train_losses = []
    val_losses = []

    for epoch in range(1, config.EPOCHS + 1):
        start_time = time.time()

        # 训练（含输入噪声数据增强）
        train_loss = train_one_epoch(
            model, train_loader, criterion, optimizer, device, scheduler,
            noise_std=config.INPUT_NOISE_STD
        )

        # 验证
        val_loss, _, _ = evaluate_model(model, val_loader, criterion, device)

        elapsed = time.time() - start_time

        train_losses.append(train_loss)
        val_losses.append(val_loss)

        # 保存最佳模型
        status = ""
        if val_loss < best_val_loss:
            best_val_loss = val_loss
            torch.save({
                "model_state_dict": model.state_dict(),
                "optimizer_state_dict": optimizer.state_dict(),
                "epoch": epoch,
                "val_loss": val_loss,
                "config": {
                    "SEQ_LEN": config.SEQ_LEN,
                    "PRED_LEN": config.PRED_LEN,
                    "D_MODEL": config.D_MODEL,
                    "N_HEADS": config.N_HEADS,
                    "ITRANSFORMER_LAYERS": config.ITRANSFORMER_LAYERS,
                    "TCN_LAYERS": config.TCN_LAYERS,
                    "TCN_KERNEL_SIZE": config.TCN_KERNEL_SIZE,
                    "FECAM_NUM_GROUPS": config.FECAM_NUM_GROUPS,
                    "FECAM_NUM_FREQS": config.FECAM_NUM_FREQS,
                    "CROSS_ATTN_HEADS": config.CROSS_ATTN_HEADS,
                    "CROSS_ATTN_LAYERS": config.CROSS_ATTN_LAYERS,
                    "FEATURE_COLUMNS": config.FEATURE_COLUMNS,
                    "TARGET_COLUMNS": config.TARGET_COLUMNS,
                },
            }, config.MODEL_SAVE_PATH)
            status = "BEST"
        else:
            status = ""

        current_lr = optimizer.param_groups[0]["lr"]

        print(f"{epoch:6d} | {train_loss:12.6f} | {val_loss:12.6f} | {current_lr:10.6f} | {elapsed:7.1f}s | {status:>8}")

    print("-" * 80)
    print(f"训练完成，最佳验证损失: {best_val_loss:.6f}")
    print(f"最佳模型已保存至: {config.MODEL_SAVE_PATH}")

    # 保存训练曲线数据
    np.save(os.path.join(config.RESULT_SAVE_DIR, "train_losses.npy"), np.array(train_losses))
    np.save(os.path.join(config.RESULT_SAVE_DIR, "val_losses.npy"), np.array(val_losses))

    # 测试集评估
    print("\n在测试集上评估...")
    checkpoint = torch.load(config.MODEL_SAVE_PATH, map_location=device, weights_only=False)
    model.load_state_dict(checkpoint["model_state_dict"])

    test_loss, test_preds, test_targets = evaluate_model(model, test_loader, criterion, device)

    # 反标准化
    from utils import inverse_transform_targets
    test_preds_inv = inverse_transform_targets(scaler, target_indices, test_preds)
    test_targets_inv = inverse_transform_targets(scaler, target_indices, test_targets)

    # 计算指标
    target_names = config.TARGET_COLUMNS
    print(f"\n测试集损失 (MSE): {test_loss:.6f}")
    print("\n各目标变量评估指标:")
    print(f"{'变量':>15} | {'MAE':>10} | {'RMSE':>10} | {'MAPE(%)':>10} | {'R²':>10}")
    print("-" * 65)

    for i, name in enumerate(target_names):
        metrics = evaluate(test_targets_inv[:, :, i], test_preds_inv[:, :, i])
        print(f"{name:>15} | {metrics['MAE']:10.4f} | {metrics['RMSE']:10.4f} | {metrics['MAPE']:10.2f} | {metrics['R2']:10.4f}")

    # 保存预测结果
    np.save(os.path.join(config.RESULT_SAVE_DIR, "test_preds.npy"), test_preds_inv)
    np.save(os.path.join(config.RESULT_SAVE_DIR, "test_targets.npy"), test_targets_inv)

    print(f"\n预测结果已保存至 {config.RESULT_SAVE_DIR}/")


if __name__ == "__main__":
    main()
