package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.jeecg.modules.member.entity.FdPointsAccount;
import org.jeecg.modules.member.entity.FdPointsLedger;
import org.jeecg.modules.member.entity.FdPointsTask;
import org.jeecg.modules.member.entity.FdPointsTaskRecord;
import org.jeecg.modules.member.service.IFdPointsAccountService;
import org.jeecg.modules.member.service.IFdPointsLedgerService;
import org.jeecg.modules.member.service.IFdPointsTaskRecordService;
import org.jeecg.modules.member.service.IFdPointsTaskService;
import org.jeecg.modules.member.service.IPointsAppService;
import org.jeecg.modules.member.service.IPointsTaskAppService;
import org.jeecg.modules.member.vo.PointsTaskProgressVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointsAppServiceImpl implements IPointsAppService {

    @Resource
    private IFdPointsAccountService accountService;
    @Resource
    private IFdPointsLedgerService ledgerService;
    @Resource
    private IFdPointsTaskService taskService;
    @Resource
    private IFdPointsTaskRecordService taskRecordService;
    @Resource
    private IPointsTaskAppService pointsTaskAppService;

    @Override
    public FdPointsAccount getAccountSummary(Long userId) {
        LambdaQueryWrapper<FdPointsAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdPointsAccount::getUserId, userId);
        FdPointsAccount account = accountService.getOne(wrapper, false);
        if (account == null) {
            account = new FdPointsAccount();
            account.setUserId(userId);
            account.setBalance(0);
            account.setFrozen(0);
            account.setTotalEarned(0);
            account.setTotalSpent(0);
        }
        return account;
    }

    @Override
    public IPage<FdPointsLedger> pageLedger(Long userId, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdPointsLedger> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdPointsLedger::getUserId, userId).orderByDesc(FdPointsLedger::getCreateTime);
        return ledgerService.page(new Page<>(pageNo, pageSize), wrapper);
    }

    @Override
    public List<FdPointsTask> listActiveTasks() {
        LambdaQueryWrapper<FdPointsTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdPointsTask::getStatus, 1).orderByAsc(FdPointsTask::getSortNo);
        return taskService.list(wrapper);
    }

    @Override
    public List<PointsTaskProgressVO> listMyTasks(Long userId) {
        LocalDate today = LocalDate.now();
        List<PointsTaskProgressVO> result = new ArrayList<>();
        for (FdPointsTask task : listActiveTasks()) {
            long todayCount = taskRecordService.count(new LambdaQueryWrapper<FdPointsTaskRecord>()
                    .eq(FdPointsTaskRecord::getUserId, userId)
                    .eq(FdPointsTaskRecord::getTaskId, task.getId())
                    .eq(FdPointsTaskRecord::getCompleteDate, today));
            long totalCount = taskRecordService.count(new LambdaQueryWrapper<FdPointsTaskRecord>()
                    .eq(FdPointsTaskRecord::getUserId, userId)
                    .eq(FdPointsTaskRecord::getTaskId, task.getId()));
            boolean dailyDone = task.getDailyLimit() != null && task.getDailyLimit() > 0 && todayCount >= task.getDailyLimit();
            boolean totalDone = task.getTotalLimit() != null && task.getTotalLimit() > 0 && totalCount >= task.getTotalLimit();
            PointsTaskProgressVO item = new PointsTaskProgressVO();
            item.setId(task.getId());
            item.setTaskCode(task.getTaskCode());
            item.setTitle(task.getTitle());
            item.setDescription(task.getDescription());
            item.setRewardPoints(task.getRewardPoints());
            item.setActionType(task.getActionType());
            item.setDailyLimit(task.getDailyLimit());
            item.setTotalLimit(task.getTotalLimit());
            item.setTodayCount((int) todayCount);
            item.setTotalCount((int) totalCount);
            item.setFinished(dailyDone || totalDone);
            result.add(item);
        }
        return result;
    }

    @Override
    public Map<String, Object> completeTask(Long userId, String taskCode, String bizRef) {
        int balance = pointsTaskAppService.completeTask(userId, taskCode, bizRef);
        Map<String, Object> data = new HashMap<>();
        data.put("balanceAfter", balance);
        return data;
    }
}
