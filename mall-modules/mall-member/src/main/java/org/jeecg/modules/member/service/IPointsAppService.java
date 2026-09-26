package org.jeecg.modules.member.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.member.entity.FdPointsAccount;
import org.jeecg.modules.member.entity.FdPointsLedger;
import org.jeecg.modules.member.entity.FdPointsTask;

import java.util.List;
import java.util.Map;

public interface IPointsAppService {

    FdPointsAccount getAccountSummary(Long userId);

    IPage<FdPointsLedger> pageLedger(Long userId, Integer pageNo, Integer pageSize);

    List<FdPointsTask> listActiveTasks();

    Map<String, Object> completeTask(Long userId, String taskCode, String bizRef);
}
