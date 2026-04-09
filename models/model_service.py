import os
import torch
import torch.nn.functional as F
from torchvision import transforms
from PIL import Image
import numpy as np
from flask import Flask, request, jsonify
import logging
import sys
import json
import base64

# 添加当前目录到Python路径
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# 导入SepResNet模型
from SepResNet import SepResNet

# 配置日志
logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
                    handlers=[logging.StreamHandler()])
logger = logging.getLogger('model_service')

# 初始化Flask应用
app = Flask(__name__)

# 配置参数
MODEL_PATH = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'global_model.pth')
PORT = 5000
device = torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')
logger.info(f'Using device: {device}')

# 图像预处理转换
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225]
    )
])

# 病虫害类别(必须与训练时的类别数量和顺序一致)
# 根据模型实际训练的15个类别
CLASSES = [
    'Curculionidae (稻象甲科)',      # 0 - 823 images
    'Delphacidae (飞虱科)',           # 1 - 857 images  
    'Cicadellidae (叶蝉科)',          # 2 - 833 images
    'Phlaeothripidae (管蓟马科)',     # 3 - 384 images
    'Cecidomyiidae (瘿蚊科)',         # 4 - 759 images
    'Hesperiidae (弄蝶科)',           # 5 - 820 images
    'Crambidae (草螟科)',             # 6 - 620 images
    'Chloropidae (秆蝇科)',           # 7 - 212 images
    'Ephydridae (水蝇科)',            # 8 - 203 images
    'Noctuidae (夜蛾科)',             # 9 - 363 images
    'Thripidae (蓟马科)',             # 10 - 433 images
    'Bacterialblight (细菌性白叶枯病)', # 11 - 1584 images
    'Blast (稻瘟病)',                 # 12 - 1440 images
    'Brownspot (褐斑病)',             # 13 - 1600 images
    'Tungro (钨谷病毒病)'              # 14 - 1308 images
]

# 加载模型
model = None

def load_model():
    global model
    try:
        logger.info(f'Loading model from {MODEL_PATH}')
        
        # 从checkpoint推断模型参数
        # 根据错误信息,checkpoint的参数是:
        # - Conv_r = 8 (从lora_E形状推断)
        # - Linear_r = 12 (从fc.lora_E形状推断)
        # - num_classes = 15 (从fc.weight形状推断)
        
        # 初始化SepResNet模型 - 使用与训练时相同的参数
        model = SepResNet(num_classes=len(CLASSES), Conv_r=8, Linear_r=12)
        
        # 加载模型权重
        if os.path.exists(MODEL_PATH):
            checkpoint = torch.load(MODEL_PATH, map_location=device)
            
            # 处理不同的权重保存格式
            if isinstance(checkpoint, dict):
                # 如果是字典格式,可能包含'model_state_dict'或'state_dict'等键
                if 'model_state_dict' in checkpoint:
                    state_dict = checkpoint['model_state_dict']
                    logger.info("使用键: 'model_state_dict'")
                elif 'state_dict' in checkpoint:
                    state_dict = checkpoint['state_dict']
                    logger.info("使用键: 'state_dict'")
                else:
                    # 直接作为state_dict使用
                    state_dict = checkpoint
                    logger.info("直接使用checkpoint作为state_dict")
            else:
                state_dict = checkpoint
                logger.info(f"Checkpoint类型: {type(checkpoint)}")
            
            # 加载权重 - 现在参数应该完全匹配
            missing_keys, unexpected_keys = model.load_state_dict(state_dict, strict=True)
            
            if missing_keys:
                logger.warning(f'Missing keys when loading model ({len(missing_keys)}): {missing_keys[:5]}...')
            if unexpected_keys:
                logger.warning(f'Unexpected keys when loading model ({len(unexpected_keys)}): {unexpected_keys[:5]}...')
            
            logger.info('Model weights loaded successfully')
        else:
            logger.warning(f'Model file not found at {MODEL_PATH}, using initialized weights')
        
        # 移动到设备并设置为评估模式
        model.to(device)
        model.eval()
        logger.info('Model ready for inference')
        logger.info(f'Model configuration: num_classes={len(CLASSES)}, Conv_r=8, Linear_r=12')
        return True
    except Exception as e:
        logger.error(f'Error loading model: {str(e)}')
        import traceback
        logger.error(traceback.format_exc())
        return False

# 图像识别函数
def predict(image_path):
    try:
        # 加载和预处理图像
        image = Image.open(image_path).convert('RGB')
        image = transform(image).unsqueeze(0).to(device)
        
        # 模型推理 - SepResNet的forward需要mode参数
        with torch.no_grad():
            # 推理时使用mode='lora'或'all'来激活LoRA层
            # 'all'表示使用完整模型(原始权重+LoRA权重)
            outputs = model(image, mode='all')
            probabilities = F.softmax(outputs, dim=1)
            max_prob, predicted_class = torch.max(probabilities, 1)
            
            # 获取所有类别的概率
            all_probs = probabilities.squeeze().cpu().numpy()
            
        # 构建结果
        result = {
            'diseaseName': CLASSES[predicted_class.item()],
            'confidence': max_prob.item(),
            'details': [
                {'diseaseName': CLASSES[i], 'confidence': float(prob)}
                for i, prob in enumerate(all_probs)
            ]
        }
        
        logger.info(f'Prediction: {CLASSES[predicted_class.item()]} (confidence: {max_prob.item():.4f})')
        return result
    except Exception as e:
        logger.error(f'Error during prediction: {str(e)}')
        import traceback
        logger.error(traceback.format_exc())
        raise

# REST API端点
@app.route('/predict', methods=['POST'])
def predict_endpoint():
    temp_path = None
    try:
        # 检查文件是否存在
        if 'file' not in request.files:
            logger.error('No file provided in request')
            return jsonify({'error': 'No file provided'}), 400
        
        file = request.files['file']
        
        # 检查文件名
        if file.filename == '':
            logger.error('No file selected')
            return jsonify({'error': 'No file selected'}), 400
        
        # 使用更安全的临时文件路径
        import tempfile
        import uuid
        temp_dir = tempfile.gettempdir()
        temp_filename = f'model_predict_{uuid.uuid4().hex}.jpg'
        temp_path = os.path.join(temp_dir, temp_filename)
        
        # 保存上传的文件 - 使用stream方式确保完整保存
        logger.info(f'Saving uploaded file to: {temp_path}')
        try:
            # 读取文件内容
            file_content = file.read()
            logger.info(f'Read {len(file_content)} bytes from uploaded file')
            
            # 写入临时文件
            with open(temp_path, 'wb') as f:
                f.write(file_content)
            
            # 验证文件已保存且可读
            if not os.path.exists(temp_path):
                logger.error(f'File not saved: {temp_path}')
                return jsonify({'error': 'Failed to save file'}), 500
            
            file_size = os.path.getsize(temp_path)
            logger.info(f'File saved successfully, size: {file_size} bytes')
            
            if file_size == 0:
                logger.error('Saved file is empty (0 bytes)')
                return jsonify({'error': 'Uploaded file is empty'}), 400
                
        except Exception as e:
            logger.error(f'Error saving file: {str(e)}')
            import traceback
            logger.error(traceback.format_exc())
            return jsonify({'error': f'Failed to save file: {str(e)}'}), 500
        
        # 进行预测
        result = predict(temp_path)
        
        return jsonify(result)
        
    except Exception as e:
        logger.error(f'Error in predict endpoint: {str(e)}')
        import traceback
        logger.error(traceback.format_exc())
        return jsonify({'error': str(e)}), 500
    finally:
        # 清理临时文件
        if temp_path and os.path.exists(temp_path):
            try:
                os.remove(temp_path)
                logger.info(f'Cleaned up temp file: {temp_path}')
            except Exception as e:
                logger.warning(f'Failed to clean up temp file: {str(e)}')

# 健康检查端点
@app.route('/health', methods=['GET'])
def health_check():
    if model is not None:
        return jsonify({'status': 'healthy', 'message': 'Model service is running'}), 200
    else:
        return jsonify({'status': 'unhealthy', 'message': 'Model not loaded'}), 503

# 直接预测函数，接受base64编码的图像数据
def direct_predict_from_base64(base64_image_data):
    """
    直接从base64编码的图像数据进行预测，不通过HTTP API
    """
    import tempfile
    import uuid
    
    # 解码base64图像数据
    image_bytes = base64.b64decode(base64_image_data)
    
    # 创建临时文件
    temp_dir = tempfile.gettempdir()
    temp_filename = f'direct_predict_{uuid.uuid4().hex}.jpg'
    temp_path = os.path.join(temp_dir, temp_filename)
    
    try:
        # 保存图像到临时文件
        with open(temp_path, 'wb') as f:
            f.write(image_bytes)
        
        # 调用预测函数
        result = predict(temp_path)
        return result
    finally:
        # 清理临时文件
        if os.path.exists(temp_path):
            try:
                os.remove(temp_path)
            except:
                pass

# 命令行直接调用入口
def main_direct_call():
    """
    用于直接调用模型进行预测的入口函数
    从标准输入读取base64编码的图像数据
    """
    import sys
    
    # 从命令行参数或标准输入获取base64编码的图像数据
    if len(sys.argv) > 1:
        base64_image_data = sys.argv[1]
    else:
        # 从标准输入读取
        base64_image_data = sys.stdin.read().strip()
    
    if not base64_image_data:
        print(json.dumps({"error": "No image data provided"}))
        return
    
    try:
        # 执行预测
        result = direct_predict_from_base64(base64_image_data)
        # 输出JSON结果到标准输出
        print(json.dumps(result))
    except Exception as e:
        print(json.dumps({"error": str(e)}))

# 直接预测函数，接受图像文件路径

def main_predict_from_file():
    """
    用于直接对指定路径的图像文件进行预测的入口函数
    """
    import sys
    import traceback
    
    try:
        if len(sys.argv) < 3:
            print(json.dumps({"error": "No image file path provided"}))
            return
        
        image_path = sys.argv[2]
        
        if not os.path.exists(image_path):
            print(json.dumps({"error": f"Image file not found: {image_path}"}))
            return
        
        # 检查依赖项
        try:
            import torch
        except ImportError as e:
            print(json.dumps({"error": f"缺少依赖项: {str(e)}"}))
            return
        
        try:
            import torchvision
        except ImportError as e:
            print(json.dumps({"error": f"缺少依赖项: {str(e)}"}))
            return
        
        try:
            from PIL import Image
        except ImportError as e:
            print(json.dumps({"error": f"缺少依赖项: {str(e)}"}))
            return
        
        # 检查模型权重文件
        if not os.path.exists(MODEL_PATH):
            print(json.dumps({"error": f"模型权重文件不存在: {MODEL_PATH}"}))
            return
        
        # 执行预测
        result = predict(image_path)
        # 只输出JSON结果到标准输出，不输出任何其他信息
        print(json.dumps(result))
    except Exception as e:
        # 只输出JSON格式的错误信息，不输出任何其他信息
        print(json.dumps({"error": str(e)}))

# 启动服务
if __name__ == '__main__':
    if load_model():
        # 检查是否有命令行参数，如果有则执行直接调用模式
        import sys
        if len(sys.argv) > 1:
            if sys.argv[1] == '--direct':
                main_direct_call()
            elif sys.argv[1] == '--predict':
                main_predict_from_file()
        else:
            logger.info(f'Starting model service on port {PORT}')
            app.run(host='0.0.0.0', port=PORT, debug=False)
    else:
        logger.error('Failed to start model service: Model loading failed')
