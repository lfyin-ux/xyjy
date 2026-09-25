-- 个人认证人脸核身每日次数限制
CREATE TABLE IF NOT EXISTS personal_verify_daily (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  verify_date DATE NOT NULL COMMENT '核身日期',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_date (user_id, verify_date)
) COMMENT '个人认证每日核身记录';
