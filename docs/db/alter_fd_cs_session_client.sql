-- 客服会话记录客户端 IP 和手机设备信息（可重复执行）

DROP PROCEDURE IF EXISTS fd_cs_add_col;
DELIMITER $$
CREATE PROCEDURE fd_cs_add_col(IN tbl VARCHAR(64), IN col VARCHAR(64), IN ddl TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
  ) THEN
    SET @fd_cs_sql = ddl;
    PREPARE fd_cs_stmt FROM @fd_cs_sql;
    EXECUTE fd_cs_stmt;
    DEALLOCATE PREPARE fd_cs_stmt;
  END IF;
END$$
DELIMITER ;

CALL fd_cs_add_col('fd_cs_session', 'client_ip',
  'ALTER TABLE `fd_cs_session` ADD COLUMN `client_ip` varchar(64) DEFAULT NULL COMMENT ''客户端IP'' AFTER `source`');
CALL fd_cs_add_col('fd_cs_session', 'device_info',
  'ALTER TABLE `fd_cs_session` ADD COLUMN `device_info` varchar(200) DEFAULT NULL COMMENT ''手机设备信息'' AFTER `client_ip`');

DROP PROCEDURE IF EXISTS fd_cs_add_col;
