package org.jeecg.modules.member.service;

import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.entity.FdMemberPlan;

import java.util.List;
import java.util.Map;

public interface IMemberAppService {

    List<FdMemberPlan> listAvailablePlans(String platform);

    Map<String, Object> getMemberStatus(Long userId);

    FdMemberOrder confirmPayIfAllowed(String orderNo, boolean allowClientConfirm);
}
