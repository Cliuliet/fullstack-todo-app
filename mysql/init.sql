-- MySQL initialization script
-- This runs automatically when the container starts for the first time

CREATE DATABASE IF NOT EXISTS `todoapp` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `todoapp`;

CREATE TABLE IF NOT EXISTS `todos` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `title`      VARCHAR(500) NOT NULL,
    `completed`  TINYINT(1)   NOT NULL DEFAULT 0,
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample data
INSERT INTO `todos` (`title`, `completed`, `created_at`, `updated_at`) VALUES
('学习 Spring Boot 框架', 1, NOW(), NOW()),
('掌握 Vue 3 + Vite 开发', 0, NOW(), NOW()),
('配置 Docker Compose 一键部署', 0, NOW(), NOW()),
('完成项目文档编写', 0, NOW(), NOW());
