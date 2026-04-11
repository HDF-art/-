package com.agri.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("local_dataset")
public class LocalDataset {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String clientId;
    private String name;
    private String path;
    private Long size;
    private String fileType;
    private String checksum;
    private Integer rowCount;
    private Integer columnCount;
    private String columns;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private LocalDateTime syncedAt;
}
