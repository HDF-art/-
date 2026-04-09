package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 农场实体类
 */
@Data
@TableName("farm")
public class Farm implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 农场名称
     */
    private String name;

    /**
     * 农场管理员ID
     */
    private Long adminId;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 描述
     */
    private String description;

    /**
     * 种植面积（亩）
     */
    private Double area;

    /**
     * 主要作物
     */
    private String mainCrops;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}