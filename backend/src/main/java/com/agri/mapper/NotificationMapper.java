package com.agri.mapper;

import com.agri.dto.NotificationDTO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

/**
 * 通知Mapper
 */
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 分页查询用户的通知，并包含发件人信息
     */
    IPage<NotificationDTO> selectUserNotificationsPage(
            IPage<NotificationDTO> page, 
            @Param(Constants.WRAPPER) Wrapper<Notification> queryWrapper);
}

}
