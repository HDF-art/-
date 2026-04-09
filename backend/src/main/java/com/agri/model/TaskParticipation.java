package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务参与实体类
 */
@Data
@TableName("task_participation")
public class TaskParticipation implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 参与用户ID（二级管理员）
     */
    private Long userId;

    /**
     * 参与状态（待确认、已同意、已拒绝）
     */
    private String status;

    /**
     * 参与时间
     */
    private LocalDateTime participateTime;

    /**
     * 确认时间
     */
    private LocalDateTime confirmTime;

    /**
     * 备注
     */
    private String remark;

}
