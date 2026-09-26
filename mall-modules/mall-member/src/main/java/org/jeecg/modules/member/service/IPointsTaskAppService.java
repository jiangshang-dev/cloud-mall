package org.jeecg.modules.member.service;

public interface IPointsTaskAppService {
    int completeTask(Long userId, String taskCode, String bizRef);
}
