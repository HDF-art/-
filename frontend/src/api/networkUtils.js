/**
 * 网络工具类
 * 用于检测网络连接状态和处理网络相关操作
 */

/**
 * 获取当前网络状态
 * @returns {Object} 包含在线状态的对象
 */
export function getCurrentNetworkStatus() {
  return {
    isOnline: navigator.onLine,
    timestamp: new Date().toISOString()
  };
}

/**
 * 检查网络连接状态（通过发送请求确认）
 * @param {string} testUrl - 用于测试连接的URL，默认使用Google
 * @param {number} timeout - 超时时间（毫秒）
 * @returns {Promise<Object>} 包含网络状态的Promise
 */
export async function checkNetworkConnection(testUrl = 'https://www.baidu.com', timeout = 5000) {
  try {
    // 首先检查浏览器的在线状态
    const browserStatus = getCurrentNetworkStatus();
    if (!browserStatus.isOnline) {
      return {
        ...browserStatus,
        verified: false,
        message: '浏览器报告离线'
      };
    }
    
    // 发送请求验证实际网络连接
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);
    
    await fetch(testUrl, {
      method: 'HEAD',
      mode: 'no-cors',
      signal: controller.signal
    });
    
    clearTimeout(timeoutId);
    
    return {
      isOnline: true,
      verified: true,
      timestamp: new Date().toISOString(),
      message: '网络连接正常'
    };
  } catch (error) {
    return {
      isOnline: false,
      verified: true,
      timestamp: new Date().toISOString(),
      message: error.name === 'AbortError' ? '网络请求超时' : error.message || '网络连接异常'
    };
  }
}

/**
 * 重试网络请求的函数
 * @param {Function} requestFn - 需要重试的请求函数
 * @param {Object} options - 重试选项
 * @param {number} options.maxRetries - 最大重试次数，默认3次
 * @param {number} options.delay - 初始延迟时间（毫秒），默认1000ms
 * @param {number} options.backoffFactor - 退避因子，默认2（每次重试延迟时间翻倍）
 * @returns {Promise<any>} 请求结果的Promise
 */
export async function retryRequest(requestFn, options = {}) {
  const {
    maxRetries = 3,
    delay = 1000,
    backoffFactor = 2
  } = options;
  
  let lastError;
  
  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      // 如果不是第一次尝试，则添加延迟
      if (attempt > 0) {
        const waitTime = delay * Math.pow(backoffFactor, attempt - 1);
        console.log(`请求失败，${waitTime}ms后尝试第${attempt}次重试...`);
        await new Promise(resolve => setTimeout(resolve, waitTime));
      }
      
      // 执行请求
      const result = await requestFn();
      return result;
    } catch (error) {
      lastError = error;
      console.error(`请求失败（尝试${attempt}）:`, error);
      
      // 如果是最后一次尝试或不应该重试的错误，则抛出异常
      if (attempt === maxRetries || shouldNotRetry(error)) {
        throw lastError;
      }
    }
  }
  
  // 所有重试都失败后抛出最后一个错误
  throw lastError;
}

/**
 * 检查错误是否应该被重试
 * @param {Error} error - 发生的错误
 * @returns {boolean} 是否应该重试
 */
function shouldNotRetry(error) {
  // 根据错误类型判断是否需要重试
  // 例如：400系列错误通常表示请求参数问题，不需要重试
  if (error.response) {
    const status = error.response.status;
    return status >= 400 && status < 500 && status !== 429; // 排除429 (Too Many Requests)
  }
  // 其他类型的错误（如网络错误）通常可以重试
  return false;
}

/**
 * 创建防抖函数
 * @param {Function} func - 需要防抖的函数
 * @param {number} wait - 等待时间（毫秒）
 * @returns {Function} 防抖后的函数
 */
export function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

/**
 * 创建节流函数
 * @param {Function} func - 需要节流的函数
 * @param {number} limit - 时间限制（毫秒）
 * @returns {Function} 节流后的函数
 */
export function throttle(func, limit) {
  let inThrottle;
  return function(...args) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
}