"""
安全的模型加载工具
防止pickle反序列化导致的RCE攻击
"""

import os
import torch
import hashlib
import json
from pathlib import Path

SAFE_MODEL_EXTENSIONS = {'.pt', '.pth', '.onnx', '.h5', '.bin'}
MAX_MODEL_SIZE = 500 * 1024 * 1024  # 500MB

def calculate_file_hash(filepath, algorithm='sha256'):
    """计算文件哈希值"""
    hash_func = hashlib.new(algorithm)
    with open(filepath, 'rb') as f:
        for chunk in iter(lambda: f.read(8192), b''):
            hash_func.update(chunk)
    return hash_func.hexdigest()

def validate_model_file(filepath):
    """验证模型文件安全性"""
    if not os.path.exists(filepath):
        return False, "文件不存在"
    
    file_size = os.path.getsize(filepath)
    if file_size > MAX_MODEL_SIZE:
        return False, f"模型文件过大 (最大 {MAX_MODEL_SIZE // (1024*1024)}MB)"
    
    ext = Path(filepath).suffix.lower()
    if ext not in SAFE_MODEL_EXTENSIONS:
        return False, f"不支持的模型格式: {ext}"
    
    return True, None

def safe_load_torch_model(filepath, map_location='cpu', weights_only=True):
    """
    安全加载PyTorch模型
    
    Args:
        filepath: 模型文件路径
        map_location: 设备映射
        weights_only: 是否只加载权重（防止RCE）
    
    Returns:
        model: 加载的模型
    """
    is_valid, error_msg = validate_model_file(filepath)
    if not is_valid:
        raise ValueError(f"模型验证失败: {error_msg}")
    
    try:
        if weights_only:
            checkpoint = torch.load(
                filepath, 
                map_location=map_location, 
                weights_only=True
            )
        else:
            checkpoint = torch.load(
                filepath, 
                map_location=map_location
            )
        return checkpoint
    except Exception as e:
        raise RuntimeError(f"模型加载失败: {str(e)}")

def safe_load_model_weights(model, filepath, strict=True):
    """
    安全加载模型权重到现有模型
    
    Args:
        model: 目标模型
        filepath: 权重文件路径
        strict: 是否严格匹配
    
    Returns:
        model: 加载权重后的模型
    """
    checkpoint = safe_load_torch_model(filepath, weights_only=True)
    
    if isinstance(checkpoint, dict):
        if 'state_dict' in checkpoint:
            state_dict = checkpoint['state_dict']
        elif 'model_state_dict' in checkpoint:
            state_dict = checkpoint['model_state_dict']
        else:
            state_dict = checkpoint
    else:
        state_dict = checkpoint
    
    model.load_state_dict(state_dict, strict=strict)
    return model

def save_model_safely(model, filepath, metadata=None):
    """
    安全保存模型
    
    Args:
        model: 要保存的模型
        filepath: 保存路径
        metadata: 额外元数据
    """
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    
    save_dict = {
        'state_dict': model.state_dict(),
        'metadata': metadata or {}
    }
    
    torch.save(save_dict, filepath)
    
    file_hash = calculate_file_hash(filepath)
    return {
        'path': filepath,
        'size': os.path.getsize(filepath),
        'hash': file_hash
    }
