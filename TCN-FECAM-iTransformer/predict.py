"""
TCN-FECAM-iTransformer 预测与可视化脚本

功能：
1. 加载训练好的模型
2. 在测试集上进行预测
3. 绘制预测vs真实值对比图
4. 绘制训练损失曲线
5. 绘制FECAM频率权重可视化
6. 输出评估指标
"""

import os
import numpy as np
import torch
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib import rcParams

from config import Config
from utils import get_dataloaders, inverse_transform_targets
from utils.metrics import evaluate
from models import TCN_FECAM_iTransformer

# 设置中文字体
rcParams["font.sans-serif"] = ["SimHei", "Microsoft YaHei", "DejaVu Sans"]
rcParams["axes.unicode_minus"] = False


def plot_predictions(preds, targets, target_names, save_dir, n_samples=5):
    """绘制预测vs真实值对比图"""
    n_targets = len(target_names)

    fig, axes = plt.subplots(n_targets, 1, figsize=(14, 4 * n_targets))
    if n_targets == 1:
        axes = [axes]

    for i, (ax, name) in enumerate(zip(axes, target_names)):
        # 取前n_samples个样本的平均值展示
        pred_mean = preds[:n_samples, :, i].mean(axis=0)
        target_mean = targets[:n_samples, :, i].mean(axis=0)

        ax.plot(target_mean, label="真实值", color="blue", linewidth=1.5)
        ax.plot(pred_mean, label="预测值", color="red", linewidth=1.5, linestyle="--")
        ax.fill_between(
            range(len(pred_mean)),
            preds[:n_samples, :, i].min(axis=0),
            preds[:n_samples, :, i].max(axis=0),
            alpha=0.2, color="red", label="预测范围"
        )
        ax.set_title(f"{name} - 预测 vs 真实值", fontsize=14)
        ax.set_xlabel("时间步")
        ax.set_ylabel(name)
        ax.legend()
        ax.grid(True, alpha=0.3)

    plt.tight_layout()
    save_path = os.path.join(save_dir, "predictions.png")
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"预测对比图已保存至: {save_path}")


def plot_scatter(preds, targets, target_names, save_dir):
    """绘制散点图"""
    n_targets = len(target_names)
    fig, axes = plt.subplots(1, n_targets, figsize=(6 * n_targets, 6))
    if n_targets == 1:
        axes = [axes]

    for i, (ax, name) in enumerate(zip(axes, target_names)):
        ax.scatter(targets[:, :, i].flatten(), preds[:, :, i].flatten(),
                   alpha=0.3, s=5, c="blue")
        min_val = min(targets[:, :, i].min(), preds[:, :, i].min())
        max_val = max(targets[:, :, i].max(), preds[:, :, i].max())
        ax.plot([min_val, max_val], [min_val, max_val], "r--", linewidth=2)
        ax.set_xlabel("真实值")
        ax.set_ylabel("预测值")
        ax.set_title(f"{name} - 散点图", fontsize=14)
        ax.grid(True, alpha=0.3)

    plt.tight_layout()
    save_path = os.path.join(save_dir, "scatter.png")
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"散点图已保存至: {save_path}")


def plot_training_curves(save_dir):
    """绘制训练损失曲线"""
    train_losses = np.load(os.path.join(save_dir, "train_losses.npy"))
    val_losses = np.load(os.path.join(save_dir, "val_losses.npy"))

    fig, ax = plt.subplots(figsize=(10, 6))
    ax.plot(train_losses, label="训练损失", color="blue", linewidth=1.5)
    ax.plot(val_losses, label="验证损失", color="red", linewidth=1.5)
    ax.set_xlabel("Epoch")
    ax.set_ylabel("Loss (MSE)")
    ax.set_title("训练损失曲线", fontsize=14)
    ax.legend()
    ax.grid(True, alpha=0.3)
    ax.set_yscale("log")

    plt.tight_layout()
    save_path = os.path.join(save_dir, "training_curves.png")
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"训练损失曲线已保存至: {save_path}")


def plot_freq_weights(model, save_dir):
    """可视化FECAM频率权重"""
    fecam = model.fecam
    n_groups = fecam.num_groups

    fig, axes = plt.subplots(1, n_groups, figsize=(5 * n_groups, 4))
    if n_groups == 1:
        axes = [axes]

    for g in range(n_groups):
        weights = torch.sigmoid(fecam.freq_filters[g].freq_weights).detach().cpu().numpy()
        ax = axes[g]
        ax.bar(range(len(weights)), weights, color="steelblue", alpha=0.7)
        ax.set_xlabel("频率分量")
        ax.set_ylabel("权重")
        ax.set_title(f"FECAM频率权重 - 组{g+1}", fontsize=12)
        ax.grid(True, alpha=0.3)

    plt.tight_layout()
    save_path = os.path.join(save_dir, "fecam_freq_weights.png")
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"FECAM频率权重图已保存至: {save_path}")


def plot_error_distribution(preds, targets, target_names, save_dir):
    """绘制预测误差分布"""
    n_targets = len(target_names)
    fig, axes = plt.subplots(1, n_targets, figsize=(6 * n_targets, 4))
    if n_targets == 1:
        axes = [axes]

    for i, (ax, name) in enumerate(zip(axes, target_names)):
        errors = (preds[:, :, i] - targets[:, :, i]).flatten()
        ax.hist(errors, bins=50, density=True, color="steelblue", alpha=0.7, edgecolor="black")
        ax.axvline(x=0, color="red", linestyle="--", linewidth=2)
        ax.set_xlabel("预测误差")
        ax.set_ylabel("密度")
        ax.set_title(f"{name} - 误差分布", fontsize=14)
        ax.grid(True, alpha=0.3)

    plt.tight_layout()
    save_path = os.path.join(save_dir, "error_distribution.png")
    plt.savefig(save_path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"误差分布图已保存至: {save_path}")


def main():
    config = Config()
    device = config.DEVICE

    print("=" * 80)
    print("TCN-FECAM-iTransformer 预测与可视化")
    print("=" * 80)

    # 加载数据
    print("\n加载数据...")
    train_loader, val_loader, test_loader, scaler, target_indices = get_dataloaders(config)
    print(f"测试集: {len(test_loader.dataset)} 样本")

    # 加载模型
    print("\n加载模型...")
    checkpoint = torch.load(config.MODEL_SAVE_PATH, map_location=device, weights_only=False)
    model = TCN_FECAM_iTransformer(config).to(device)
    model.load_state_dict(checkpoint["model_state_dict"])
    model.eval()
    print(f"模型加载完成 (Epoch {checkpoint['epoch']}, Val Loss: {checkpoint['val_loss']:.6f})")

    # 预测
    print("\n在测试集上预测...")
    all_preds = []
    all_targets = []

    with torch.no_grad():
        for batch_x, batch_y in test_loader:
            batch_x = batch_x.to(device)
            pred = model(batch_x)
            all_preds.append(pred.cpu().numpy())
            all_targets.append(batch_y.numpy())

    all_preds = np.concatenate(all_preds, axis=0)
    all_targets = np.concatenate(all_targets, axis=0)

    # 反标准化
    all_preds_inv = inverse_transform_targets(scaler, target_indices, all_preds)
    all_targets_inv = inverse_transform_targets(scaler, target_indices, all_targets)

    target_names = config.TARGET_COLUMNS

    # 计算指标
    print("\n各目标变量评估指标:")
    print(f"{'变量':>15} | {'MAE':>10} | {'MSE':>10} | {'RMSE':>10} | {'MAPE(%)':>10} | {'R²':>10}")
    print("-" * 80)

    for i, name in enumerate(target_names):
        metrics = evaluate(all_targets_inv[:, :, i], all_preds_inv[:, :, i])
        print(f"{name:>15} | {metrics['MAE']:10.4f} | {metrics['MSE']:10.4f} | "
              f"{metrics['RMSE']:10.4f} | {metrics['MAPE']:10.2f} | {metrics['R2']:10.4f}")

    # 绘图
    print("\n生成可视化图表...")
    save_dir = config.RESULT_SAVE_DIR
    os.makedirs(save_dir, exist_ok=True)

    # 预测对比图
    plot_predictions(all_preds_inv, all_targets_inv, target_names, save_dir)

    # 散点图
    plot_scatter(all_preds_inv, all_targets_inv, target_names, save_dir)

    # 训练损失曲线
    if os.path.exists(os.path.join(save_dir, "train_losses.npy")):
        plot_training_curves(save_dir)

    # FECAM频率权重
    plot_freq_weights(model, save_dir)

    # 误差分布
    plot_error_distribution(all_preds_inv, all_targets_inv, target_names, save_dir)

    # 保存预测结果
    np.save(os.path.join(save_dir, "test_preds.npy"), all_preds_inv)
    np.save(os.path.join(save_dir, "test_targets.npy"), all_targets_inv)

    print(f"\n所有结果已保存至 {save_dir}/")
    print("=" * 80)


if __name__ == "__main__":
    main()
