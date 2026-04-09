package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据集实体类
 */
@Data
@TableName("dataset")
public class Dataset implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 数据集路径
     */
    private String path;

    /**
     * 数据集大小（字节）
     */
    private Long size;

    /**
     * 数据集类型（图像、文本、时间序列等）
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    /**
     * 所属农场ID
     */
    private Long farmId;

    /**
     * 状态（0:未使用, 1:使用中, 2:已归档）
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

}
