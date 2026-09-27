package org.jeecg.modules.member.service;

public interface IPointsTaskAppService {
    int completeTask(Long userId, String taskCode, String bizRef);

    /**
     * 按后台任务配置发放积分。任务关闭、达到上限或同一业务已发放时返回 0。
     */
    int tryReward(Long userId, String taskCode, String bizRef);
}
