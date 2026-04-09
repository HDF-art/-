# 农业大数据联合建模平台 - 本地训练客户端

本目录包含本地训练所需的所有脚本。

## 文件说明

```
client/
├── local_trainer.py    # 主训练脚本（含环境检测）
├── run.sh             # 一键启动脚本
└── README.md          # 本文档
```

## 快速开始

### 1. 首次配置环境

```bash
cd client
./run.sh --setup
```

脚本会自动检测：
- Python版本（需要3.8+）
- pip包管理器
- GPU/CUDA
- 所需依赖包

### 2. 开始训练

```bash
# 使用默认参数训练
./run.sh --train

# 指定模型和参数
./run.sh --train --model CNN --epochs 10 --batch 32

# 使用自定义数据
./run.sh --train --data ./my_data.csv
```

## 参数说明

| 参数 | 简写 | 默认值 | 说明 |
|------|------|--------|------|
| --setup | -s | - | 配置环境 |
| --train | -t | - | 开始训练 |
| --model | -m | CNN | 模型类型 |
| --epochs | -e | 10 | 训练轮数 |
| --batch | -b | 32 | 批次大小 |
| --lr | -l | 0.01 | 学习率 |
| --rounds | -r | 5 | 联邦学习轮次 |
| --data | -d | - | 数据文件路径 |
| --server | - | admp.online | 服务器地址 |
| --gpu | - | CPU | 使用GPU训练 |

## 支持的模型

- **CNN** - 图像分类
- **LSTM** - 时序预测
- **XGBoost** - 表格数据
- **LinearRegression** - 线性回归
- **LogisticRegression** - 逻辑回归

## 数据格式

### CSV格式
```csv
feature1,feature2,feature3,...,label
0.1,0.2,0.3,...,1
```

### NumPy格式
```python
np.save('data.npz', X=data_array, y=label_array)
```

## 训练流程

```
1. 环境检测 → 自动检查Python、GPU、依赖
2. 环境配置 → 自动安装所需包
3. 数据加载 → 支持CSV/NumPy格式
4. 模型训练 → 本地执行训练
5. 结果保存 → 模型文件输出
```

## 常见问题

### Q: 提示"Python版本过低"
A: 请升级Python到3.8+

### Q: 提示"缺少torch"
A: 运行 `./run.sh --setup` 自动安装

### Q: 训练很慢
A: 确保电脑有NVIDIA GPU，或使用GPU版本PyTorch

### Q: 如何使用自己的数据？
A: 使用 `--data` 参数指定数据文件路径

## 平台集成

训练完成后，模型会自动与平台同步：

- 访问 https://admp.online
- 登录后进入"联邦学习"页面
- 可查看训练任务和模型
