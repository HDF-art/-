-- 初始化数据库
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

-- 插入初始用户数据
INSERT IGNORE INTO `user` (`username`, `password`, `role`, `status`) VALUES 
('admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 2, 1),
('root', '$2a$10$sPr/z.JORxZKJkn./3/Yp.HOv4ZRIZIrt.NQ2.ZBPoWSv/o9.mZqW', 1, 1),
('user', '$2a$10$NllAMu6PFB.wtHPJNWRLPu.WDD9.nT0.ty.p26.KV.ObaSR.jw83u', 3, 1);

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

-- 插入默认模型数据
INSERT INTO `model` (`name`, `type`, `file_path`, `accuracy`, `is_default`, `created_at`) VALUES 
('SepResNet全局模型', 'SepResNet', 'models/global_model.pth', 0.9200, 1, NOW())
ON DUPLICATE KEY UPDATE `is_default` = 1;
