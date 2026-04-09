package com.agri.mapper;

import com.agri.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User findByUsername(@Param("username") String username);

    /**
     * 根据农场ID查询用户列表
     * @param farmId 农场ID
     * @return 用户列表
     */
    @Select("SELECT * FROM user WHERE farm_id = #{farmId}")
    java.util.List<User> findByFarmId(@Param("farmId") Long farmId);

    /**
     * 根据手机号查询用户
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM user WHERE phone = #{phone}")
    User findByPhone(@Param("phone") String phone);
    
    User findByEmail(@Param("email") String email);
}

