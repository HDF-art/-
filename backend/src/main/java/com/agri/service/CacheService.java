package com.agri.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // 缓存前缀
    private static final String PREFIX = "agri:";
    
    /**
     * 设置缓存
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(PREFIX + key, value, timeout, unit);
    }
    
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(PREFIX + key, value);
    }
    
    /**
     * 获取缓存
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(PREFIX + key);
    }
    
    /**
     * 删除缓存
     */
    public void delete(String key) {
        redisTemplate.delete(PREFIX + key);
    }
    
    /**
     * 判断缓存是否存在
     */
    public boolean exists(String key) {
        return redisTemplate.hasKey(PREFIX + key);
    }
    
    // 常用缓存方法
    
    /**
     * 缓存用户信息
     */
    public void cacheUser(String userId, Object userInfo) {
        set("user:" + userId, userInfo, 30, TimeUnit.MINUTES);
    }
    
    public Object getCachedUser(String userId) {
        return get("user:" + userId);
    }
    
    /**
     * 缓存模型列表
     */
    public void cacheModels(Object models) {
        set("federation:models", models, 1, TimeUnit.HOURS);
    }
    
    public Object getCachedModels() {
        return get("federation:models");
    }
    
    /**
     * 缓存任务列表
     */
    public void cacheTasks(String userId, Object tasks) {
        set("task:list:" + userId, tasks, 5, TimeUnit.MINUTES);
    }
    
    public Object getCachedTasks(String userId) {
        return get("task:list:" + userId);
    }
    
    /**
     * 缓存统计数据
     */
    public void cacheStatistics(Object stats) {
        set("statistics", stats, 10, TimeUnit.MINUTES);
    }
    
    public Object getCachedStatistics() {
        return get("statistics");
    }
    
    /**
     * 清除用户相关缓存
     */
    public void clearUserCache(String userId) {
        delete("user:" + userId);
        delete("task:list:" + userId);
    }
    
    /**
     * 清除所有缓存
     */
    public void clearAll() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
