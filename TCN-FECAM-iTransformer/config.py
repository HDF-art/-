"""TCN-FECAM-iTransformer 温湿度预测模型配置"""

import torch


class Config:
    # ==================== 数据配置 ====================
    # 数据文件路径
    DATA_FILE = "温室春茬番茄种植监测数据（2024年5月1日~2025年7月10日）.xlsx"

    # 使用的变量列名（8个环境变量）
    FEATURE_COLUMNS = [
        "空气温度（℃）",
        "空气湿度（%）",
        "20cm土壤水分（%）",
        "40cm土壤水分（%）",
        "60cm土壤水分（%）",
        "20cm土壤温度（℃）",
        "40cm土壤温度（℃）",
        "60cm土壤温度（℃）",
    ]

    # 预测目标列名（空气温度和空气湿度）
    TARGET_COLUMNS = ["空气温度（℃）", "空气湿度（%）"]

    # 是否使用时间特征（小时/日/月的sin-cos编码）
    USE_TIME_FEATURES = False

    # 采样间隔（分钟），原始数据为5分钟
    RESAMPLE_MINUTES = 30

    # 序列长度（用过去多少个时间步预测）
    SEQ_LEN = 96

    # 预测长度（预测未来多少个时间步）
    PRED_LEN = 24

    # 训练集比例
    TRAIN_RATIO = 0.7
    # 验证集比例
    VAL_RATIO = 0.15
    # 测试集比例（剩余部分）

    # ==================== 模型配置 ====================
    # 模型维度（d_model）
    D_MODEL = 32

    # 多头注意力头数
    N_HEADS = 4

    # iTransformer编码器层数
    ITRANSFORMER_LAYERS = 2

    # TCN层数（每层膨胀系数为2^i）
    TCN_LAYERS = 3

    # TCN卷积核大小
    TCN_KERNEL_SIZE = 3

    # TCN基础通道数
    TCN_CHANNELS = 32

    # TCN dropout
    TCN_DROPOUT = 0.2

    # FECAM频率分组数
    FECAM_NUM_GROUPS = 4

    # FECAM频率分量数（DCT保留的频率分量数）
    FECAM_NUM_FREQS = 8

    # 交叉注意力头数
    CROSS_ATTN_HEADS = 4

    # 交叉注意力层数
    CROSS_ATTN_LAYERS = 1

    # 全局dropout
    DROPOUT = 0.3

    # 是否使用DLinear直接预测路径（残差捷径）
    USE_DLINEAR_SHORTCUT = False

    # ==================== 训练配置 ====================
    # 批大小
    BATCH_SIZE = 64

    # 学习率
    LR = 0.0003

    # 训练轮数
    EPOCHS = 200

    # 早停耐心值
    PATIENCE = 30

    # 训练时输入噪声标准差（数据增强）
    INPUT_NOISE_STD = 0.0

    # 设备
    DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    # 模型保存路径
    MODEL_SAVE_PATH = "checkpoints/best_model.pth"

    # 结果保存路径
    RESULT_SAVE_DIR = "results"
