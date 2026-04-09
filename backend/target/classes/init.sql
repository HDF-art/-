-- 如果数据库不存在则创建
CREATE DATABASE IF NOT EXISTS agri_platform;
USE agri_platform;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `role` int NOT NULL COMMENT '角色（1:一级管理员, 2:二级管理员, 3:普通用户）',
  `farm_id` bigint DEFAULT NULL COMMENT '所属农场ID',
  `name` varchar(50) DEFAULT NULL COMMENT '姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态（0:禁用, 1:启用）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 农场表
CREATE TABLE IF NOT EXISTS `farm` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '农场ID',
  `name` varchar(100) NOT NULL COMMENT '农场名称',
  `admin_id` bigint NOT NULL COMMENT '管理员用户ID',
  `location` varchar(200) DEFAULT NULL COMMENT '农场位置',
  `description` text COMMENT '农场描述',
  `area` decimal(10,2) DEFAULT NULL COMMENT '农场面积（亩）',
  `main_crops` varchar(200) DEFAULT NULL COMMENT '主要作物',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='农场表';

-- 训练任务表
CREATE TABLE IF NOT EXISTS `training_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `name` varchar(100) NOT NULL COMMENT '任务名称',
  `status` varchar(20) NOT NULL COMMENT '状态（待开始/进行中/已完成/已失败）',
  `dataset_path` varchar(200) DEFAULT NULL COMMENT '数据集路径',
  `model_type` varchar(50) DEFAULT NULL COMMENT '模型类型',
  `parameters` text COMMENT '训练参数（JSON格式）',
  `accuracy` decimal(5,4) DEFAULT NULL COMMENT '准确率',
  `farm_id` bigint DEFAULT NULL COMMENT '关联农场ID',
  `created_by` bigint DEFAULT NULL COMMENT '创建者ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  PRIMARY KEY (`id`),
  KEY `idx_farm_id` (`farm_id`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练任务表';

-- 模型表
CREATE TABLE IF NOT EXISTS `model` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模型ID',
  `name` varchar(100) NOT NULL COMMENT '模型名称',
  `type` varchar(50) NOT NULL COMMENT '模型类型',
  `task_id` bigint DEFAULT NULL COMMENT '关联训练任务ID',
  `farm_id` bigint DEFAULT NULL COMMENT '关联农场ID',
  `file_path` varchar(200) NOT NULL COMMENT '模型文件路径',
  `accuracy` decimal(5,4) DEFAULT NULL COMMENT '准确率',
  `is_default` tinyint DEFAULT 0 COMMENT '是否默认模型（0-否，1-是）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_farm_id` (`farm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型表';

-- 识别记录表
CREATE TABLE IF NOT EXISTS `recognition_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `model_id` bigint DEFAULT NULL COMMENT '使用的模型ID',
  `image_path` varchar(200) NOT NULL COMMENT '图片路径',
  `result` varchar(100) NOT NULL COMMENT '识别结果',
  `result_json` text COMMENT '识别结果JSON（完整）',
  `confidence` decimal(5,4) DEFAULT NULL COMMENT '置信度',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `prevention_advice` text COMMENT '防治建议',
  `crop_type` varchar(50) DEFAULT NULL COMMENT '作物类型',
  `task_type` varchar(50) DEFAULT NULL COMMENT '任务类型',
  `status` varchar(20) DEFAULT 'normal' COMMENT '状态（normal:正常, abnormal:异常）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='识别记录表';

-- 任务参与表
CREATE TABLE IF NOT EXISTS `task_participation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '参与ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `user_id` bigint NOT NULL COMMENT '参与用户ID',
  `status` varchar(20) NOT NULL COMMENT '参与状态（待确认、已同意、已拒绝）',
  `participate_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '参与时间',
  `confirm_time` datetime DEFAULT NULL COMMENT '确认时间',
  `remark` text COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_user_id` (`user_id`),
  UNIQUE KEY `uk_user_task` (`user_id`, `task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务参与表';

-- 训练任务表添加新字段
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `audit_status` varchar(20) DEFAULT '待审核' COMMENT '审核状态（待审核、已通过、已拒绝）';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `task_type` varchar(50) DEFAULT NULL COMMENT '任务类型（图像识别、目标检测、时序预测）';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `federated_algorithm` varchar(100) DEFAULT NULL COMMENT '联邦学习算法';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `training_model` varchar(100) DEFAULT NULL COMMENT '训练模型';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `deadline` datetime DEFAULT NULL COMMENT '任务等待截止时间';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `expected_participants` int DEFAULT 0 COMMENT '预定参与客户端数量';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `actual_participants` int DEFAULT 0 COMMENT '实际参与客户端数量';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `audit_comment` text COMMENT '审核意见';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `audited_by` bigint DEFAULT NULL COMMENT '审核人ID';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `audited_at` datetime DEFAULT NULL COMMENT '审核时间';
ALTER TABLE `training_task` ADD COLUMN IF NOT EXISTS `dataset_id` bigint DEFAULT NULL COMMENT '数据集ID';
ALTER TABLE `training_task` ADD KEY IF NOT EXISTS `idx_dataset_id` (`dataset_id`);

-- 插入初始数据
-- 插入预设用户（密码：admin/admin123，root/root，user/user，均已BCrypt加密）
-- 使用INSERT IGNORE确保只在用户不存在时插入数据
INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`) VALUES 
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 2, 1),
('root', '$2a$10$sPr/z.JORxZKJkn./3/Yp.HOv4ZRIZIrt.NQ2.ZBPoWSv/o9.mZqW', 1, 1),
('user', '$2a$10$NllAMu6PFB.wtHPJNWRLPu.WDD9.nT0.ty.p26.KV.ObaSR.jw83u', 3, 1);

-- 插入默认模型数据
INSERT INTO `model` (`name`, `type`, `file_path`, `accuracy`, `is_default`, `created_at`) VALUES 
('SepResNet全局模型', 'SepResNet', 'models/global_model.pth', 0.9200, 1, NOW())
ON DUPLICATE KEY UPDATE `is_default` = 1;

-- 注意：模型能识别的15个类别如下：
-- 0: Curculionidae (稻象甲科)
-- 1: Delphacidae (飞虱科)
-- 2: Cicadellidae (叶蝉科)
-- 3: Phlaeothripidae (管蓟马科)
-- 4: Cecidomyiidae (瘿蚊科)
-- 5: Hesperiidae (弄蝶科)
-- 6: Crambidae (草螟科)
-- 7: Chloropidae (秆蝇科)
-- 8: Ephydridae (水蝇科)
-- 9: Noctuidae (夜蛾科)
-- 10: Thripidae (蓟马科)
-- 11: Bacterialblight (细菌性白叶枯病)
-- 12: Blast (稻瘟病)
-- 13: Brownspot (褐斑病)
-- 14: Tungro (钨谷病毒病)

-- 注意：上面的密码是加密后的'admin123'，实际项目中应该使用安全的密码策略

-- 创建审核请求表
CREATE TABLE IF NOT EXISTS `audit_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核请求ID',
  `applicant_id` bigint NOT NULL COMMENT '申请人ID',
  `type` int NOT NULL COMMENT '申请类型（1:二级管理员个人信息修改, 2:二级管理员账号注销, 3:普通用户加入农场）',
  `status` int NOT NULL DEFAULT 0 COMMENT '审核状态（0:待审核, 1:已通过, 2:已拒绝）',
  `farm_id` bigint DEFAULT NULL COMMENT '相关农场ID（普通用户加入农场时使用）',
  `content` text COMMENT '申请内容（JSON格式）',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `audit_comment` text COMMENT '审核意见',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `audited_at` datetime DEFAULT NULL COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核请求表';

-- 创建通知表
CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID（如果为null则表示发送给所有用户）',
  `receiver_type` int NOT NULL COMMENT '接收者类型（1:所有用户, 2:一级管理员, 3:二级管理员, 4:普通用户, 5:特定用户）',
  `farm_id` bigint DEFAULT NULL COMMENT '关联农场ID（二级管理员发送通知时使用）',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0:未读, 1:已读）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` datetime DEFAULT NULL COMMENT '读取时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_receiver_type` (`receiver_type`),
  KEY `idx_farm_id` (`farm_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 创建数据集表
CREATE TABLE IF NOT EXISTS `dataset` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据集ID',
  `name` varchar(100) NOT NULL COMMENT '数据集名称',
  `path` varchar(255) NOT NULL COMMENT '数据集路径',
  `size` bigint NOT NULL COMMENT '数据集大小（字节）',
  `type` varchar(50) NOT NULL COMMENT '数据集类型（图像、文本、时间序列等）',
  `description` text COMMENT '描述',
  `uploader_id` bigint NOT NULL COMMENT '上传者ID',
  `farm_id` bigint NOT NULL COMMENT '所属农场ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0:未使用, 1:使用中, 2:已归档）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_farm_id` (`farm_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据集表';

-- 创建数据流通表
CREATE TABLE IF NOT EXISTS `data_circulation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '数据流通ID',
  `dataset_id` bigint NOT NULL COMMENT '关联的数据集ID',
  `dataset_name` varchar(100) NOT NULL COMMENT '数据集名称',
  `dataset_type` varchar(50) NOT NULL COMMENT '数据集类型',
  `dataset_size` bigint NOT NULL COMMENT '数据集大小（字节）',
  `description` text COMMENT '数据集描述',
  `price` double NOT NULL COMMENT '价格',
  `publisher_id` bigint NOT NULL COMMENT '发布者ID',
  `farm_id` bigint NOT NULL COMMENT '发布者所属农场ID',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0:待交易, 1:交易中, 2:已完成, 3:已取消）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_dataset_id` (`dataset_id`),
  KEY `idx_publisher_id` (`publisher_id`),
  KEY `idx_farm_id` (`farm_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据流通表';

-- 创建数据交易表
CREATE TABLE IF NOT EXISTS `data_transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `circulation_id` bigint NOT NULL COMMENT '数据流通ID',
  `dataset_id` bigint NOT NULL COMMENT '数据集ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `buyer_id` bigint NOT NULL COMMENT '买家ID',
  `price` double NOT NULL COMMENT '交易价格',
  `status` int NOT NULL DEFAULT 0 COMMENT '交易状态（0:待确认, 1:已确认, 2:已完成, 3:已取消）',
  `transaction_time` datetime DEFAULT NULL COMMENT '交易时间',
  `completion_time` datetime DEFAULT NULL COMMENT '完成时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_circulation_id` (`circulation_id`),
  KEY `idx_dataset_id` (`dataset_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据交易表';

-- 创建安全消息表
CREATE TABLE IF NOT EXISTS `secure_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `transaction_id` bigint NOT NULL COMMENT '交易ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `encrypted_content` text NOT NULL COMMENT '加密消息内容',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0:未读, 1:已读）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` datetime DEFAULT NULL COMMENT '读取时间',
  PRIMARY KEY (`id`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='安全消息表';

-- 创建训练日志表
CREATE TABLE IF NOT EXISTS `training_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `task_id` bigint NOT NULL COMMENT '训练任务ID',
  `log_type` varchar(20) NOT NULL COMMENT '日志类型（info:信息, warning:警告, error:错误）',
  `content` text NOT NULL COMMENT '日志内容',
  `timestamp` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  `node_id` varchar(100) DEFAULT NULL COMMENT '相关节点/客户端ID',
  `round` int DEFAULT NULL COMMENT '轮次信息',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_log_type` (`log_type`),
  KEY `idx_timestamp` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练日志表';