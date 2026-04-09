"""
FLGo 配置脚本生成器
客户端下载此脚本后自动配置环境并加入训练
"""
import os
import json

class ScriptGenerator:
    """生成客户端配置脚本"""
    
    def __init__(self, task_dir="/tmp/flgo_tasks"):
        self.task_dir = task_dir
        os.makedirs(task_dir, exist_ok=True)
    
    def generate_client_script(self, task_id: str, server_ip: str, 
                                participant_ips: list, algorithm: str = "fedavg",
                                num_rounds: int = 10, num_epochs: int = 1,
                                learning_rate: float = 0.01,
                                server_port: int = 9999) -> str:
        """生成客户端启动脚本"""
        
        script = f'''#!/usr/bin/env python3
"""
FLGo 联邦学习客户端配置脚本
任务ID: {task_id}
服务器IP: {server_ip}
生成时间: {self.get_timestamp()}
"""

import os
import sys
import json
import subprocess
import socket
import time

# ==================== 配置参数 ====================
TASK_ID = "{task_id}"
SERVER_IP = "{server_ip}"
SERVER_PORT = {server_port}
PARTICIPANT_IPS = {json.dumps(participant_ips)}
ALGORITHM = "{algorithm}"
NUM_ROUNDS = {num_rounds}
NUM_EPOCHS = {num_epochs}
LEARNING_RATE = {learning_rate}
CLIENT_NAME = socket.gethostname()
CLIENT_IP = self.get_local_ip()

print("=" * 50)
print("FLGo 客户端配置脚本")
print("=" * 50)
print(f"任务ID: {{TASK_ID}}")
print(f"服务器IP: {{SERVER_IP}}")
print(f"本机IP: {{CLIENT_IP}}")
print(f"本机名称: {{CLIENT_NAME}}")
print("=" * 50)

# ==================== 环境检查 ====================
def check_environment():
    """检查运行环境"""
    print("\\n[1/4] 检查环境...")
    
    # 检查Python版本
    if sys.version_info < (3, 7):
        print("错误: 需要Python 3.7+")
        return False
    
    # 检查并安装依赖
    required_packages = [
        "flgo",
        "torch",
        "numpy",
        " torchvision"
    ]
    
    for package in required_packages:
        try:
            __import__(package.replace("-", "_").split("==")[0])
            print(f"  ✓ {{package}} 已安装")
        except ImportError:
            print(f"  安装 {{package}}...")
            subprocess.run([sys.executable, "-m", "pip", "install", package, "-q"])
    
    print("  ✓ 环境检查完成")
    return True

# ==================== 本地IP获取 ====================
def get_local_ip():
    """获取本机IP地址"""
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect((SERVER_IP, 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except:
        return "127.0.0.1"

# ==================== 任务配置 ====================
def create_task_config():
    """创建任务配置"""
    print("\\n[2/4] 创建任务配置...")
    
    config = {{
        "task_id": TASK_ID,
        "server_ip": SERVER_IP,
        "server_port": SERVER_PORT,
        "algorithm": ALGORITHM,
        "num_rounds": NUM_ROUNDS,
        "num_epochs": NUM_EPOCHS,
        "learning_rate": LEARNING_RATE,
        "participants": PARTICIPANT_IPS,
        "client_ip": CLIENT_IP,
        "client_name": CLIENT_NAME,
    }}
    
    config_path = f"/tmp/flgo_client_{{CLIENT_IP.replace('.', '_')}}_{{TASK_ID}}.json"
    with open(config_path, 'w') as f:
        json.dump(config, f, indent=2)
    
    print(f"  配置文件: {{config_path}}")
    return config_path

# ==================== 注册到平台 ====================
def register_to_platform():
    """向平台注册"""
    print("\\n[3/4] 向平台注册...")
    
    # 这里调用平台API通知已加入
    # platform_url = "http://your-platform-server/api/flgo/participant/register"
    # 实际部署时替换为真实的平台地址
    
    print(f"  本机信息:")
    print(f"    IP: {{CLIENT_IP}}")
    print(f"    名称: {{CLIENT_NAME}}")
    print(f"    任务: {{TASK_ID}}")
    print("  ✓ 注册信息已记录")
    return True

# ==================== 启动训练 ====================
def start_training():
    """启动本地训练"""
    print("\\n[4/4] 启动训练...")
    print(f"  算法: {{ALGORITHM}}")
    print(f"  轮次: {{NUM_ROUNDS}}")
    print(f"  本地轮次: {{NUM_EPOCHS}}")
    print(f"  学习率: {{LEARNING_RATE}}")
    
    # 生成FLGo训练代码
    training_code = f'''
import flgo
import flgo.benchmark.mnist_classification as mnist
import flgo.benchmark.partition as fbp
import flgo.algorithm.{ALGORITHM} as fedalg
import torch
import json
import os

# 加载配置
config_path = "/tmp/flgo_client_{{CLIENT_IP.replace('.', '_')}}_{{TASK_ID}}.json"
with open(config_path, 'r') as f:
    config = json.load(f)

# 创建任务目录
task_dir = f"/tmp/flgo_task_{{CLIENT_IP.replace('.', '_')}}"
os.makedirs(task_dir, exist_ok=True)

# 生成联邦数据集
flgo.gen_task_by_(
    mnist,
    fbp.IIDPartitioner(num_clients=len(config["participants"])),
    task_dir
)

# 配置
fedavg_config = {{
    'gpu': [0] if os.path.exists('/dev/nvidia0') else [],
    'num_rounds': config["num_rounds"],
    'num_epochs': config["num_epochs"],
    'learning_rate': config["learning_rate"],
    'server_ip': config["server_ip"],
    'server_port': config["server_port"],
    'participants': config["participants"],
}}

# 初始化
runner = flgo.init(task_dir, fedalg, fedavg_config)

# 开始训练
print("开始联邦学习训练...")
runner.run()
print("训练完成!")
'''
    
    # 保存训练脚本
    script_path = f"/tmp/flgo_train_{{CLIENT_IP.replace('.', '_')}}_{{TASK_ID}}.py"
    with open(script_path, 'w') as f:
        f.write(training_code)
    
    print(f"  训练脚本: {{script_path}}")
    print("\\n" + "=" * 50)
    print("配置完成！")
    print("运行训练: python {{script_path}}")
    print("=" * 50)
    
    return script_path

# ==================== 主程序 ====================
if __name__ == "__main__":
    try:
        # 步骤1: 检查环境
        if not check_environment():
            sys.exit(1)
        
        # 步骤2: 创建配置
        create_task_config()
        
        # 步骤3: 注册
        register_to_platform()
        
        # 步骤4: 启动训练
        script_path = start_training()
        
        # 自动启动（可选）
        # print("\\n自动启动训练...")
        # subprocess.run([sys.executable, script_path])
        
    except KeyboardInterrupt:
        print("\\n用户取消")
    except Exception as e:
        print(f"\\n错误: {{e}}")
        import traceback
        traceback.print_exc()
'''
        
        return script
    
    def get_timestamp(self):
        from datetime import datetime
        return datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    
    def save_script(self, task_id: str, script: str) -> str:
        """保存脚本到文件"""
        script_path = f"{self.task_dir}/{task_id}/client_script.sh"
        os.makedirs(os.path.dirname(script_path), exist_ok=True)
        
        with open(script_path, 'w') as f:
            f.write(script)
        
        os.chmod(script_path, 0o755)
        return script_path

# 创建全局实例
script_generator = ScriptGenerator()
