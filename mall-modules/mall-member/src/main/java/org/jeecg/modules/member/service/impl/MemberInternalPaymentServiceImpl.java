package org.jeecg.modules.member.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.member.config.FondiaMemberPaymentProperties;
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.service.IFdMemberOrderService;
import org.jeecg.modules.member.service.IFdMemberPlanService;
import org.jeecg.modules.member.service.IMemberInternalPaymentService;
import org.jeecg.modules.member.service.IMemberPaymentAppService;
import org.springframework.stereotype.Service;

@Service
public class MemberInternalPaymentServiceImpl implements IMemberInternalPaymentService {

    @Resource
    private FondiaMemberPaymentProperties paymentProperties;
    @Resource
    private IFdMemberOrderService orderService;
    @Resource
    private IFdMemberPlanService planService;
    @Resource
    private IMemberPaymentAppService paymentAppService;

    @Override
    public JSONObject getOrderDetail(String orderNo, String secret) {
        checkSecret(secret);
        FdMemberOrder order = findOrder(orderNo);
        FdMemberPlan plan = planService.getById(order.getPlanId());
        JSONObject data = (JSONObject) JSONObject.toJSON(order);
        if (plan != null) {
            data.put("appleProductId", plan.getAppleProductId());
            data.put("googleProductId", plan.getGoogleProductId());
        }
        return data;
    }

    @Override
    public JSONObject fulfillOrder(String orderNo, String externalTradeNo, String payChannel,
                                   String externalPayload, String secret) {
        checkSecret(secret);
        FdMemberOrder order = paymentAppService.confirmPaidFromPayment(
                orderNo, externalTradeNo, payChannel, externalPayload);
        return (JSONObject) JSONObject.toJSON(order);
    }

    private void checkSecret(String secret) {
        if (oConvertUtils.isEmpty(secret)
                || !secret.equals(paymentProperties.getInternalSecret())) {
            throw new JeecgBootException("非法的内部调用");
        }
    }

    private FdMemberOrder findOrder(String orderNo) {
        FdMemberOrder order = orderService.getOne(new LambdaQueryWrapper<FdMemberOrder>()
                .eq(FdMemberOrder::getOrderNo, orderNo), false);
        if (order == null) {
            throw new JeecgBootException("订单不存在");
        }
        return order;
    }
}
