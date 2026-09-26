package org.jeecg.modules.member.service;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.modules.member.entity.FdMemberOrder;

public interface IMemberInternalPaymentService {

    JSONObject getOrderDetail(String orderNo, String secret);

    JSONObject fulfillOrder(String orderNo, String externalTradeNo, String payChannel,
                            String externalPayload, String secret);
}
