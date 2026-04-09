package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("training_task")
public class TrainingTask implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String status;
    private String datasetPath;
    private String modelType;
    private String parameters;
    private BigDecimal accuracy;
    private Long farmId;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
