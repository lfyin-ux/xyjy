-- 违规记录增加已读状态，供小程序端展示平台通知
SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'violation_record' AND COLUMN_NAME = 'read_status') = 0,
  'ALTER TABLE violation_record ADD COLUMN read_status TINYINT DEFAULT 0 COMMENT ''是否已读 0未读 1已读'' AFTER admin_name',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 历史记录默认已读，避免上线后弹出旧通知
UPDATE violation_record SET read_status = 1 WHERE read_status IS NULL OR read_status = 0;
