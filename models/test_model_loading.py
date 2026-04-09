"""
测试SepResNet模型加载和推理
"""
import os
import torch
import sys
from torchvision import transforms
from PIL import Image
import numpy as np

# 添加当前目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from SepResNet import SepResNet

def test_model_loading():
    """测试模型加载"""
    print("=" * 60)
    print("测试1: 模型初始化")
    print("=" * 60)
    
    # 配置参数
    num_classes = 10
    device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')
    print(f"使用设备: {device}")
    
    # 初始化模型
    try:
        model = SepResNet(num_classes=num_classes, Conv_r=4, Linear_r=16)
        print("✓ 模型初始化成功")
        
        # 统计参数数量
        total_params = sum(p.numel() for p in model.parameters())
        trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
        print(f"总参数量: {total_params:,}")
        print(f"可训练参数量: {trainable_params:,}")
        
        # 移动到设备
        model.to(device)
        model.eval()
        print("✓ 模型已移动到设备并设置为评估模式")
        
    except Exception as e:
        print(f"✗ 模型初始化失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False
    
    return model, device

def test_model_inference(model, device):
    """测试模型推理"""
    print("\n" + "=" * 60)
    print("测试2: 模型推理")
    print("=" * 60)
    
    # 创建随机输入
    try:
        batch_size = 2
        dummy_input = torch.randn(batch_size, 3, 224, 224).to(device)
        print(f"输入张量形状: {dummy_input.shape}")
        
        # 测试不同的mode参数
        modes = ['all', 'sigma', 'tau']
        
        for mode in modes:
            print(f"\n测试mode='{mode}':")
            try:
                with torch.no_grad():
                    output = model(dummy_input, mode=mode)
                print(f"  输出形状: {output.shape}")
                print(f"  输出范围: [{output.min().item():.4f}, {output.max().item():.4f}]")
                
                # 检查是否有NaN或Inf
                if torch.isnan(output).any():
                    print(f"  ✗ 警告: 输出包含NaN值")
                elif torch.isinf(output).any():
                    print(f"  ✗ 警告: 输出包含Inf值")
                else:
                    print(f"  ✓ 推理成功,无异常值")
                    
            except Exception as e:
                print(f"  ✗ 推理失败: {str(e)}")
                import traceback
                traceback.print_exc()
                return False
        
        return True
        
    except Exception as e:
        print(f"✗ 推理测试失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def test_weight_loading(model, device):
    """测试权重加载"""
    print("\n" + "=" * 60)
    print("测试3: 权重加载")
    print("=" * 60)
    
    model_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'global_model.pth')
    
    if not os.path.exists(model_path):
        print(f"✗ 模型文件不存在: {model_path}")
        print("  跳过权重加载测试")
        return True
    
    try:
        print(f"加载模型权重: {model_path}")
        print(f"文件大小: {os.path.getsize(model_path) / 1024 / 1024:.2f} MB")
        
        # 加载checkpoint
        checkpoint = torch.load(model_path, map_location=device)
        
        # 处理不同的权重保存格式
        if isinstance(checkpoint, dict):
            print(f"Checkpoint类型: dict")
            print(f"Checkpoint键: {list(checkpoint.keys())}")
            
            if 'model_state_dict' in checkpoint:
                state_dict = checkpoint['model_state_dict']
                print("使用键: 'model_state_dict'")
            elif 'state_dict' in checkpoint:
                state_dict = checkpoint['state_dict']
                print("使用键: 'state_dict'")
            else:
                state_dict = checkpoint
                print("直接使用checkpoint作为state_dict")
        else:
            state_dict = checkpoint
            print(f"Checkpoint类型: {type(checkpoint)}")
        
        # 加载权重
        missing_keys, unexpected_keys = model.load_state_dict(state_dict, strict=False)
        
        if missing_keys:
            print(f"\n缺失的键 ({len(missing_keys)}个):")
            for key in missing_keys[:5]:  # 只显示前5个
                print(f"  - {key}")
            if len(missing_keys) > 5:
                print(f"  ... 还有 {len(missing_keys) - 5} 个")
        
        if unexpected_keys:
            print(f"\n意外的键 ({len(unexpected_keys)}个):")
            for key in unexpected_keys[:5]:  # 只显示前5个
                print(f"  - {key}")
            if len(unexpected_keys) > 5:
                print(f"  ... 还有 {len(unexpected_keys) - 5} 个")
        
        print("\n✓ 权重加载完成")
        
        # 再次测试推理
        print("\n测试加载权重后的推理:")
        dummy_input = torch.randn(1, 3, 224, 224).to(device)
        
        with torch.no_grad():
            output = model(dummy_input, mode='all')
        
        print(f"  输出形状: {output.shape}")
        print(f"  输出范围: [{output.min().item():.4f}, {output.max().item():.4f}]")
        
        # 应用softmax
        probabilities = torch.nn.functional.softmax(output, dim=1)
        max_prob, predicted_class = torch.max(probabilities, 1)
        
        print(f"  预测类别: {predicted_class.item()}")
        print(f"  最大概率: {max_prob.item():.4f}")
        print("  ✓ 推理成功")
        
        return True
        
    except Exception as e:
        print(f"✗ 权重加载失败: {str(e)}")
        import traceback
        traceback.print_exc()
        return False

def main():
    """主测试函数"""
    print("\n" + "=" * 60)
    print("SepResNet模型加载和推理测试")
    print("=" * 60 + "\n")
    
    # 测试1: 模型初始化
    result = test_model_loading()
    if not result:
        print("\n测试失败: 模型初始化")
        return
    
    model, device = result
    
    # 测试2: 模型推理
    if not test_model_inference(model, device):
        print("\n测试失败: 模型推理")
        return
    
    # 测试3: 权重加载
    if not test_weight_loading(model, device):
        print("\n测试失败: 权重加载")
        return
    
    print("\n" + "=" * 60)
    print("所有测试通过! ✓")
    print("=" * 60)

if __name__ == '__main__':
    main()
