-- 用户关注表
CREATE TABLE IF NOT EXISTS user_follow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  user_id BIGINT NOT NULL COMMENT '关注者用户ID',
  target_id BIGINT NOT NULL COMMENT '被关注用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  UNIQUE KEY uk_user_target (user_id, target_id),
  KEY idx_user_id (user_id),
  KEY idx_target_id (target_id)
) ENGINE=InnoDB COMMENT='用户关注表';
