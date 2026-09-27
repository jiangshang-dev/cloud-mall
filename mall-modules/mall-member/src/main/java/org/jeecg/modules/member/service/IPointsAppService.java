package org.jeecg.modules.member.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.modules.member.entity.FdPointsAccount;
import org.jeecg.modules.member.entity.FdPointsLedger;
import org.jeecg.modules.member.entity.FdPointsTask;
import org.jeecg.modules.member.vo.PointsTaskProgressVO;

import java.util.List;
import java.util.Map;

public interface IPointsAppService {

    FdPointsAccount getAccountSummary(Long userId);

    IPage<FdPointsLedger> pageLedger(Long userId, Integer pageNo, Integer pageSize);

    List<FdPointsTask> listActiveTasks();

    List<PointsTaskProgressVO> listMyTasks(Long userId);

    Map<String, Object> completeTask(Long userId, String taskCode, String bizRef);
}
