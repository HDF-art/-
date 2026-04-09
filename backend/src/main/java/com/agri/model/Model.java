package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模型实体类
 */
@Data
@TableName("model")
public class Model implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型类型（全局模型、个性化模型）
     */
    private String type;

    /**
     * 关联的训练任务ID
     */
    private Long taskId;

    /**
     * 所属农场ID（个性化模型）
     */
    private Long farmId;

    /**
     * 准确率
     */
    private Double accuracy;

    /**
     * 精确率
     */
    private Double precision;

    /**
     * 召回率
     */
    private Double recall;

    /**
     * F1分数
     */
    private Double f1Score;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 模型文件路径
     */
    private String filePath;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 是否为默认模型
     */
    private Integer isDefault;

}