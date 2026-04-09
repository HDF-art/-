#!/usr/bin/env python3
"""
农业大数据联合建模平台 - 一键训练脚本
Professor 专用本地训练客户端
"""
import os
import sys
import subprocess
import platform

# 颜色
C_GREEN = '\033[92m'
C_YELLOW = '\033[93m'
C_RED = '\033[91m'
C_BLUE = '\033[94m'
C_END = '\033[0m'

def log(msg, color=C_GREEN):
    print(f"{color}{msg}{C_END}")

def step(msg):
    print(f"{C_BLUE}➜ {msg}{C_END}")

def err(msg):
    print(f"{C_RED}✗ {msg}{C_END}", file=sys.stderr)

def main():
    print(f"\n{'='*50}")
    print(f"🌾 农业大数据联合建模平台 - 本地训练")
    print(f"{'='*50}\n")
    
    # 检查Python
    if sys.version_info < (3, 8):
        err(f"Python版本过低: {sys.version}, 需要 3.8+")
        return 1
    
    log(f"Python版本: {sys.version}", C_YELLOW)
    
    # 检查torch
    try:
        import torch
        device = "cuda" if torch.cuda.is_available() else "cpu"
        log(f"PyTorch已安装, 设备: {device}", C_GREEN)
    except ImportError:
        log("正在安装PyTorch...", C_YELLOW)
        subprocess.run([sys.executable, "-m", "pip", "install", "torch", "torchvision", "torchaudio", 
                      "--index-url", "https://download.pytorch.org/whl/cpu"], check=True)
        import torch
        log("PyTorch安装完成", C_GREEN)
    
    # 检查依赖
    deps = ["numpy", "pandas", "sklearn", "matplotlib"]
    for dep in deps:
        try:
            __import__(dep)
        except:
            log(f"安装 {dep}...", C_YELLOW)
            subprocess.run([sys.executable, "-m", "pip", "install", dep], check=True)
    
    log("环境检查完成！\n", C_GREEN)
    
    # 开始训练
    import torch
    import torch.nn as nn
    from torch.utils.data import DataLoader, TensorDataset
    from sklearn.datasets import make_classification
    from sklearn.model_selection import train_test_split
    
    step("生成模拟数据...")
    X, y = make_classification(n_samples=5000, n_features=20, n_classes=10, random_state=42)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    log(f"训练集: {len(X_train)}, 测试集: {len(X_test)}")
    
    # 创建模型
    step("创建模型...")
    model = nn.Sequential(
        nn.Linear(20, 64),
        nn.ReLU(),
        nn.Linear(64, 32),
        nn.ReLU(),
        nn.Linear(32, 10)
    )
    
    # 训练
    criterion = nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=0.01)
    
    X_t = torch.FloatTensor(X_train)
    y_t = torch.LongTensor(y_train)
    loader = DataLoader(TensorDataset(X_t, y_t), batch_size=32, shuffle=True)
    
    step("开始训练...")
    for epoch in range(10):
        model.train()
        total_loss = 0
        for data, target in loader:
            optimizer.zero_grad()
            output = model(data)
            loss = criterion(output, target)
            loss.backward()
            optimizer.step()
            total_loss += loss.item()
        
        # 评估
        model.eval()
        with torch.no_grad():
            X_test_t = torch.FloatTensor(X_test)
            outputs = model(X_test_t)
            _, predicted = outputs.max(1)
            acc = (predicted.numpy() == y_test).mean()
        
        log(f"  Epoch {epoch+1}/10 | 损失: {total_loss/len(loader):.4f} | 准确率: {acc*100:.2f}%")
    
    # 保存
    torch.save(model.state_dict(), "model.pt")
    log("\n✅ 训练完成！模型已保存到 model.pt", C_GREEN)
    
    return 0

if __name__ == "__main__":
    try:
        sys.exit(main())
    except Exception as e:
        err(f"错误: {e}")
        sys.exit(1)
