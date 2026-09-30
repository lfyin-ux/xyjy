USE xyjy;
SET NAMES utf8mb4;

ALTER TABLE app_user ADD COLUMN personal_approved_at DATETIME NULL COMMENT '个人认证通过时间' AFTER identity_verified;
ALTER TABLE app_user ADD COLUMN grace_publish_used TINYINT DEFAULT 0 COMMENT '宽限期是否已发布动态 0否 1是' AFTER personal_approved_at;
ALTER TABLE app_user ADD COLUMN grace_post_id BIGINT NULL COMMENT '宽限期内发布的动态ID' AFTER grace_publish_used;
