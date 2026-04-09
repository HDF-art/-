#!/usr/bin/env python
# coding: utf-8
"""
测试模型服务直接调用功能
"""

import base64
import sys
import os

# 添加当前目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
os.chdir(os.path.dirname(os.path.abspath(__file__)))

def test_with_sample_image():
    """
    使用示例图片测试直接调用功能
    """
    print("开始测试模型服务直接调用功能...")
    
    # 尝试导入模型服务中的直接调用函数
    try:
        from models.model_service import load_model, direct_predict_from_base64
        print("成功导入模型服务模块")
    except ImportError as e:
        print(f"导入模型服务模块失败: {e}")
        return False
    
    # 尝试加载模型
    try:
        if not load_model():
            print("模型加载失败")
            return False
        print("模型加载成功")
    except Exception as e:
        print(f"加载模型时发生错误: {e}")
        return False
    
    # 创建一个虚拟的测试图像（纯色图像，用于测试）
    try:
        from PIL import Image
        import io
        
        # 创建一个简单的测试图像
        img = Image.new('RGB', (224, 224), color='green')
        buffer = io.BytesIO()
        img.save(buffer, format='JPEG')
        buffer.seek(0)
        image_bytes = buffer.getvalue()
        
        # 将图像转换为base64
        base64_image = base64.b64encode(image_bytes).decode('utf-8')
        
        print("开始执行直接预测...")
        result = direct_predict_from_base64(base64_image)
        print("预测结果:", result)
        print("直接调用测试成功!")
        return True
        
    except Exception as e:
        print(f"执行预测时发生错误: {e}")
        import traceback
        traceback.print_exc()
        return False

def test_command_line_call():
    """
    测试命令行直接调用功能
    """
    import subprocess
    import json
    
    print("\n开始测试命令行直接调用功能...")
    
    try:
        from PIL import Image
        import io
        
        # 创建一个简单的测试图像
        img = Image.new('RGB', (224, 224), color='red')
        buffer = io.BytesIO()
        img.save(buffer, format='JPEG')
        buffer.seek(0)
        image_bytes = buffer.getvalue()
        
        # 将图像转换为base64
        base64_image = base64.b64encode(image_bytes).decode('utf-8')
        
        # 调用命令行版本
        cmd = [sys.executable, "models/model_service.py", "--direct", base64_image]
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=60)
        
        print(f"命令行返回码: {result.returncode}")
        print(f"命令行输出: {result.stdout}")
        if result.stderr:
            print(f"命令行错误: {result.stderr}")
        
        if result.returncode == 0:
            try:
                parsed_result = json.loads(result.stdout)
                print("解析的JSON结果:", parsed_result)
                print("命令行调用测试成功!")
                return True
            except json.JSONDecodeError as e:
                print(f"解析JSON输出失败: {e}")
                return False
        else:
            print("命令行调用失败!")
            return False
            
    except subprocess.TimeoutExpired:
        print("命令行调用超时!")
        return False
    except Exception as e:
        print(f"命令行调用时发生错误: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    print("="*50)
    print("农业大数据联合建模平台 - 模型服务直接调用测试")
    print("="*50)
    
    success1 = test_with_sample_image()
    success2 = test_command_line_call()
    
    print("\n" + "="*50)
    if success1 and success2:
        print("所有测试均通过! 模型服务直接调用功能正常工作。")
    else:
        print("部分测试失败，请检查模型服务配置。")
    print("="*50)