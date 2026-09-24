-- 游戏组局增加联系方式
SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'game_team' AND COLUMN_NAME = 'creator_contact') = 0,
  'ALTER TABLE game_team ADD COLUMN creator_contact VARCHAR(100) COMMENT ''发起人联系方式'' AFTER require_desc',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'game_team_member' AND COLUMN_NAME = 'member_contact') = 0,
  'ALTER TABLE game_team_member ADD COLUMN member_contact VARCHAR(100) COMMENT ''成员联系方式'' AFTER user_id',
  'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
