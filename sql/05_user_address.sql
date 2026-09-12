-- 收货地址表迁移（可重复执行）
SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.TABLES
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user_address') = 0,
  'CREATE TABLE user_address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT ''主键'',
    user_id BIGINT NOT NULL COMMENT ''用户ID'',
    receiver VARCHAR(50) NOT NULL COMMENT ''收货人'',
    phone VARCHAR(20) NOT NULL COMMENT ''手机号'',
    address VARCHAR(255) NOT NULL COMMENT ''详细地址'',
    is_default TINYINT DEFAULT 0 COMMENT ''是否默认 1是 0否'',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
    KEY idx_user_id (user_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''用户收货地址''',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
