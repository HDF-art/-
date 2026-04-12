/**
 * DeepSeek API 集成模块
 * 通过后端代理调用DeepSeek API，避免API Key暴露在前端
 */

import axios from 'axios';

const API_BASE_URL = process.env.VUE_APP_BASE_API || '/api';

class DeepSeekAPI {
  constructor() {
    this.baseUrl = `${API_BASE_URL}/ai`;
  }
  
  async analyzeDisease(identifyResult) {
    if (!identifyResult) {
      throw new Error('识别结果对象为空');
    }
    
    if (!identifyResult.diseaseName) {
      throw new Error('识别结果中缺少病虫害名称');
    }
    
    return await this._callApi(identifyResult);
  }
  
  _buildAnalysisPrompt(identifyResult) {
    const mainDisease = identifyResult.diseaseName;
    const confidence = identifyResult.confidence;
    const details = identifyResult.details ? identifyResult.details.map(item => 
      `- ${item.diseaseName} (${(item.confidence * 100).toFixed(2)}%)`
    ).join('\n') : '';
    
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
  
  async _callApi(identifyResult) {
    const prompt = this._buildAnalysisPrompt(identifyResult);
    
    const requestData = {
      messages: [
        {
          role: 'user',
          content: prompt
        }
      ]
    };
    
    try {
      const response = await axios.post(`${this.baseUrl}/analyze`, requestData, {
        headers: {
          'Content-Type': 'application/json'
        },
        timeout: 60000
      });
      
      if (response.data && response.data.success) {
        const content = response.data.data?.choices?.[0]?.message?.content;
        if (content) {
          let jsonContent = content.trim();
          if (jsonContent.startsWith('```json')) {
            jsonContent = jsonContent.replace(/```json\s*/g, '').replace(/```\s*$/g, '');
          } else if (jsonContent.startsWith('```')) {
            jsonContent = jsonContent.replace(/```\s*/g, '').replace(/```\s*$/g, '');
          }
          return JSON.parse(jsonContent);
        }
      }
      throw new Error('AI分析服务返回数据格式错误');
    } catch (error) {
      if (error.response) {
        throw new Error(error.response.data?.message || 'AI分析服务调用失败');
      }
      throw new Error('AI分析服务暂时不可用: ' + error.message);
    }
  }
}

let deepseekApiInstance = null;

export function initDeepseekAPI() {
  deepseekApiInstance = new DeepSeekAPI();
  return deepseekApiInstance;
}

export function getDeepseekAPI() {
  if (!deepseekApiInstance) {
    return initDeepseekAPI();
  }
  return deepseekApiInstance;
}

export async function analyzeDiseaseResult(identifyResult) {
  if (!identifyResult || !identifyResult.diseaseName) {
    throw new Error('识别结果无效');
  }
  
  const api = getDeepseekAPI();
  return await api.analyzeDisease(identifyResult);
}
