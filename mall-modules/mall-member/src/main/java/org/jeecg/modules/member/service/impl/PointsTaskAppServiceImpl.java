package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.entity.FdPointsTask;
import org.jeecg.modules.member.entity.FdPointsTaskRecord;
import org.jeecg.modules.member.service.IFdPointsTaskRecordService;
import org.jeecg.modules.member.service.IFdPointsTaskService;
import org.jeecg.modules.member.service.IPointsTaskAppService;
import org.jeecg.modules.member.service.impl.PointsCoreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class PointsTaskAppServiceImpl implements IPointsTaskAppService {

    @Resource
    private IFdPointsTaskService taskService;
    @Resource
    private IFdPointsTaskRecordService taskRecordService;
    @Resource
    private PointsCoreService pointsCoreService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeTask(Long userId, String taskCode, String bizRef) {
        LambdaQueryWrapper<FdPointsTask> taskWrapper = new LambdaQueryWrapper<>();
        taskWrapper.eq(FdPointsTask::getTaskCode, taskCode).eq(FdPointsTask::getStatus, 1);
        FdPointsTask task = taskService.getOne(taskWrapper, false);
        if (task == null) {
            throw new JeecgBootException("任务不存在或已禁用");
        }

        LocalDate today = LocalDate.now();
        if (task.getDailyLimit() != null && task.getDailyLimit() > 0) {
            LambdaQueryWrapper<FdPointsTaskRecord> dailyWrapper = new LambdaQueryWrapper<>();
            dailyWrapper.eq(FdPointsTaskRecord::getUserId, userId)
                    .eq(FdPointsTaskRecord::getTaskId, task.getId())
                    .eq(FdPointsTaskRecord::getCompleteDate, today);
            if (taskRecordService.count(dailyWrapper) >= task.getDailyLimit()) {
                throw new JeecgBootException("今日任务已达上限");
            }
        }

        if (task.getTotalLimit() != null && task.getTotalLimit() > 0) {
            LambdaQueryWrapper<FdPointsTaskRecord> totalWrapper = new LambdaQueryWrapper<>();
            totalWrapper.eq(FdPointsTaskRecord::getUserId, userId).eq(FdPointsTaskRecord::getTaskId, task.getId());
            if (taskRecordService.count(totalWrapper) >= task.getTotalLimit()) {
                throw new JeecgBootException("任务总次数已达上限");
            }
        }

        if (oConvertUtils.isNotEmpty(bizRef)) {
            LambdaQueryWrapper<FdPointsTaskRecord> bizWrapper = new LambdaQueryWrapper<>();
            bizWrapper.eq(FdPointsTaskRecord::getUserId, userId)
                    .eq(FdPointsTaskRecord::getTaskId, task.getId())
                    .eq(FdPointsTaskRecord::getBizRef, bizRef);
            if (taskRecordService.count(bizWrapper) > 0) {
                throw new JeecgBootException("该任务已发放");
            }
        }

        long now = System.currentTimeMillis();
        FdPointsTaskRecord record = new FdPointsTaskRecord();
        record.setUserId(userId);
        record.setTaskId(task.getId());
        record.setTaskCode(task.getTaskCode());
        record.setCompleteDate(today);
        record.setRewardPoints(task.getRewardPoints());
        record.setBizRef(bizRef);
        record.setCreateTime(now);
        taskRecordService.save(record);

        return pointsCoreService.addPoints(userId, task.getRewardPoints(), "TASK", taskCode, task.getTitle());
    }

    @Override
    public int tryReward(Long userId, String taskCode, String bizRef) {
        if (userId == null || oConvertUtils.isEmpty(taskCode)) {
            return 0;
        }
        try {
            int before = pointsCoreService.getBalance(userId);
            int after = completeTask(userId, taskCode, bizRef);
            return Math.max(0, after - before);
        } catch (JeecgBootException e) {
            log.info("跳过积分任务 taskCode={} userId={} : {}", taskCode, userId, e.getMessage());
            return 0;
        }
    }
}
