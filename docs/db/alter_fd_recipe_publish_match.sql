-- 发布菜谱与详情页字段对齐（已有库执行，可重复执行）
-- 新库请同步 mall-cuisine/src/main/resources/db/sql/init_recipe_tables.sql

DROP PROCEDURE IF EXISTS fd_add_col;
DELIMITER $$
CREATE PROCEDURE fd_add_col(IN tbl VARCHAR(64), IN col VARCHAR(64), IN ddl TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = tbl AND COLUMN_NAME = col
    ) THEN
        SET @fd_ddl = ddl;
        PREPARE fd_stmt FROM @fd_ddl;
        EXECUTE fd_stmt;
        DEALLOCATE PREPARE fd_stmt;
    END IF;
END$$
DELIMITER ;

CALL fd_add_col('fd_recipe', 'yield_count',
    'ALTER TABLE `fd_recipe` ADD COLUMN `yield_count` varchar(50) DEFAULT NULL COMMENT ''出品数量，如2份'' AFTER `serving_size`');
CALL fd_add_col('fd_recipe', 'show_calories',
    'ALTER TABLE `fd_recipe` ADD COLUMN `show_calories` tinyint NOT NULL DEFAULT 0 COMMENT ''是否展示热量：0否 1是'' AFTER `calories`');
CALL fd_add_col('fd_recipe', 'private_only',
    'ALTER TABLE `fd_recipe` ADD COLUMN `private_only` tinyint NOT NULL DEFAULT 0 COMMENT ''仅自己可见：0否 1是'' AFTER `status`');
CALL fd_add_col('fd_recipe', 'prep_note',
    'ALTER TABLE `fd_recipe` ADD COLUMN `prep_note` varchar(50) DEFAULT NULL COMMENT ''烹饪时长区间，如15~30分钟'' AFTER `cook_minutes`');
CALL fd_add_col('fd_recipe', 'skill_level',
    'ALTER TABLE `fd_recipe` ADD COLUMN `skill_level` varchar(30) DEFAULT NULL COMMENT ''操作难度，如容易做'' AFTER `difficulty`');

CALL fd_add_col('fd_recipe_ingredient', 'unit',
    'ALTER TABLE `fd_recipe_ingredient` ADD COLUMN `unit` varchar(20) DEFAULT NULL COMMENT ''用量单位'' AFTER `amount`');

CALL fd_add_col('fd_recipe_step', 'title',
    'ALTER TABLE `fd_recipe_step` ADD COLUMN `title` varchar(100) DEFAULT NULL COMMENT ''步骤标题'' AFTER `step_no`');

DROP PROCEDURE IF EXISTS fd_add_col;

ALTER TABLE `fd_recipe`
    MODIFY COLUMN `video_url` varchar(1000) DEFAULT NULL COMMENT '视频地址或阿里云VOD视频ID';

ALTER TABLE `fd_recipe_step`
    MODIFY COLUMN `image` text COMMENT '步骤图片，多张用||分隔',
    MODIFY COLUMN `content` varchar(2000) NOT NULL DEFAULT '' COMMENT '步骤说明';
