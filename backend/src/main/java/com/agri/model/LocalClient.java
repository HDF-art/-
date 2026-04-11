package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("local_client")
public class LocalClient {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String clientId;
    private Long userId;
    private String clientName;
    private String status;
    private String platform;
    private String pythonVersion;
    private Integer cpuCount;
    private Long memoryTotal;
    private Long diskTotal;
    private String dataDir;
    private Integer datasetsCount;
    private Integer runningTasks;
    private LocalDateTime lastHeartbeat;
    private LocalDateTime connectedAt;
    private LocalDateTime disconnectedAt;
}
