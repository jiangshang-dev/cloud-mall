package org.jeecg.modules.member.service;

import org.jeecg.modules.member.entity.FdMemberOrder;

public interface IMemberOrderAppService {
    FdMemberOrder createOrder(Long userId, Long planId, String payChannel, String clientPlatform);
}
