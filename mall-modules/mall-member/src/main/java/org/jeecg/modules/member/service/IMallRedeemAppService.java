package org.jeecg.modules.member.service;

import org.jeecg.modules.member.entity.FdMallOrder;

public interface IMallRedeemAppService {
    FdMallOrder redeem(Long userId, Long productId, Integer quantity, Long addressId);
}
