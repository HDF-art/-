#!/bin/bash
# 农业大数据联合建模平台 - 一键训练脚本

echo "🌾 农业大数据联合建模平台 - 本地训练"
echo "========================================"

# 检查Python
if ! command -v python3 &> /dev/null; then
    echo "错误: 未找到Python3"
    echo "请安装Python 3.8+: https://www.python.org/downloads/"
    exit 1
fi

# 检查参数
if [ "$1" == "--setup" ]; then
    echo "开始配置环境..."
    python3 local_trainer.py --setup $@
    exit $?
fi

if [ "$1" == "--train" ] || [ "$1" == "-t" ]; then
    echo "开始训练..."
    python3 local_trainer.py --train $@
    exit $?
fi

# 显示帮助
echo ""
echo "使用方法:"
echo "  ./run.sh --setup          # 配置环境（首次运行）"
echo "  ./run.sh --train          # 开始训练"
echo "  ./run.sh --train --model LSTM --epochs 20  # 自定义参数"
echo ""
echo "完整参数:"
python3 local_trainer.py --help
