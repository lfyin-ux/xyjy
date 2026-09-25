-- 个人认证增加 E证通人脸核身记录
SET @db := DATABASE();

SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'personal_auth' AND COLUMN_NAME = 'eid_token') = 0,
  'ALTER TABLE personal_auth ADD COLUMN eid_token VARCHAR(128) COMMENT ''E证通Token'' AFTER id_back_img',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'personal_auth' AND COLUMN_NAME = 'face_verified') = 0,
  'ALTER TABLE personal_auth ADD COLUMN face_verified TINYINT DEFAULT 0 COMMENT ''人脸核身是否通过 0否 1是'' AFTER eid_token',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
