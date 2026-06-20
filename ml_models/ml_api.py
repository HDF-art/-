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
import io
from PIL import Image

app = Flask(__name__)
CORS(app)

MAX_FILE_SIZE = 10 * 1024 * 1024
ALLOWED_IMAGE_TYPES = {'image/jpeg', 'image/png', 'image/gif', 'image/webp'}
ALLOWED_EXTENSIONS = {'.jpg', '.jpeg', '.png', '.gif', '.webp'}

def validate_image_file(image_file):
    if not image_file:
        return False, "未提供图像文件"
    
    image_file.seek(0, 2)
    file_size = image_file.tell()
    image_file.seek(0)
    
    if file_size > MAX_FILE_SIZE:
        return False, f"文件大小超过限制 (最大 {MAX_FILE_SIZE // (1024*1024)}MB)"
    
    content_type = image_file.content_type
    if content_type not in ALLOWED_IMAGE_TYPES:
        return False, f"不支持的文件类型: {content_type}"
    
    try:
        image_bytes = image_file.read()
        image_file.seek(0)
        
        img = Image.open(io.BytesIO(image_bytes))
        img.verify()
        
        image_file.seek(0)
        return True, None
    except Exception as e:
        return False, f"无效的图像文件: {str(e)}"

def validate_base64_image(base64_data):
    try:
        if len(base64_data) > MAX_FILE_SIZE * 1.5:
            return False, "Base64数据过大"
        
        image_bytes = base64.b64decode(base64_data)
        if len(image_bytes) > MAX_FILE_SIZE:
            return False, f"解码后文件大小超过限制"
        
        img = Image.open(io.BytesIO(image_bytes))
        img.verify()
        
        return True, None
    except Exception as e:
        return False, f"无效的Base64图像数据: {str(e)}"

from image_classifier import ImageClassifier
from object_detector import ObjectDetector

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
    if 'image' not in request.files:
        return jsonify({'error': '未提供图像文件'}), 400
    
    image_file = request.files['image']
    
    is_valid, error_msg = validate_image_file(image_file)
    if not is_valid:
        return jsonify({'error': error_msg}), 400
    
    image_bytes = image_file.read()
    result = classifier.classify(image_bytes)
    return jsonify(result)

@app.route('/api/detect', methods=['POST'])
def detect():
    if 'image' not in request.files:
        return jsonify({'error': '未提供图像文件'}), 400
    
    image_file = request.files['image']
    
    is_valid, error_msg = validate_image_file(image_file)
    if not is_valid:
        return jsonify({'error': error_msg}), 400
    
    image_bytes = image_file.read()
    
    try:
        conf = float(request.form.get('conf', 0.25))
        conf = max(0.01, min(1.0, conf))
    except ValueError:
        conf = 0.25
    
    result = detector.detect(image_bytes, conf)
    return jsonify(result)

@app.route('/api/classify/base64', methods=['POST'])
def classify_base64():
    data = request.get_json()
    if not data or 'image' not in data:
        return jsonify({'error': '未提供图像数据'}), 400
    
    base64_data = data['image']
    if base64_data.startswith('data:image'):
        base64_data = base64_data.split(',', 1)[1]
    
    is_valid, error_msg = validate_base64_image(base64_data)
    if not is_valid:
        return jsonify({'error': error_msg}), 400
    
    image_bytes = base64.b64decode(base64_data)
    result = classifier.classify(image_bytes)
    return jsonify(result)

@app.route('/api/detect/base64', methods=['POST'])
def detect_base64():
    data = request.get_json()
    if not data or 'image' not in data:
        return jsonify({'error': '未提供图像数据'}), 400
    
    base64_data = data['image']
    if base64_data.startswith('data:image'):
        base64_data = base64_data.split(',', 1)[1]
    
    is_valid, error_msg = validate_base64_image(base64_data)
    if not is_valid:
        return jsonify({'error': error_msg}), 400
    
    image_bytes = base64.b64decode(base64_data)
    
    try:
        conf = float(data.get('conf', 0.25))
        conf = max(0.01, min(1.0, conf))
    except ValueError:
        conf = 0.25
    
    result = detector.detect(image_bytes, conf)
    return jsonify(result)

@app.route('/api/models', methods=['GET'])
def models():
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

@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': '未提供图像文件'}), 400
    
    image_file = request.files['file']
    
    is_valid, error_msg = validate_image_file(image_file)
    if not is_valid:
        return jsonify({'error': error_msg}), 400
    
    image_bytes = image_file.read()
    result = classifier.classify(image_bytes)
    
    if result.get('success') and result.get('predictions'):
        top = result['predictions'][0]
        raw_class = top.get('class', '未知')
        conf = top.get('confidence', 0.5)
        
        # ImageNet常见类名中文翻译（覆盖农业相关和常见类别）
        imagenet_cn = {
            # 农作物
            'rice': '水稻', 'corn': '玉米', 'wheat': '小麦', 'barley': '大麦',
            'oat': '燕麦', 'sorghum': '高粱', 'soybean': '大豆',
            # 常见植物/花卉
            'daisy': '雏菊', 'rose': '玫瑰', 'sunflower': '向日葵',
            'dandelion': '蒲公英', 'tulip': '郁金香', 'lotus': '莲花',
            'banana': '香蕉', 'orange': '橙子', 'lemon': '柠檬',
            'apple': '苹果', 'pineapple': '菠萝', 'strawberry': '草莓',
            'fig': '无花果', 'pomegranate': '石榴', 'coconut': '椰子',
            # 昆虫（农业害虫）
            'damselfly': '豆娘（益虫）', 'dragonfly': '蜻蜓（益虫）',
            'bee': '蜜蜂（益虫）', 'ant': '蚂蚁', 'grasshopper': '蝗虫（害虫）',
            'beetle': '甲虫', 'ladybug': '瓢虫', 'butterfly': '蝴蝶',
            'moth': '飞蛾', 'cockroach': '蟑螂', 'mantis': '螳螂',
            'cicada': '蝉', 'fly': '苍蝇', 'mosquito': '蚊子',
            'aphid': '蚜虫（害虫）', 'weevil': '象甲（害虫）',
            'caterpillar': '毛虫（害虫）', 'worm': '蠕虫',
            # 叶片/植物部位
            'leaf': '叶片', 'leafhopper': '叶蝉（害虫）',
            # 其他常见
            'spider': '蜘蛛', 'snail': '蜗牛', 'slug': '蛞蝓',
            'frog': '青蛙', 'toad': '蟾蜍', 'lizard': '蜥蜴',
            'bird': '鸟类', 'hen': '母鸡', 'rooster': '公鸡',
            'dog': '犬', 'cat': '猫',
        }
        
        display_name = imagenet_cn.get(raw_class.lower(), raw_class)
        
        # 根据置信度判断结果可信度
        if conf < 0.1:
            display_name = '无法识别（图像可能不清晰或非农业相关）'
            prevention = '建议：请拍摄清晰的农作物或病虫害图片重新识别'
        elif conf < 0.3:
            prevention = '注意：识别置信度较低，结果仅供参考。建议拍摄更清晰的特写图片'
        else:
            prevention = '建议：定期巡查田间，保持通风，合理施肥浇水。如需精准诊断，请咨询当地农技部门'
        
        # 构建Top-5详细信息
        details = []
        for pred in result['predictions'][:5]:
            pred_class = pred.get('class', '未知')
            pred_cn = imagenet_cn.get(pred_class.lower(), pred_class)
            details.append({
                'diseaseName': pred_cn,
                'confidence': round(pred.get('confidence', 0), 4)
            })
        
        return jsonify({
            'diseaseName': display_name,
            'confidence': conf,
            'prevention': prevention,
            'success': True,
            'model': result.get('model', 'unknown'),
            'details': details
        })
    else:
        return jsonify({
            'diseaseName': '识别失败',
            'confidence': 0.0,
            'prevention': '请重新拍摄清晰的图片',
            'success': False
        })

@app.route('/api/crop-disease', methods=['POST'])
def crop_disease():
    if 'image' not in request.files:
        return jsonify({'error': '未提供图像文件'}), 400
    
    image_file = request.files['image']
    
    is_valid, error_msg = validate_image_file(image_file)
    if not is_valid:
        return jsonify({'error': error_msg}), 400
    
    image_bytes = image_file.read()
    result = classifier.classify(image_bytes)
    
    if result.get('success'):
        result['disease_type'] = 'crop_health'
        result['treatment'] = '建议：定期喷洒农药，保持通风'
    
    return jsonify(result)

@app.errorhandler(413)
def request_entity_too_large(error):
    return jsonify({'error': '文件大小超过限制'}), 413

@app.errorhandler(500)
def internal_server_error(error):
    return jsonify({'error': '服务器内部错误'}), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    print(f"Starting ML API on port {port}...")
    app.run(host='0.0.0.0', port=port, debug=False)
