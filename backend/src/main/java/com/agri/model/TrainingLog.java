package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 训练日志实体类
 */
@Data
@TableName("training_log")
public class TrainingLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 训练任务ID
     */
    private Long taskId;

    /**
     * 日志类型（信息、警告、错误）
     */
    private String logType;

    /**
     * 日志内容
     */
    private String content;

    /**
     * 发生时间
     */
    private LocalDateTime timestamp;

    /**
     * 相关节点/客户端ID
     */
    private String nodeId;

    /**
     * 轮次信息
     */
    private Integer round;

}
