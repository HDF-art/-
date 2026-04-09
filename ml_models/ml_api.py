#!/usr/bin/env python3
"""
机器学习API服务
为农业大数据平台提供图像分类和目标检测服务
"""

from flask import Flask, request, jsonify, Response
from flask_cors import CORS
import base64
import json
import os

app = Flask(__name__)
CORS(app)

# 导入分类器和检测器
from image_classifier import ImageClassifier
from object_detector import ObjectDetector

# 初始化模型
classifier = ImageClassifier('mobilenetv2_100')
detector = ObjectDetector('yolov8n')

@app.route('/health', methods=['GET'])
def health():
    return jsonify({
        'status': 'ok',
        'service': 'ML API',
        'models': {
            'classifier': classifier.model_name if classifier else 'fallback',
            'detector': detector.model_type if detector else 'fallback'
        }
    })

@app.route('/api/classify', methods=['POST'])
def classify():
    """图像分类"""
    if 'image' not in request.files:
        return jsonify({'error': 'No image provided'}), 400
    
    image_file = request.files['image']
    image_bytes = image_file.read()
    
    result = classifier.classify(image_bytes)
    return jsonify(result)

@app.route('/api/detect', methods=['POST'])
def detect():
    """目标检测"""
    if 'image' not in request.files:
        return jsonify({'error': 'No image provided'}), 400
    
    image_file = request.files['image']
    image_bytes = image_file.read()
    
    conf = float(request.form.get('conf', 0.25))
    result = detector.detect(image_bytes, conf)
    return jsonify(result)

@app.route('/api/models', methods=['GET'])
def models():
    """获取可用模型列表"""
    return jsonify({
        'classifier_models': [
            {'name': 'mobilenetv2_100', 'type': 'classification'},
            {'name': 'resnet50', 'type': 'classification'},
            {'name': 'efficientnet_b0', 'type': 'classification'},
        ],
        'detector_models': [
            {'name': 'yolov8n', 'type': 'detection'},
            {'name': 'yolov8s', 'type': 'detection'},
            {'name': 'yolov8m', 'type': 'detection'},
        ]
    })

@app.route('/api/crop-disease', methods=['POST'])
def crop_disease():
    """农作物病害检测"""
    if 'image' not in request.files:
        return jsonify({'error': 'No image provided'}), 400
    
    image_file = request.files['image']
    image_bytes = image_file.read()
    
    # 使用分类器
    result = classifier.classify(image_bytes)
    
    # 添加农业相关的类别过滤
    if result.get('success'):
        # 模拟农业病害检测结果
        result['disease_type'] = 'crop_health'
        result['treatment'] = '建议：定期喷洒农药，保持通风'
    
    return jsonify(result)

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    print(f"Starting ML API on port {port}...")
    app.run(host='0.0.0.0', port=port, debug=False)
