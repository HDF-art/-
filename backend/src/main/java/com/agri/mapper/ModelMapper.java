package com.agri.mapper;

import com.agri.model.Model;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 模型Mapper接口
 */
@Mapper
public interface ModelMapper extends BaseMapper<Model> {

    /**
     * 根据任务ID查询模型列表
     * @param taskId 任务ID
     * @return 模型列表
     */
    @Select("SELECT * FROM model WHERE task_id = #{taskId}")
    java.util.List<Model> findByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据农场ID查询个性化模型
     * @param farmId 农场ID
     * @return 模型列表
     */
    @Select("SELECT * FROM model WHERE farm_id = #{farmId}")
    java.util.List<Model> findByFarmId(@Param("farmId") Long farmId);

    /**
     * 查询默认模型
     * @return 默认模型
     */
    @Select("SELECT * FROM model WHERE is_default = 1 LIMIT 1")
    Model findDefaultModel();

}