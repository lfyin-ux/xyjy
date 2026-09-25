-- 头像、简介不再人工审核，历史数据统一标记为已通过
UPDATE app_user SET avatar_audit_status = 1 WHERE avatar_audit_status IS NULL OR avatar_audit_status = 0;
UPDATE app_user SET intro_audit_status = 1 WHERE intro_audit_status IS NULL OR intro_audit_status = 0;
