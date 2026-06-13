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
        disease_name = top.get('class', '未知')
        conf = top.get('confidence', 0.5)
        
        disease_map = {
            'rice': '水稻健康', 'wheat': '小麦健康', 'corn': '玉米健康',
            'barley': '大麦健康', 'oat': '燕麦健康',
            'blight': '水稻稻瘟病', 'blast': '水稻纹枯病',
            'rust': '小麦锈病', 'powdery_mildew': '白粉病',
            'leaf_spot': '叶斑病', 'bacterial_leaf_blight': '细菌性叶枯病',
            'brown_spot': '褐斑病', 'sheath_blight': '纹枯病',
        }
        
        display_name = disease_map.get(disease_name.lower(), disease_name)
        prevention_map = {
            '水稻稻瘟病': '建议：选用抗病品种，合理施肥，发病初期喷施三环唑或稻瘟灵',
            '水稻纹枯病': '建议：合理密植，适时晒田，发病初期喷施井冈霉素',
            '小麦锈病': '建议：选用抗锈品种，适时播种，发病初期喷施粉锈宁',
            '白粉病': '建议：增施磷钾肥，改善通风条件，喷施三唑酮',
            '叶斑病': '建议：清除病残体，合理轮作，喷施多菌灵',
            '细菌性叶枯病': '建议：选用无病种子，及时排水晒田，喷施农用链霉素',
            '褐斑病': '建议：平衡施肥，避免偏施氮肥，喷施稻瘟灵',
            '纹枯病': '建议：合理密植，适时晒田，喷施井冈霉素',
        }
        prevention = prevention_map.get(display_name, '建议：定期巡查，保持通风，合理施肥浇水')
        
        return jsonify({
            'diseaseName': display_name,
            'confidence': conf,
            'prevention': prevention,
            'success': True,
            'model': result.get('model', 'unknown')
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
