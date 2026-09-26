package org.jeecg.modules.member.service;

import org.jeecg.modules.member.entity.FdMemberOrder;

public interface IMemberPaymentAppService {

    FdMemberOrder confirmPaid(String orderNo);

    FdMemberOrder confirmPaidFromPayment(String orderNo, String externalTradeNo,
                                         String payChannel, String externalPayload);
}
