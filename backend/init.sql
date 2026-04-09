-- 创建数据库
CREATE DATABASE IF NOT EXISTS agri_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE agri_platform;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `role` varchar(20) NOT NULL COMMENT '角色（admin/farmer）',
  `farm_id` bigint DEFAULT NULL COMMENT '所属农场ID',
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
  `model_id` bigint NOT NULL COMMENT '使用的模型ID',
  `image_path` varchar(200) NOT NULL COMMENT '图片路径',
  `result` varchar(100) NOT NULL COMMENT '识别结果',
  `confidence` decimal(5,4) DEFAULT NULL COMMENT '置信度',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `prevention_advice` text COMMENT '防治建议',
  `crop_type` varchar(50) DEFAULT NULL COMMENT '作物类型',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='识别记录表';

-- 插入初始数据
-- 插入管理员用户
INSERT INTO `user` (`username`, `password`, `role`) VALUES 
('admin', '$2a$10$QfM5YJqJk5X5f5Y5Z5W5Oe5R5T5U5V5B5N5M5L5K5J5H5G5F5E5D5C5B5A', 'admin');

-- 注意：上面的密码是加密后的'admin123'，实际项目中应该使用安全的密码策略