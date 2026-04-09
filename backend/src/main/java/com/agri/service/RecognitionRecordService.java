package com.agri.service;

import com.agri.model.RecognitionRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 识别记录服务接口
 */
public interface RecognitionRecordService extends IService<RecognitionRecord> {

    /**
     * 进行农作物识别
     * @param fileBytes 图片文件字节数组
     * @param fileName 原始文件名
     * @param userId 用户ID
     * @param modelId 模型ID
     * @param cropType 作物类型
     * @return 识别记录
     */
    RecognitionRecord recognize(byte[] fileBytes, String fileName, Long userId, Long modelId, String cropType, String taskType);

    /**
     * 获取用户的识别记录
     * @param userId 用户ID
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<RecognitionRecord> getUserRecords(Long userId, Integer page, Integer pageSize);

    /**
     * 生成防治建议
     * @param record 识别记录
     * @return 防治建议
     */
    String generatePreventionAdvice(RecognitionRecord record);

}