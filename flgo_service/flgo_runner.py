"""
FLGo联邦学习服务 - 与平台集成
"""
import flgo
import flgo.benchmark
import flgo.algorithm
import os
import json
import time
import socket
import threading
from typing import List, Dict, Optional

class FederatedTask:
    """联邦学习任务"""
    def __init__(self, task_id: str, name: str, dataset: str, algorithm: str, 
                 num_clients: int, num_rounds: int, num_epochs: int):
        self.task_id = task_id
        self.name = name
        self.dataset = dataset
        self.algorithm = algorithm
        self.num_clients = num_clients
        self.num_rounds = num_rounds
        self.num_epochs = num_epochs
        self.task_dir = f"/tmp/flgo_tasks/{task_id}"
        self.runner = None
        self.status = "PENDING"  # PENDING, RUNNING, COMPLETED, FAILED
        self.results = {}
        self.start_time = None
        self.end_time = None
        
class FLGoService:
    """FLGo联邦学习服务"""
    
    def __init__(self):
        self.tasks: Dict[str, FederatedTask] = {}
        self.participants: Dict[str, List[str]] = {}  # task_id -> [client_ips]
        self.server_ip: Optional[str] = None
        os.makedirs("/tmp/flgo_tasks", exist_ok=True)
        
    def set_server_ip(self, ip: str):
        """设置服务器端IP"""
        self.server_ip = ip
        
    def add_participant(self, task_id: str, client_ip: str):
        """添加参与客户端"""
        if task_id not in self.participants:
            self.participants[task_id] = []
        if client_ip not in self.participants[task_id]:
            self.participants[task_id].append(client_ip)
            
    def get_participants(self, task_id: str) -> List[str]:
        """获取任务的所有参与客户端"""
        return self.participants.get(task_id, [])
    
    def select_server_from_participants(self, task_id: str) -> str:
        """从参与者中选择服务器端"""
        participants = self.get_participants(task_id)
        if not participants:
            raise ValueError("没有参与者")
        # 选择第一个作为服务器端
        return participants[0]
    
    def create_task(self, task_id: str, name: str, dataset: str = "mnist",
                     algorithm: str = "fedavg", num_clients: int = 5,
                     num_rounds: int = 10, num_epochs: int = 1) -> FederatedTask:
        """创建联邦学习任务"""
        task = FederatedTask(task_id, name, dataset, algorithm, 
                           num_clients, num_rounds, num_epochs)
        self.tasks[task_id] = task
        return task
    
    def get_dataset(self, dataset: str):
        """获取数据集"""
        datasets = {
            "mnist": flgo.benchmark.mnist_classification,
            "cifar10": flgo.benchmark.cifar10_classification,
            "fashionmnist": flgo.benchmark.fashionmnist_classification,
        }
        return datasets.get(dataset.lower(), flgo.benchmark.mnist_classification)
    
    def get_algorithm(self, algorithm: str):
        """获取算法"""
        algorithms = {
            "fedavg": flgo.algorithm.fedavg,
            "fedprox": flgo.algorithm.fedprox,
            "fednova": flgo.algorithm.fednova,
            "scaffold": flgo.algorithm.scaffold,
        }
        return algorithms.get(algorithm.lower(), flgo.algorithm.fedavg)
    
    def get_partitioner(self, num_clients: int):
        """获取数据分区器"""
        return flgo.benchmark.partition.IIDPartitioner(num_clients=num_clients)
    
    def run_task_async(self, task_id: str):
        """异步运行任务"""
        task = self.tasks.get(task_id)
        if not task:
            raise ValueError(f"任务 {task_id} 不存在")
            
        task.status = "RUNNING"
        task.start_time = time.time()
        
        def run():
            try:
                # 生成任务
                dataset = self.get_dataset(task.dataset)
                partitioner = self.get_partitioner(task.num_clients)
                
                flgo.gen_task_by_(
                    dataset, 
                    partitioner, 
                    task.task_dir
                )
                
                # 初始化运行器
                algorithm = self.get_algorithm(task.algorithm)
                config = {
                    'gpu': [0] if os.path.exists('/dev/nvidia0') else [],
                    'num_rounds': task.num_rounds,
                    'num_epochs': task.num_epochs,
                    'learning_rate': 0.01,
                }
                
                task.runner = flgo.init(task.task_dir, algorithm, config)
                
                # 运行训练
                task.runner.run()
                
                # 获取结果
                if hasattr(task.runner, 'writer') and task.runner.writer:
                    history = task.runner.writer.history
                    task.results = {
                        'rounds': len(history.get('train_loss', [])) if history else 0,
                        'final_accuracy': history.get('test_accuracy', [0])[-1] if history else 0,
                        'final_loss': history.get('test_loss', [0])[-1] if history else 0,
                    }
                
                task.status = "COMPLETED"
                task.end_time = time.time()
                
            except Exception as e:
                task.status = "FAILED"
                task.results = {'error': str(e)}
                task.end_time = time.time()
                
        thread = threading.Thread(target=run)
        thread.start()
        return task
    
    def get_task_status(self, task_id: str) -> Dict:
        """获取任务状态"""
        task = self.tasks.get(task_id)
        if not task:
            return {"error": "任务不存在"}
            
        return {
            "task_id": task.task_id,
            "name": task.name,
            "status": task.status,
            "algorithm": task.algorithm,
            "dataset": task.dataset,
            "num_clients": task.num_clients,
            "num_rounds": task.num_rounds,
            "results": task.results,
            "start_time": task.start_time,
            "end_time": task.end_time,
            "participants": self.get_participants(task_id)
        }
    
    def list_tasks(self) -> List[Dict]:
        """列出所有任务"""
        return [self.get_task_status(tid) for tid in self.tasks.keys()]

# 全局服务实例
flgo_service = FLGoService()
