package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据流通实体类
 */
@Data
@TableName("data_circulation")
public class DataCirculation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据集名称
     */
    private String datasetName;

    /**
     * 数据集类型
     */
    private String datasetType;

    /**
     * 数据集大小（字节）
     */
    private Long datasetSize;

    /**
     * 数据集描述
     */
    private String description;

    /**
     * 价格
     */
    private Double price;

    /**
     * 发布者ID
     */
    private Long publisherId;

    /**
     * 发布者所属农场ID
     */
    private Long farmId;

    /**
     * 状态（0:待交易, 1:交易中, 2:已完成, 3:已取消）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 关联的数据集ID
     */
    private Long datasetId;

} 
