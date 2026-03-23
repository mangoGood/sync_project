-- 创建数据库
CREATE DATABASE IF NOT EXISTS sync_task_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sync_task_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色',
    enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建工作流任务表
CREATE TABLE IF NOT EXISTS workflows (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '任务名称',
    source_connection VARCHAR(255) COMMENT '源连接信息',
    target_connection VARCHAR(255) COMMENT '目标连接信息',
    status ENUM('PENDING', 'STARTING', 'FULL_MIGRATING', 'FULL_COMPLETED', 'INCREMENT_RUNNING', 'COMPLETED', 'FAILED', 'PAUSED') DEFAULT 'PENDING' COMMENT '任务状态',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    migration_mode ENUM('full', 'fullAndIncre') DEFAULT 'full' COMMENT '迁移模式：full-全量同步，fullAndIncre-全量+增量同步',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否软删除',
    sync_objects TEXT COMMENT '同步对象JSON，格式: {"db1":["t1","t2"]}',
    source_db_name VARCHAR(255) COMMENT '源数据库名',
    total_tables INT DEFAULT NULL COMMENT '全量同步总表数',
    completed_tables INT DEFAULT 0 COMMENT '全量同步已完成表数',
    current_table VARCHAR(255) DEFAULT NULL COMMENT '当前正在同步的表名',
    current_table_progress INT DEFAULT 0 COMMENT '当前表同步进度百分比',
    current_table_rows BIGINT DEFAULT 0 COMMENT '当前表已同步行数',
    current_table_total_rows BIGINT DEFAULT 0 COMMENT '当前表总行数',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    error_message TEXT COMMENT '错误信息',
    is_billing TINYINT(1) DEFAULT 0 COMMENT '是否计费中',
    INDEX idx_status (status),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_deleted (is_deleted),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步任务工作流表';

-- 创建工作流日志表
CREATE TABLE IF NOT EXISTS workflow_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    workflow_id VARCHAR(36) NOT NULL,
    level ENUM('INFO', 'WARNING', 'ERROR') DEFAULT 'INFO' COMMENT '日志级别',
    message TEXT NOT NULL COMMENT '日志内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workflow_id) REFERENCES workflows(id) ON DELETE CASCADE,
    INDEX idx_workflow_id (workflow_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流日志表';

-- 创建校验任务表
CREATE TABLE IF NOT EXISTS validation_tasks (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT '校验任务名称',
    workflow_id VARCHAR(36) NOT NULL COMMENT '关联的同步任务ID',
    workflow_name VARCHAR(255) COMMENT '关联的同步任务名称',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    source_connection TEXT COMMENT '源数据库连接',
    target_connection TEXT COMMENT '目标数据库连接',
    sync_objects TEXT COMMENT '同步对象JSON',
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING' COMMENT '校验状态',
    total_tables INT DEFAULT 0 COMMENT '总表数',
    passed_tables INT DEFAULT 0 COMMENT '通过校验的表数',
    failed_tables INT DEFAULT 0 COMMENT '校验失败的表数',
    total_rows BIGINT DEFAULT 0 COMMENT '总行数',
    mismatched_rows BIGINT DEFAULT 0 COMMENT '不一致的行数',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    started_at TIMESTAMP NULL COMMENT '开始时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否软删除',
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_is_deleted (is_deleted),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据校验任务表';

-- 创建校验任务日志表
CREATE TABLE IF NOT EXISTS validation_task_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    validation_task_id VARCHAR(36) NOT NULL COMMENT '校验任务ID',
    level ENUM('INFO', 'WARNING', 'ERROR') DEFAULT 'INFO' COMMENT '日志级别',
    message TEXT COMMENT '日志内容',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_validation_task_id (validation_task_id),
    FOREIGN KEY (validation_task_id) REFERENCES validation_tasks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校验任务日志表';

-- 插入默认管理员账号 (密码: admin123)
INSERT INTO users (username, password, email, role, enabled) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'admin@example.com', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE username=username;

-- 插入测试用户账号 (密码: user123)
INSERT INTO users (username, password, email, role, enabled) 
VALUES ('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user1@example.com', 'USER', 1)
ON DUPLICATE KEY UPDATE username=username;

INSERT INTO users (username, password, email, role, enabled) 
VALUES ('user2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user2@example.com', 'USER', 1)
ON DUPLICATE KEY UPDATE username=username;
