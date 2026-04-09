import request from '../utils/request'

// 图像识别 - 调用真实的后端API
export const identifyImage = (formData) => {
  console.log('调用真实后端API进行图像识别');
  
  // 从localStorage获取用户信息
  const userStr = localStorage.getItem('user');
  let userId = 1; // 默认用户ID
  
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      userId = user.id || user.userId || 1;
    } catch (e) {
      console.warn('解析用户信息失败，使用默认用户ID');
    }
  }
  
  // 构建请求FormData
  const requestData = new FormData();
  requestData.append('file', formData.get('image'));
  requestData.append('userId', userId);
  requestData.append('cropType', '水稻'); // 默认作物类型
  requestData.append('taskType', formData.get('taskType') || 'pest_disease'); // 任务类型
  requestData.append('modelId', formData.get('modelId') || 'default'); // 模型ID
  
  // 调用后端识别接口
  return request({
    url: '/recognition-records/recognize',
    method: 'post',
    data: requestData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 30000 // 30秒超时，因为模型推理可能需要时间
  }).then(response => {
    // 转换后端返回的数据格式为前端期望的格式
    const backendData = response.data || response;
    
    console.log('后端返回的原始数据:', backendData);
    
    // 解析result_json中的details
    let details = [];
    if (backendData.resultJson) {
      try {
        const resultJson = typeof backendData.resultJson === 'string' 
          ? JSON.parse(backendData.resultJson) 
          : backendData.resultJson;
        details = resultJson.details || [];
        console.log('解析出的details:', details);
      } catch (e) {
        console.error('解析resultJson失败:', e);
      }
    }
    
    const formattedData = {
      success: true,
      data: {
        diseaseName: backendData.result || '未知',
        confidence: backendData.confidence || 0,
        details: details,
        imageUrl: backendData.imagePath || URL.createObjectURL(formData.get('image')),
        identifyTime: backendData.createdAt || new Date().toISOString(),
        preventionAdvice: backendData.preventionAdvice || '请咨询专业农业技术人员',
        recordId: backendData.id
      },
      message: '识别成功'
    };
    
    console.log('格式化后的数据:', formattedData);
    return formattedData;
  }).catch(error => {
    console.error('识别API调用失败:', error);
    // 如果后端调用失败，返回友好的错误信息
    throw new Error(error.message || '识别服务暂时不可用，请稍后重试');
  });
}

// 获取识别记录列表
export const getIdentifyRecords = (params) => {
  return request({
    url: `/recognition-records/user/${params.userId}`,
    method: 'get',
    params: {
      page: params.page || 1,
      pageSize: params.pageSize || 10
    }
  })
}

// 获取单个识别记录详情
export const getIdentifyRecordDetail = (recordId) => {
  return request({
    url: `/recognition-records/${recordId}`,
    method: 'get'
  })
}

// 删除识别记录
export const deleteIdentifyRecord = (recordId) => {
  return request({
    url: `/recognition-records/${recordId}`,
    method: 'delete'
  })
}

// 注意：后端暂未提供批量删除、导出和统计功能的直接接口
// 如需这些功能，可能需要在前端实现或等待后端更新
