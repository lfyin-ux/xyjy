-- 支持每人每天多次人脸核身记录（每日上限由业务代码控制）
SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'personal_verify_daily' AND INDEX_NAME = 'uk_user_date') > 0,
  'ALTER TABLE personal_verify_daily DROP INDEX uk_user_date',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'personal_verify_daily' AND INDEX_NAME = 'idx_user_date') = 0,
  'ALTER TABLE personal_verify_daily ADD INDEX idx_user_date (user_id, verify_date)',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
