CREATE TABLE IF NOT EXISTS `sys_app_setting` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(64) NOT NULL,
  `config_value` varchar(512) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
