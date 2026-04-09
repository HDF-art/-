package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据交易实体类
 */
@Data
@TableName("data_transaction")
public class DataTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据流通ID
     */
    private Long circulationId;

    /**
     * 数据集ID
     */
    private Long datasetId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 交易价格
     */
    private Double price;

    /**
     * 交易状态（0:待确认, 1:已确认, 2:已完成, 3:已取消）
     */
    private Integer status;

    /**
     * 交易时间
     */
    private LocalDateTime transactionTime;

    /**
     * 完成时间
     */
    private LocalDateTime completionTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

}
