# 农业大数据联合建模平台 - 本地客户端

## 简介

本地客户端是一个运行在用户电脑上的Python程序，它实现了"数据不出本地、平台远程控制"的核心功能。

## 特点

- **数据隐私保护**: 所有数据存储在本地 `~/fedlearn-data/` 目录，永不离开你的硬盘
- **远程控制**: 通过WebSocket与平台通信，在网页上操作本地计算资源
- **实时监控**: 监控本地数据集变化，实时同步到平台
- **训练管理**: 支持启动、停止、监控训练任务
- **完全控制**: 可随时断开网络，本地训练继续运行

## 安装

```bash
cd local-client
pip install -r requirements.txt
```

## 使用方法

### 基本启动

```bash
python local_client.py
```

### 带参数启动

```bash
python local_client.py --port 8080 --platform wss://admp.online/ws/local-client --token YOUR_TOKEN
```

### 参数说明

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `--port` | 本地HTTP服务端口 | 8080 |
| `--platform` | 平台WebSocket地址 | wss://admp.online/ws/local-client |
| `--token` | 认证Token | 无 |
| `--client-id` | 客户端ID | 自动生成 |

## 目录结构

```
~/fedlearn-data/
├── datasets/      # 存放数据集文件
├── models/        # 存放训练好的模型
├── results/       # 存放评估结果
└── logs/          # 存放运行日志
```

## 本地API

客户端启动后，可以通过 `http://localhost:8080` 访问本地API：

- `GET /status` - 获取客户端状态
- `GET /datasets` - 获取本地数据集列表

## 支持的命令

| 命令 | 说明 |
|------|------|
| `ping` | 心跳检测 |
| `get_datasets` | 获取数据集列表 |
| `get_dataset_info` | 获取数据集详情 |
| `start_training` | 启动训练任务 |
| `stop_training` | 停止训练任务 |
| `get_training_status` | 获取训练状态 |
| `evaluate_model` | 评估模型 |
| `get_system_info` | 获取系统信息 |
| `set_hyperparameters` | 设置超参数 |

## 安全说明

1. 所有数据存储在本地，不会上传到平台
2. WebSocket连接使用WSS加密
3. 支持Token认证
4. 本地HTTP服务仅监听localhost

## 故障排除

### 无法连接到平台

1. 检查网络连接
2. 确认平台地址正确
3. 检查Token是否有效

### 数据集未识别

1. 确认文件在 `~/fedlearn-data/datasets/` 目录
2. 检查文件格式（支持CSV、JSON等）
3. 查看客户端日志

## 许可证

MIT License
