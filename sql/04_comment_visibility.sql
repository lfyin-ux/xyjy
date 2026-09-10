-- 评论可见范围字段迁移（可重复执行）
SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'post_comment' AND COLUMN_NAME = 'visibility') = 0,
  'ALTER TABLE post_comment ADD COLUMN visibility TINYINT DEFAULT 3 COMMENT ''可见范围 1发布人可见 2回复人可见 3全部可见'' AFTER content',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'post_comment' AND COLUMN_NAME = 'reply_to_user_id') = 0,
  'ALTER TABLE post_comment ADD COLUMN reply_to_user_id BIGINT COMMENT ''回复对象用户ID visibility=2时必填'' AFTER visibility',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'post_comment' AND COLUMN_NAME = 'parent_id') = 0,
  'ALTER TABLE post_comment ADD COLUMN parent_id BIGINT COMMENT ''父评论ID'' AFTER reply_to_user_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE post_comment SET visibility = 3 WHERE visibility IS NULL;
