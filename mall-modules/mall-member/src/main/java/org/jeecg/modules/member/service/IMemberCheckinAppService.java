package org.jeecg.modules.member.service;

import org.jeecg.modules.member.entity.FdCheckinConfig;

import java.util.List;
import java.util.Map;

public interface IMemberCheckinAppService {

    List<FdCheckinConfig> listActiveConfig();

    Map<String, Object> getTodayStatus(Long userId);

    Map<String, Object> doCheckin(Long userId);
}
