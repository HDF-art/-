"""
联邦学习客户端 SDK
用于各客户端本地执行训练任务
"""
import requests
import json
import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, TensorDataset
import base64
import hashlib
import random

class FederatedClient:
    def __init__(self, server_url, client_id, data_path=None):
        """
        初始化客户端
        server_url: 服务器地址 (如 https://admp.online)
        client_id: 客户端唯一标识
        data_path: 本地训练数据路径
        """
        self.server_url = server_url.rstrip('/')
        self.client_id = client_id
        self.data_path = data_path
        self.model = None
        self.token = None
        
    def register(self):
        """注册到服务器"""
        response = requests.post(
            f"{self.server_url}/api/federation/client/register",
            json={"clientId": self.client_id}
        )
        return response.json()
    
    def login(self, username, password):
        """登录获取token"""
        response = requests.post(
            f"{self.server_url}/api/users/login",
            json={"username": username, "password": password}
        )
        result = response.json()
        if result.get("code") == 200:
            self.token = result["data"]["token"]
        return result
    
    def get_global_model(self, task_id):
        """获取全局模型参数"""
        headers = {"Authorization": f"Bearer {self.token}"} if self.token else {}
        response = requests.get(
            f"{self.server_url}/api/federation/model/download/{task_id}",
            headers=headers
        )
        if response.status_code == 200:
            return response.json()
        return None
    

    def train_local(self, model_type="CNN", epochs=5, batch_size=32, learning_rate=0.01):
        """
        本地训练模型
        返回: 训练后的模型参数和指标
        """
        print(f"开始本地训练: {model_type}, epochs={epochs}")
        
        # 加载数据
        train_data = self.load_data()
        X_train, y_train = train_data['X'], train_data['y']
        
        # 创建模型
        model = self.create_model(model_type, X_train.shape[1])
        
        # 训练
        optimizer = optim.Adam(model.parameters(), lr=learning_rate)
        criterion = nn.CrossEntropyLoss() if model_type != "LinearRegression" else nn.MSELoss()
        
        dataset = TensorDataset(
            torch.FloatTensor(X_train),
            torch.FloatTensor(y_train)
        )
        loader = DataLoader(dataset, batch_size=batch_size, shuffle=True)
        
        model.train()
        total_loss = 0
        for epoch in range(epochs):
            epoch_loss = 0
            for batch_X, batch_y in loader:
                optimizer.zero_grad()
                output = model(batch_X)
                loss = criterion(output, batch_y.long() if model_type != "LinearRegression" else batch_y)
                loss.backward()
                optimizer.step()
                epoch_loss += loss.item()
            total_loss = epoch_loss / len(loader)
            print(f"  Epoch {epoch+1}/{epochs}, Loss: {total_loss:.4f}")
        
        # 提取参数
        params = self.extract_params(model)
        
        # 计算指标
        metrics = self.evaluate(model, X_train, y_train)
        
        return params, metrics
    
    def create_model(self, model_type, input_dim):
        """创建模型"""
        if model_type == "CNN":
            model = nn.Sequential(
                nn.Conv2d(1, 32, 3, padding=1),
                nn.ReLU(),
                nn.MaxPool2d(2),
                nn.Conv2d(32, 64, 3, padding=1),
                nn.ReLU(),
                nn.MaxPool2d(2),
                nn.Flatten(),
                nn.Linear(64 * 7 * 7, 128),
                nn.ReLU(),
                nn.Linear(128, 10)
            )
        elif model_type == "LSTM":
            model = nn.LSTM(input_dim, 128, 2, batch_first=True)
            model = nn.Linear(128, 10)
        elif model_type == "ResNet":
            model = nn.Sequential(
                nn.Conv2d(3, 64, 7, stride=2, padding=3),
                nn.BatchNorm2d(64),
                nn.ReLU(),
                nn.MaxPool2d(3, stride=2, padding=1),
                nn.Residual(nn.Conv2d(64, 64, 3, padding=1)),
                nn.Residual(nn.Conv2d(64, 128, 3, stride=2, padding=1)),
                nn.AdaptiveAvgPool2d(1),
                nn.Flatten(),
                nn.Linear(128, 10)
            )
        elif model_type == "XGBoost":
            # 简化的树模型
            model = nn.Sequential(
                nn.Linear(input_dim, 64),
                nn.ReLU(),
                nn.Linear(64, 32),
                nn.ReLU(),
                nn.Linear(32, 10)
            )
        elif model_type == "LinearRegression":
            model = nn.Linear(input_dim, 1)
        elif model_type == "LogisticRegression":
            model = nn.Linear(input_dim, 2)
        else:  # Transformer
            model = nn.TransformerEncoder(
                nn.TransformerEncoderLayer(d_model=input_dim, nhead=8),
                num_layers=2
            )
            model = nn.Linear(input_dim, 10)
        
        return model
    
    def load_data(self):
        """加载本地数据"""
        # 模拟数据 - 实际从self.data_path加载
        # 支持: CSV, JSON, NumPy格式
        if self.data_path:
            # TODO: 实现真实数据加载
            pass
        
        # 生成模拟数据用于演示
        np.random.seed(random.randint(0, 1000))
        n_samples = 1000
        X = np.random.randn(n_samples, 784).astype(np.float32)
        y = np.random.randint(0, 10, n_samples)
        
        return {'X': X, 'y': y}
    
    def extract_params(self, model):
        """提取模型参数"""
        params = []
        for param in model.parameters():
            params.append(param.detach().numpy().tolist())
        return params
    
    def evaluate(self, model, X, y):
        """评估模型"""
        model.eval()
        with torch.no_grad():
            X_tensor = torch.FloatTensor(X)
            outputs = model(X_tensor)
            _, predicted = torch.max(outputs, 1)
            accuracy = (predicted.numpy() == y).mean()
        
        return {"accuracy": float(accuracy), "samples": len(y)}
    
    def run_federated_training(self, task_id, model_type="CNN", rounds=10, epochs=5):
        """
        运行联邦学习训练
        """
        print(f"=== 开始联邦学习训练 (Task: {task_id}) ===")
        
        for round_num in range(rounds):
            print(f"\n--- Round {round_num+1}/{rounds} ---")
            
            # 1. 获取全局模型
            global_model = self.get_global_model(task_id)
            if global_model:
                print("  已获取全局模型")
                # TODO: 应用全局模型参数到本地
            
            # 2. 本地训练
            params, metrics = self.train_local(model_type, epochs=epochs)
            print(f"  训练完成: Accuracy={metrics['accuracy']:.4f}")
            
            # 3. 本步骤原本上传更新，已被废除
            print(f"  不再上传模型权重到Java服务端。")
        
        print("\n=== 训练完成 ===")
        return True


# 使用示例
if __name__ == "__main__":
    # 创建客户端
    client = FederatedClient(
        server_url="https://admp.online",
        client_id="client_001",
        data_path=None  # 替换为实际数据路径
    )
    
    # 登录
    client.login("admin", "admin123")
    
    # 运行联邦训练
    client.run_federated_training(
        task_id="task_001",
        model_type="CNN",
        rounds=10,
        epochs=5
    )
