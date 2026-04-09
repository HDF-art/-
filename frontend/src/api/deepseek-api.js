/**
 * DeepSeek API 集成模块
 * 用于对病虫害识别结果进行分析和推荐
 */

import { checkNetworkConnection } from './networkUtils';

class DeepSeekAPI {
  constructor() {
    this.baseUrl = 'https://api.deepseek.com/v1/chat/completions';
    this.headers = {
      'Content-Type': 'application/json'
    };
    // 获取环境变量中的API密钥
    this.apiKey = process.env.VUE_APP_DEEPSEEK_API_KEY || '';
  }
  
  /**
 * 分析病虫害识别结果并提供详细信息和防治建议
 * 
 * @param {Object} identifyResult - 识别结果对象
 * @returns {Promise<Object>} - 包含分析结果的对象
 * @throws {Error} - 当API调用失败或参数无效时抛出错误
 */
  async analyzeDisease(identifyResult) {
    console.log('开始分析病虫害:', identifyResult);
    
    // 验证输入参数
    if (!identifyResult) {
      throw new Error('识别结果对象为空');
    }
    
    if (!identifyResult.diseaseName) {
      throw new Error('识别结果中缺少病虫害名称');
    }
    
    // 调用真实的DeepSeek API
    console.log('调用真实的DeepSeek API进行病虫害分析');
    return await this._callApi(identifyResult);
  }
  
  /**
   * 构建分析提示词
   * 
   * @param {Object} identifyResult - 识别结果对象
   * @returns {string} - 完整的提示词文本
   */
  _buildAnalysisPrompt(identifyResult) {
    // 主要病虫害信息
    const mainDisease = identifyResult.diseaseName;
    const confidence = identifyResult.confidence;
    const details = identifyResult.details.map(item => 
      `- ${item.diseaseName} (${(item.confidence * 100).toFixed(2)}%)`
    ).join('\n');
    
    const prompt = `
你是一位专业的农业病虫害专家，请对以下病虫害识别结果进行详细分析：

识别结果：
- 主要病虫害：${mainDisease}
- 识别置信度：${(confidence * 100).toFixed(2)}%
- 多模型识别结果：
${details}

请提供以下信息（请用JSON格式返回）：

1. 基本信息：
   - 病虫害类型（病害/虫害/其他）
   - 主要危害作物
   - 发病高峰期
   - 传播途径

2. 症状描述：
   - 典型症状特征
   - 发病部位
   - 发展阶段特征
   - 与相似病虫害的区别

3. 危害程度：
   - 对作物生长的影响
   - 产量损失预估
   - 经济影响

4. 防治措施：
   - 农业防治方法
   - 化学防治方法及推荐药剂
   - 生物防治方法
   - 物理防治方法

5. 预防建议：
   - 种植前预防措施
   - 生长期预防措施
   - 收获后预防措施
   - 轮作倒茬建议

6. 用药指南：
   - 推荐药剂及用量
   - 用药时间和频次
   - 安全间隔期
   - 注意事项

7. 识别评价：
   - 对当前识别结果的评价
   - 提高识别准确率的建议
   - 进一步确认的方法

请确保返回的JSON格式正确，所有字段都包含专业、准确且实用的中文内容，便于普通农户理解和操作。
`;
    
    return prompt;
  }
  
  /**
 * 调用DeepSeek API的核心方法
 * 
 * @param {Object} identifyResult - 识别结果对象
 * @returns {Promise<Object>} - API响应内容
 * @throws {Error} - 当网络连接失败或API调用失败时抛出错误
 */
  async _callApi(identifyResult) {
    // 检查网络连接
    const networkStatus = await checkNetworkConnection();
    if (!networkStatus.isOnline) {
      throw new Error('网络连接不可用，无法调用DeepSeek API');
    }
    
    console.log('正在调用DeepSeek API...');
    
    // 检查API密钥
    if (!this.apiKey) {
      throw new Error('DeepSeek API密钥未配置，请在.env文件中设置VUE_APP_DEEPSEEK_API_KEY');
    }
    
    // 添加Authorization头
    const requestHeaders = {...this.headers};
    requestHeaders['Authorization'] = `Bearer ${this.apiKey}`;
    console.log('添加Authorization头进行认证');
    
    // 构建提示词
    const prompt = this._buildAnalysisPrompt(identifyResult);
    
    // 构建请求数据
    const requestData = {
      model: 'deepseek-chat',
      messages: [
        {
          role: 'user',
          content: prompt
        }
      ],
      temperature: 0.7,
      max_tokens: 2000
    };
    
    console.log('准备发送API请求:', {
      url: this.baseUrl,
      hasApiKey: !!this.apiKey,
      model: 'deepseek-chat'
    });
    
    // 发送请求
    const response = await fetch(this.baseUrl, {
      method: 'POST',
      headers: requestHeaders,
      body: JSON.stringify(requestData)
    });
    
    console.log('API响应状态:', response.status);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('API错误响应内容:', errorText);
      throw new Error(`API请求失败: ${response.status} ${response.statusText} - ${errorText}`);
    }
    
    const data = await response.json();
    console.log('DeepSeek API原始响应:', data);
    
    // 解析响应
    const content = data.choices && data.choices[0] && data.choices[0].message && data.choices[0].message.content;
    if (!content) {
      throw new Error('API返回内容为空');
    }
    
    // 尝试解析JSON
    try {
      // 提取JSON内容（去除代码块标记）
      let jsonContent = content.trim();
      if (jsonContent.startsWith('```json')) {
        jsonContent = jsonContent.replace(/```json\s*/g, '').replace(/```\s*$/g, '');
      } else if (jsonContent.startsWith('```')) {
        jsonContent = jsonContent.replace(/```\s*/g, '').replace(/```\s*$/g, '');
      }
      
      const result = JSON.parse(jsonContent);
      console.log('DeepSeek解析结果:', result);
      return result;
    } catch (parseError) {
      console.error('JSON解析失败:', parseError);
      console.error('原始内容:', content);
      throw new Error('无法解析API返回的JSON数据: ' + parseError.message);
    }
  }
  
  /**
   * 解析分析响应
   * 
   * @param {string} response - API响应内容
   * @param {string} diseaseName - 主要病虫害名称
   * @returns {Object} - 解析后的分析结果
   */
  _parseAnalysisResponse(response) {
    try {
      // 尝试解析JSON响应
      const parsedData = JSON.parse(response);
      return parsedData;
    } catch (error) {
      console.error('解析JSON响应失败:', error);
      // 如果JSON解析失败，返回一个基本的分析结果
      return {
        basicInfo: {
          diseaseType: '未知',
          mainCrops: '未知',
          peakPeriod: '未知',
          transmissionRoute: '未知'
        },
        symptoms: {
          typicalSymptoms: 'JSON解析失败，无法获取症状描述',
          affectedParts: '未知',
          developmentStages: '未知',
          differentiation: '未知'
        },
        severity: {
          impactOnGrowth: '未知',
          yieldLossEstimate: '未知',
          economicImpact: '未知'
        },
        preventionMeasures: {
          agriculturalMeasures: '未知',
          chemicalMeasures: '未知',
          biologicalMeasures: '未知',
          physicalMeasures: '未知'
        },
        preventionSuggestions: {
          prePlantingMeasures: '未知',
          growingPeriodMeasures: '未知',
          postHarvestMeasures: '未知',
          cropRotationSuggestions: '未知'
        },
        medicationGuide: {
          recommendedMedicines: '未知',
          medicationTime: '未知',
          safetyInterval: '未知',
          precautions: '未知'
        },
        identificationEvaluation: {
          evaluation: 'JSON解析失败',
          improvementSuggestions: '未知',
          furtherConfirmationMethods: '未知'
        }
      };
    }
  }
  
  /**
   * 获取模拟的病虫害分析数据
   * @param {string} diseaseName - 病虫害名称
   * @returns {Object} - 模拟的分析结果数据
   */
  _getMockAnalysisData(diseaseName) {
    // 根据不同的病虫害返回不同的模拟数据
    const mockDataMap = {
      '健康植株': {
        basicInfo: {
          diseaseType: '正常',
          mainCrops: '水稻',
          peakPeriod: '无',
          transmissionRoute: '无'
        },
        symptoms: {
          typicalSymptoms: '植株生长健壮，叶片翠绿，无明显病斑或虫害迹象',
          affectedParts: '无',
          developmentStages: '生长正常',
          differentiation: '无需区分'
        },
        severity: {
          currentLevel: '无',
          potentialImpact: '无影响',
          spreadRisk: '无'
        },
        prevention: {
          agriculturalMeasures: ['继续保持良好的田间管理'],
          chemicalMeasures: ['无需化学防治'],
          biologicalMeasures: ['无需生物防治']
        },
        advice: {
          immediateActions: ['继续正常管理'],
          monitoringPlan: ['定期巡查'],
          longTermStrategy: ['保持科学种植习惯']
        }
      },
      '稻瘟病': {
        basicInfo: {
          diseaseType: '真菌性病害',
          mainCrops: ['水稻'],
          peakPeriod: ['分蘖期', '抽穗期'],
          transmissionRoute: ['气流传播', '雨水传播']
        },
        symptoms: {
          typicalSymptoms: '叶片出现梭形病斑，中央灰白色，边缘褐色，有黄色晕圈',
          affectedParts: ['叶片', '茎秆', '穗颈'],
          developmentStages: ['苗期', '分蘖期', '抽穗期'],
          differentiation: '注意与胡麻斑病、细菌性条斑病区分'
        },
        severity: {
          currentLevel: '中等',
          potentialImpact: '可能造成5-20%减产',
          spreadRisk: '高'
        },
        prevention: {
          agriculturalMeasures: ['合理密植', '科学施肥', '及时排水'],
          chemicalMeasures: ['75%三环唑可湿性粉剂', '40%稻瘟灵乳油'],
          biologicalMeasures: ['井冈霉素', '春雷霉素']
        },
        advice: {
          immediateActions: ['喷施三环唑防治', '加强田间通风'],
          monitoringPlan: ['每3天检查一次', '关注天气预报'],
          longTermStrategy: ['种植抗病品种', '实行轮作']
        }
      },
      '纹枯病': {
        basicInfo: {
          diseaseType: '真菌性病害',
          mainCrops: ['水稻', '小麦'],
          peakPeriod: ['分蘖末期', '孕穗期'],
          transmissionRoute: ['土壤传播', '病残体传播']
        },
        symptoms: {
          typicalSymptoms: '叶鞘和叶片出现云纹状病斑，边缘褐色，中央灰白色',
          affectedParts: ['叶鞘', '叶片', '茎秆'],
          developmentStages: ['分蘖期', '孕穗期', '抽穗期'],
          differentiation: '注意与稻瘟病、小菌核病区分'
        },
        severity: {
          currentLevel: '轻度',
          potentialImpact: '可能造成3-10%减产',
          spreadRisk: '中等'
        },
        prevention: {
          agriculturalMeasures: ['降低种植密度', '控制氮肥用量', '晒田控蘖'],
          chemicalMeasures: ['20%井冈霉素可溶粉剂', '10%苯醚甲环唑水分散粒剂'],
          biologicalMeasures: ['木霉菌制剂', '荧光假单胞菌']
        },
        advice: {
          immediateActions: ['喷施井冈霉素', '合理灌溉'],
          monitoringPlan: ['定期检查茎基部', '记录病情发展'],
          longTermStrategy: ['深耕翻土', '清除病残体']
        }
      },
      '细菌性条斑病': {
        basicInfo: {
          diseaseType: '细菌性病害',
          mainCrops: ['水稻'],
          peakPeriod: ['孕穗期', '抽穗期'],
          transmissionRoute: ['风雨传播', '农事操作传播']
        },
        symptoms: {
          typicalSymptoms: '叶片出现暗绿色水渍状条斑，后变为黄褐色，有菌脓溢出',
          affectedParts: ['叶片', '叶鞘'],
          developmentStages: ['孕穗期', '抽穗期'],
          differentiation: '注意与稻瘟病、胡麻斑病区分'
        },
        severity: {
          currentLevel: '中等',
          potentialImpact: '可能造成10-30%减产',
          spreadRisk: '高'
        },
        prevention: {
          agriculturalMeasures: ['加强检疫', '合理灌溉', '避免偏施氮肥'],
          chemicalMeasures: ['20%噻菌铜悬浮剂', '72%农用硫酸链霉素可溶性粉剂'],
          biologicalMeasures: ['枯草芽孢杆菌', '荧光假单胞菌']
        },
        advice: {
          immediateActions: ['喷施噻菌铜', '避免串灌漫灌'],
          monitoringPlan: ['密切观察病情', '记录发病面积'],
          longTermStrategy: ['种植抗病品种', '建立无病留种田']
        }
      }
    };
    
    // 返回对应病虫害的数据，如果没有找到则返回默认数据
    return mockDataMap[diseaseName] || {
      basicInfo: {
        diseaseType: '未知',
        mainCrops: ['水稻'],
        peakPeriod: ['未知'],
        transmissionRoute: ['未知']
      },
      symptoms: {
        typicalSymptoms: '症状特征不明确',
        affectedParts: ['叶片'],
        developmentStages: ['未知'],
        differentiation: '无法明确区分'
      },
      severity: {
        currentLevel: '未知',
        potentialImpact: '影响程度未知',
        spreadRisk: '未知'
      },
      prevention: {
        agriculturalMeasures: ['加强田间管理'],
        chemicalMeasures: ['建议咨询专业人员'],
        biologicalMeasures: ['建议咨询专业人员']
      },
      advice: {
        immediateActions: ['请专业人员确诊'],
        monitoringPlan: ['密切观察'],
        longTermStrategy: ['加强栽培管理']
      }
    };
  }
}

// 全局实例
let deepseekApiInstance = null;

/**
 * 初始化DeepSeek API实例
 * 
 * @returns {DeepSeekAPI} - 初始化后的API实例
 */
export function initDeepseekAPI() {
  deepseekApiInstance = new DeepSeekAPI();
  console.log('DeepSeek API实例已初始化');
  return deepseekApiInstance;
}

/**
 * 获取DeepSeek API实例
 * 
 * @returns {DeepSeekAPI|null} - API实例
 */
export function getDeepseekAPI() {
  // 如果实例不存在，自动初始化
  if (!deepseekApiInstance) {
    return initDeepseekAPI();
  }
  return deepseekApiInstance;
}

/**
 * 分析病虫害识别结果的便捷函数 - 调用真实DeepSeek API
 * 
 * @param {Object} identifyResult - 识别结果对象
 * @returns {Promise<Object>} - 分析结果
 */
export async function analyzeDiseaseResult(identifyResult) {
  console.log('开始调用DeepSeek API分析病虫害:', identifyResult);
  
  // 验证输入参数
  if (!identifyResult || !identifyResult.diseaseName) {
    throw new Error('识别结果无效');
  }
  
  const diseaseName = identifyResult.diseaseName;
  const confidence = identifyResult.confidence || 0;
  
  // 获取API配置
  const apiKey = process.env.VUE_APP_DEEPSEEK_API_KEY;
  const apiBaseUrl = process.env.VUE_APP_DEEPSEEK_API_BASE_URL || 'https://api.deepseek.com/v1';
  
  if (!apiKey) {
    throw new Error('DeepSeek API密钥未配置，请在.env文件中设置VUE_APP_DEEPSEEK_API_KEY');
  }
  
  try {
    // 构建提示词
    const prompt = `你是一位专业的农业病虫害专家，请对以下识别结果进行详细分析：

识别结果：${diseaseName}
置信度：${(confidence * 100).toFixed(2)}%

请提供以下信息（请用JSON格式返回，使用中文键名）：

{
  "基本信息": {
    "病虫害类型": "病害/虫害/其他",
    "主要危害作物": "作物名称",
    "发生高峰期": "时期",
    "传播途径": "传播方式"
  },
  "症状描述": {
    "典型症状特征": "详细描述",
    "受害部位": "部位",
    "发展阶段特征": "阶段",
    "与相似病虫害的区别": "区别特征"
  },
  "防治措施": {
    "农业防治方法": "方法",
    "化学防治方法及推荐药剂": "药剂",
    "生物防治方法": "方法",
    "物理防治方法": "方法"
  },
  "预防建议": {
    "种植前预防措施": "措施",
    "生长期预防措施": "措施",
    "收获后预防措施": "措施",
    "轮作倒茸建议": "建议"
  },
  "用药指南": {
    "推荐药剂及用量": "药剂和用量",
    "用药时间和频次": "时间频次",
    "安全间隔期": "天数",
    "注意事项": "注意事项"
  },
  "识别评价": {
    "评价": "评价内容",
    "提高识别准确率的建议": "建议",
    "进一步确认的方法": "方法"
  }
}

请确保返回的JSON格式正确，所有字段都包含专业、准确且实用的中文内容。只返回JSON，不要包含其他文本。`;
    
    console.log('准备调用DeepSeek API:', {
      url: `${apiBaseUrl}/chat/completions`,
      hasApiKey: !!apiKey,
      model: 'deepseek-chat'
    });
    
    // 调用DeepSeek API
    const response = await fetch(`${apiBaseUrl}/chat/completions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: [
          {
            role: 'user',
            content: prompt
          }
        ],
        temperature: 0.7,
        max_tokens: 2000
      })
    });
    
    console.log('API响应状态:', response.status);
    console.log('API响应头:', [...response.headers.entries()]);
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('API错误响应内容:', errorText);
      throw new Error(`API请求失败: ${response.status} ${response.statusText} - ${errorText}`);
    }
    
    const data = await response.json();
    console.log('DeepSeek API原始响应:', data);
    
    // 解析响应
    const content = data.choices && data.choices[0] && data.choices[0].message && data.choices[0].message.content;
    if (!content) {
      throw new Error('API返回内容为空');
    }
    
    // 尝试解析JSON
    try {
      // 提取JSON内容（去除代码块标记）
      let jsonContent = content.trim();
      if (jsonContent.startsWith('```json')) {
        jsonContent = jsonContent.replace(/```json\s*/g, '').replace(/```\s*$/g, '');
      } else if (jsonContent.startsWith('```')) {
        jsonContent = jsonContent.replace(/```\s*/g, '').replace(/```\s*$/g, '');
      }
      
      const result = JSON.parse(jsonContent);
      console.log('DeepSeek解析结果:', result);
      return result;
    } catch (parseError) {
      console.error('JSON解析失败:', parseError);
      console.error('原始内容:', content);
      throw new Error('无法解析API返回的JSON数据: ' + parseError.message);
    }
    
  } catch (error) {
    console.error('DeepSeek API调用失败:', error);
    // 直接抛出错误，不使用模拟数据
    throw new Error('调用DeepSeek API失败: ' + error.message);
  }
}
