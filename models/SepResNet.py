import sys
import os

# 添加adalora_lib模块的路径
sys.path.append(os.path.join(os.path.dirname(__file__), 'FedAD'))

import torch
import torch.nn as nn
import torch.nn.functional as F
import adalora_lib as adalora
import math


class ECA(nn.Module):
    """Efficient Channel Attention 模块
    轻量级通道注意力机制，使用一维卷积实现局部跨通道交互
    参考文献: https://arxiv.org/abs/1910.03151
    """
    
    def __init__(self, channels, gamma=2, b=1):
        super(ECA, self).__init__()
        # 计算内核大小，确保是奇数
        t = int(abs((math.log2(channels) + b) / gamma))
        kernel_size = t if t % 2 else t + 1
        
        # 自适应池化和一维卷积
        self.avg_pool = nn.AdaptiveAvgPool2d(1)
        self.conv = nn.Conv1d(1, 1, kernel_size=kernel_size, padding=(kernel_size - 1) // 2, bias=False)
        self.sigmoid = nn.Sigmoid()
        
    def forward(self, x):
        # x: [B, C, H, W]
        b, c, h, w = x.size()
        
        # 全局平均池化: [B, C, H, W] -> [B, C, 1, 1]
        y = self.avg_pool(x)
        
        # 调整形状进行1D卷积: [B, C, 1, 1] -> [B, 1, C]
        y = y.view(b, 1, c)
        
        # 应用1D卷积: [B, 1, C] -> [B, 1, C]
        y = self.conv(y)
        
        # 调整回原形状: [B, 1, C] -> [B, C, 1, 1]
        y = y.view(b, c, 1, 1)
        
        # Sigmoid激活获取注意力权重
        y = self.sigmoid(y)
        
        # 应用注意力权重
        return x * y.expand_as(x)


class DepthwiseSeparableConv(nn.Module):
    """使用AdaLoRA的MobileNet深度可分离卷积块（带线性瓶颈）"""

    def __init__(self, in_channels, out_channels, stride=1, r=4, expansion_factor=6):
        super(DepthwiseSeparableConv, self).__init__()
        # 确保r是整数
        r = max(1, int(r))
        
        # 计算扩展后的通道数
        expanded_channels = int(in_channels * expansion_factor)
        
        # 线性瓶颈结构：扩展 -> 深度卷积 -> 压缩
        # 1. 点卷积（升维）
        self.expand = adalora.Conv2d(in_channels, expanded_channels,
                                     kernel_size=1, stride=1, bias=False, r=r)
        self.bn_expand = nn.BatchNorm2d(expanded_channels)
        
        # 2. 深度卷积
        self.depthwise = adalora.Conv2d(expanded_channels, expanded_channels, kernel_size=3,
                                        stride=stride, padding=1, groups=expanded_channels, bias=False, r=r)
        self.bn_depthwise = nn.BatchNorm2d(expanded_channels)
        
        # 3. 点卷积（降维）- 线性瓶颈的关键：最后不使用ReLU
        self.project = adalora.Conv2d(expanded_channels, out_channels,
                                      kernel_size=1, stride=1, bias=False, r=r)
        self.bn_project = nn.BatchNorm2d(out_channels)
        
        # 残差连接（如果输入输出通道数相同且步长为1）
        self.use_residual = (in_channels == out_channels and stride == 1)
        
        # ECA注意力模块
        self.eca = ECA(out_channels)

    def forward(self, x, mode='all'):
        # 保存输入用于残差连接
        residual = x
        
        # 1. 扩展通道数
        x = self.expand(x, mode=mode)
        x = self.bn_expand(x)
        x = F.relu(x)
        
        # 2. 深度卷积处理空间特征
        x = self.depthwise(x, mode=mode)
        x = self.bn_depthwise(x)
        x = F.relu(x)
        
        # 3. 压缩通道数 - 关键：这里不使用ReLU激活，保持线性
        x = self.project(x, mode=mode)
        x = self.bn_project(x)
        
        # 应用ECA注意力
        x = self.eca(x)
            
        # 添加残差连接
        if self.use_residual:
            x += residual
            
        return x


class MobileNetResidualBlock(nn.Module):
    """使用AdaLoRA的ResNet基础块与MobileNet深度可分离卷积"""
    expansion = 1

    def __init__(self, in_channels, out_channels, stride=1, downsample=None, r=4):
        super(MobileNetResidualBlock, self).__init__()
        # 确保r是整数
        r = max(1, int(r))

        self.conv1 = DepthwiseSeparableConv(in_channels, out_channels, stride, r=r)
        self.conv2 = DepthwiseSeparableConv(out_channels, out_channels, r=r)
        self.downsample = downsample
        self.stride = stride
        
        # 全局ECA注意力（在残差连接后）
        self.global_eca = ECA(out_channels)

    def forward(self, x, mode='all'):
        identity = x

        out = self.conv1(x, mode=mode)
        out = self.conv2(out, mode=mode)

        if self.downsample is not None:
            if isinstance(self.downsample, nn.Sequential):
                # 处理序列中的AdaLoRA层
                for layer in self.downsample:
                    if hasattr(layer, 'forward') and len(
                            [p for p in layer.forward.__code__.co_varnames if p == 'mode']) > 0:
                        identity = layer(identity, mode=mode)
                    else:
                        identity = layer(identity)
            else:
                identity = self.downsample(x, mode=mode)

        out = out + identity
        
        # 应用全局ECA注意力
        out = self.global_eca(out)
            
        out = F.relu(out)
        return out


class MobileNetBottleneckBlock(nn.Module):
    """使用AdaLoRA的ResNet瓶颈块与MobileNet深度可分离卷积"""
    expansion = 4

    def __init__(self, in_channels, out_channels, stride=1, downsample=None, r=4):
        super(MobileNetBottleneckBlock, self).__init__()
        # 确保r是整数
        r = max(1, int(r))
        width = out_channels

        self.conv1 = adalora.Conv2d(in_channels, width, kernel_size=1, stride=1, bias=False, r=r)
        self.bn1 = nn.BatchNorm2d(width)

        self.conv2 = DepthwiseSeparableConv(width, width, stride, r=r)

        self.conv3 = adalora.Conv2d(width, out_channels * self.expansion,
                                    kernel_size=1, stride=1, bias=False, r=r)
        self.bn3 = nn.BatchNorm2d(out_channels * self.expansion)

        self.downsample = downsample
        self.stride = stride
        
        # ECA注意力模块（在最后一个卷积层后）
        self.eca = ECA(out_channels * self.expansion)

    def forward(self, x, mode='all'):
        identity = x

        out = self.conv1(x, mode=mode)
        out = self.bn1(out)
        out = F.relu(out)

        out = self.conv2(out, mode=mode)

        out = self.conv3(out, mode=mode)
        out = self.bn3(out)
        
        # 应用ECA注意力
        out = self.eca(out)

        if self.downsample is not None:
            if isinstance(self.downsample, nn.Sequential):
                for layer in self.downsample:
                    if hasattr(layer, 'forward') and len(
                            [p for p in layer.forward.__code__.co_varnames if p == 'mode']) > 0:
                        identity = layer(identity, mode=mode)
                    else:
                        identity = layer(identity)
            else:
                identity = self.downsample(x, mode=mode)

        out = out + identity
        out = F.relu(out)
        return out


class ResNetWithMobileNet(nn.Module):
    def __init__(self, block, layers, num_classes=1000, Conv_r=4, Linear_r=16):
        super(ResNetWithMobileNet, self).__init__()
        self.in_channels = 64
        # 确保参数是整数
        self.Conv_r = max(1, int(Conv_r))
        self.Linear_r = max(1, int(Linear_r))

        self.conv1 = adalora.Conv2d(3, 64, kernel_size=7, stride=2, padding=3, bias=False, r=self.Conv_r)
        self.bn1 = nn.BatchNorm2d(64)
        self.relu = nn.ReLU(inplace=False)
        self.maxpool = nn.MaxPool2d(kernel_size=3, stride=2, padding=1)

        self.layer1 = self._make_layer(block, 64, layers[0])
        self.layer2 = self._make_layer(block, 128, layers[1], stride=2)
        self.layer3 = self._make_layer(block, 256, layers[2], stride=2)
        self.layer4 = self._make_layer(block, 512, layers[3], stride=2)

        self.avgpool = nn.AdaptiveAvgPool2d((1, 1))
        self.fc = adalora.Linear(512 * block.expansion, num_classes, r=self.Linear_r)

    def _make_layer(self, block, out_channels, blocks, stride=1):
        downsample = None
        if stride != 1 or self.in_channels != out_channels * block.expansion:
            downsample = nn.Sequential(
                adalora.Conv2d(self.in_channels, out_channels * block.expansion,
                               kernel_size=1, stride=stride, bias=False, r=self.Conv_r),
                nn.BatchNorm2d(out_channels * block.expansion),
            )

        layers = []
        # 第一个块可能有步长和下采样
        layers.append(block(self.in_channels, out_channels, stride, downsample, 
                            r=self.Conv_r))
        self.in_channels = out_channels * block.expansion
        # 后续块
        for _ in range(1, blocks):
            layers.append(block(self.in_channels, out_channels, 
                                r=self.Conv_r))

        # 使用nn.ModuleList而不是nn.Sequential，以便在forward中传递mode参数
        return nn.ModuleList(layers)

    def forward(self, x, mode='all'):
        x = self.conv1(x, mode=mode)
        x = self.bn1(x)
        x = self.relu(x)
        x = self.maxpool(x)

        # 遍历每个layer中的所有模块并传递mode参数
        for layer in self.layer1:
            x = layer(x, mode=mode)
        for layer in self.layer2:
            x = layer(x, mode=mode)
        for layer in self.layer3:
            x = layer(x, mode=mode)
        for layer in self.layer4:
            x = layer(x, mode=mode)

        x = self.avgpool(x)
        x = torch.flatten(x, 1)
        x = self.fc(x, mode=mode)
        return x


def SepResNet(num_classes=1000, Conv_r=4, Linear_r=16, verbose=False):
    """创建使用AdaLoRA和ECA注意力机制的SepResNet模型
    
    Args:
        num_classes (int): 分类数量
        Conv_r (int): 卷积层的LoRA秩
        Linear_r (int): 线性层的LoRA秩
        verbose (bool): 详细输出标志
    
    Returns:
        ResNetWithMobileNet: 使用AdaLoRA和ECA注意力机制的SepResNet模型
    """
    # verbose参数在这里不直接使用，但为了与main.py中的调用兼容而保留
    return ResNetWithMobileNet(MobileNetResidualBlock, [2, 2, 2, 2], 
                              num_classes, Conv_r, Linear_r)