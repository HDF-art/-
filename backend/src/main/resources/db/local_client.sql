-- 本地客户端表
CREATE TABLE IF NOT EXISTS local_client (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL UNIQUE COMMENT '客户端唯一标识',
    user_id BIGINT COMMENT '所属用户ID',
    client_name VARCHAR(100) COMMENT '客户端名称',
    status VARCHAR(20) DEFAULT 'disconnected' COMMENT '状态: connected, disconnected',
    platform VARCHAR(50) COMMENT '操作系统平台',
    python_version VARCHAR(20) COMMENT 'Python版本',
    cpu_count INT COMMENT 'CPU核心数',
    memory_total BIGINT COMMENT '总内存(字节)',
    disk_total BIGINT COMMENT '总磁盘空间(字节)',
    data_dir VARCHAR(500) COMMENT '数据目录路径',
    datasets_count INT DEFAULT 0 COMMENT '数据集数量',
    running_tasks INT DEFAULT 0 COMMENT '运行中任务数',
    last_heartbeat DATETIME COMMENT '最后心跳时间',
    connected_at DATETIME COMMENT '连接时间',
    disconnected_at DATETIME COMMENT '断开时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_last_heartbeat (last_heartbeat)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地客户端信息表';

-- 本地数据集表
CREATE TABLE IF NOT EXISTS local_dataset (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL COMMENT '所属客户端ID',
    name VARCHAR(255) NOT NULL COMMENT '文件名',
    path VARCHAR(500) NOT NULL COMMENT '文件路径',
    size BIGINT COMMENT '文件大小(字节)',
    file_type VARCHAR(20) COMMENT '文件类型',
    checksum VARCHAR(64) COMMENT '文件校验和',
    row_count INT COMMENT '行数',
    column_count INT COMMENT '列数',
    columns TEXT COMMENT '列名列表(JSON)',
    created_at DATETIME COMMENT '文件创建时间',
    modified_at DATETIME COMMENT '文件修改时间',
    synced_at DATETIME COMMENT '同步时间',
    INDEX idx_client_id (client_id),
    INDEX idx_file_type (file_type),
    UNIQUE KEY uk_client_path (client_id, path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='本地数据集信息表';

-- 训练任务表（如果不存在则创建）
CREATE TABLE IF NOT EXISTS training_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(100) NOT NULL UNIQUE COMMENT '任务唯一标识',
    client_id VARCHAR(100) COMMENT '执行客户端ID',
    user_id BIGINT COMMENT '创建用户ID',
    dataset_path VARCHAR(500) COMMENT '数据集路径',
    model_type VARCHAR(50) COMMENT '模型类型',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending, running, completed, failed, stopped',
    progress DECIMAL(5,2) DEFAULT 0 COMMENT '进度百分比',
    current_epoch INT DEFAULT 0 COMMENT '当前轮次',
    total_epochs INT COMMENT '总轮次',
    loss DECIMAL(10,6) COMMENT '损失值',
    accuracy DECIMAL(5,4) COMMENT '准确率',
    hyperparameters TEXT COMMENT '超参数(JSON)',
    error_message TEXT COMMENT '错误信息',
    started_at DATETIME COMMENT '开始时间',
    finished_at DATETIME COMMENT '结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_client_id (client_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练任务表';
