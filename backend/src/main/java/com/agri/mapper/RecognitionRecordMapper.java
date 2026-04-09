package com.agri.mapper;

import com.agri.model.RecognitionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 识别记录Mapper接口
 */
@Mapper
public interface RecognitionRecordMapper extends BaseMapper<RecognitionRecord> {

    /**
     * 根据用户ID分页查询识别记录
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 查询数量限制
     * @return 识别记录列表
     */
    java.util.List<RecognitionRecord> selectByUserId(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

}