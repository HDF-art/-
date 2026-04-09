package com.agri.mapper;

import com.agri.model.Farm;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 农场Mapper接口
 */
@Mapper
public interface FarmMapper extends BaseMapper<Farm> {

    /**
     * 根据管理员ID查询农场
     * @param adminId 管理员ID
     * @return 农场信息
     */
    Farm findByAdminId(@Param("adminId") Long adminId);

}