"""
数据预处理与加载模块

功能：
1. 从Excel读取温室监测数据（空气温湿度 + 土壤墒情）
2. 按时间对齐合并两个数据表
3. 重采样到统一时间间隔
4. 标准化处理
5. 构建滑动窗口数据集
"""

import os
import numpy as np
import pandas as pd
import torch
from torch.utils.data import Dataset, DataLoader
from sklearn.preprocessing import StandardScaler

from config import Config


def load_and_preprocess_data(config):
    """
    加载并预处理数据

    Returns:
        data: 合并后的DataFrame，包含所有特征列（含时间特征）
        time_feature_dim: 时间特征维度数（0表示不使用）
    """
    data_file = config.DATA_FILE

    # 读取空气温湿度数据
    df_air = pd.read_excel(data_file, sheet_name="空气温湿度")
    df_air["时间"] = pd.to_datetime(df_air["时间"])
    df_air = df_air.set_index("时间")

    # 读取土壤墒情数据
    df_soil = pd.read_excel(data_file, sheet_name="土壤墒情")
    df_soil["时间"] = pd.to_datetime(df_soil["时间"])
    df_soil = df_soil.set_index("时间")

    # 按指定间隔重采样（取均值聚合）
    resample_rule = f"{config.RESAMPLE_MINUTES}min"
    df_air = df_air.resample(resample_rule).mean()
    df_soil = df_soil.resample(resample_rule).mean()

    # 合并两个数据表（按时间索引对齐，取交集）
    data = df_air.join(df_soil, how="inner")

    # 插值填充可能的缺失值
    data = data.interpolate(method="linear")
    data = data.dropna()

    # 确保只使用配置中指定的特征列
    feature_cols = config.FEATURE_COLUMNS
    data = data[feature_cols].copy()

    time_feature_dim = 0
    # 添加时间特征（sin-cos编码）
    if getattr(config, "USE_TIME_FEATURES", False):
        time_idx = data.index
        # 每天采样数 = 24*60 / RESAMPLE_MINUTES
        steps_per_day = 24 * 60 / config.RESAMPLE_MINUTES
        steps_per_year = 365 * steps_per_day

        hour_angle = 2 * np.pi * (time_idx.hour + time_idx.minute / 60) / 24
        day_angle = 2 * np.pi * time_idx.dayofyear / 365
        month_angle = 2 * np.pi * time_idx.month / 12

        data["hour_sin"] = np.sin(hour_angle)
        data["hour_cos"] = np.cos(hour_angle)
        data["day_sin"] = np.sin(day_angle)
        data["day_cos"] = np.cos(day_angle)
        data["month_sin"] = np.sin(month_angle)
        data["month_cos"] = np.cos(month_angle)

        time_feature_dim = 6

    return data, time_feature_dim


def create_scaler(data):
    """创建标准化器"""
    scaler = StandardScaler()
    scaler.fit(data.values)
    return scaler


def split_data(data, config):
    """
    按比例划分训练集、验证集、测试集

    Returns:
        train_data, val_data, test_data: 三个DataFrame
    """
    n = len(data)
    train_end = int(n * config.TRAIN_RATIO)
    val_end = int(n * (config.TRAIN_RATIO + config.VAL_RATIO))

    train_data = data.iloc[:train_end]
    val_data = data.iloc[train_end:val_end]
    test_data = data.iloc[val_end:]

    return train_data, val_data, test_data


class TimeSeriesDataset(Dataset):
    """
    滑动窗口时间序列数据集

    输入: (batch, seq_len, n_features)
    目标: (batch, pred_len, n_targets)
    """

    def __init__(self, data, targets, seq_len, pred_len):
        """
        Args:
            data: numpy array, shape (n_samples, n_features)
            targets: numpy array, shape (n_samples, n_targets)
            seq_len: 输入序列长度
            pred_len: 预测长度
        """
        self.data = data
        self.targets = targets
        self.seq_len = seq_len
        self.pred_len = pred_len
        self.n_samples = len(data) - seq_len - pred_len + 1

    def __len__(self):
        return max(0, self.n_samples)

    def __getitem__(self, idx):
        # 输入序列: (seq_len, n_features)
        x = self.data[idx: idx + self.seq_len]

        # 目标序列: (pred_len, n_targets)
        y = self.targets[idx + self.seq_len: idx + self.seq_len + self.pred_len]

        return torch.FloatTensor(x), torch.FloatTensor(y)


def get_dataloaders(config):
    """
    获取训练、验证、测试数据加载器

    Returns:
        train_loader, val_loader, test_loader, scaler, target_indices
    """
    # 加载并预处理数据
    data, time_feature_dim = load_and_preprocess_data(config)

    # 划分数据集
    train_data, val_data, test_data = split_data(data, config)

    # 创建标准化器（仅在训练集上拟合）
    scaler = create_scaler(train_data)

    # 标准化
    train_scaled = scaler.transform(train_data.values)
    val_scaled = scaler.transform(val_data.values)
    test_scaled = scaler.transform(test_data.values)

    # 提取目标列索引（目标变量在原始特征列中的位置）
    target_indices = [config.FEATURE_COLUMNS.index(col) for col in config.TARGET_COLUMNS]

    # 构建目标数组（目标变量在标准化后的数据中的列索引不变，
    # 因为时间特征追加在原始特征列之后）
    train_targets = train_scaled[:, target_indices]
    val_targets = val_scaled[:, target_indices]
    test_targets = test_scaled[:, target_indices]

    # 创建数据集
    train_dataset = TimeSeriesDataset(train_scaled, train_targets,
                                      config.SEQ_LEN, config.PRED_LEN)
    val_dataset = TimeSeriesDataset(val_scaled, val_targets,
                                    config.SEQ_LEN, config.PRED_LEN)
    test_dataset = TimeSeriesDataset(test_scaled, test_targets,
                                     config.SEQ_LEN, config.PRED_LEN)

    # 创建数据加载器
    train_loader = DataLoader(train_dataset, batch_size=config.BATCH_SIZE,
                              shuffle=True, drop_last=True)
    val_loader = DataLoader(val_dataset, batch_size=config.BATCH_SIZE,
                            shuffle=False, drop_last=False)
    test_loader = DataLoader(test_dataset, batch_size=config.BATCH_SIZE,
                             shuffle=False, drop_last=False)

    return train_loader, val_loader, test_loader, scaler, target_indices


def inverse_transform_targets(scaler, target_indices, values):
    """
    反标准化目标值

    Args:
        scaler: StandardScaler
        target_indices: 目标列索引列表
        values: numpy array, shape (..., n_targets)

    Returns:
        反标准化后的值
    """
    n_features = len(scaler.mean_)
    n_targets = len(target_indices)

    # 构建完整维度的数组
    full_values = np.zeros((values.shape[0], values.shape[1], n_features))
    full_values[:, :, target_indices] = values

    # 反标准化
    full_values_inv = scaler.inverse_transform(
        full_values.reshape(-1, n_features)
    ).reshape(values.shape[0], values.shape[1], n_features)

    return full_values_inv[:, :, target_indices]
