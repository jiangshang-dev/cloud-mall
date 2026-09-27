-- 发布菜谱积分任务，以及签到奖励缺省数据（可重复执行）
-- 依赖 fd_points_task、fd_checkin_config 已建表
-- 积分数量可在管理后台「积分任务」「签到规则」里改

INSERT INTO `fd_checkin_config` (`id`, `day_index`, `reward_points`, `reward_growth`, `status`, `create_by`, `create_time`, `del_flag`)
SELECT 3002000000000000001, 1, 5, 2, 1, 'admin', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `fd_checkin_config` WHERE `day_index` = 1 AND `del_flag` = 0);
INSERT INTO `fd_checkin_config` (`id`, `day_index`, `reward_points`, `reward_growth`, `status`, `create_by`, `create_time`, `del_flag`)
SELECT 3002000000000000002, 2, 8, 3, 1, 'admin', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `fd_checkin_config` WHERE `day_index` = 2 AND `del_flag` = 0);
INSERT INTO `fd_checkin_config` (`id`, `day_index`, `reward_points`, `reward_growth`, `status`, `create_by`, `create_time`, `del_flag`)
SELECT 3002000000000000003, 3, 10, 5, 1, 'admin', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `fd_checkin_config` WHERE `day_index` = 3 AND `del_flag` = 0);
INSERT INTO `fd_checkin_config` (`id`, `day_index`, `reward_points`, `reward_growth`, `status`, `create_by`, `create_time`, `del_flag`)
SELECT 3002000000000000004, 4, 12, 5, 1, 'admin', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `fd_checkin_config` WHERE `day_index` = 4 AND `del_flag` = 0);
INSERT INTO `fd_checkin_config` (`id`, `day_index`, `reward_points`, `reward_growth`, `status`, `create_by`, `create_time`, `del_flag`)
SELECT 3002000000000000005, 5, 15, 8, 1, 'admin', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `fd_checkin_config` WHERE `day_index` = 5 AND `del_flag` = 0);
INSERT INTO `fd_checkin_config` (`id`, `day_index`, `reward_points`, `reward_growth`, `status`, `create_by`, `create_time`, `del_flag`)
SELECT 3002000000000000006, 6, 18, 8, 1, 'admin', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `fd_checkin_config` WHERE `day_index` = 6 AND `del_flag` = 0);
INSERT INTO `fd_checkin_config` (`id`, `day_index`, `reward_points`, `reward_growth`, `status`, `create_by`, `create_time`, `del_flag`)
SELECT 3002000000000000007, 7, 30, 15, 1, 'admin', NOW(), 0 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `fd_checkin_config` WHERE `day_index` = 7 AND `del_flag` = 0);

INSERT INTO `fd_points_task` (
    `id`, `task_code`, `title`, `description`, `reward_points`,
    `action_type`, `daily_limit`, `total_limit`, `sort_no`, `status`,
    `create_by`, `create_time`, `del_flag`
)
SELECT 3003000000000000006, 'PUBLISH_RECIPE', '发布菜谱', '成功发布一道菜谱', 30,
       'PUBLISH_RECIPE', 3, 0, 6, 1, 'admin', NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `fd_points_task` WHERE `task_code` = 'PUBLISH_RECIPE' AND `del_flag` = 0
);
