package org.jeecg.modules.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.entity.FdMemberSubscription;
import org.jeecg.modules.member.service.IFdMemberPlanService;
import org.jeecg.modules.member.service.IFdMemberSubscriptionService;
import org.jeecg.modules.member.service.IMemberAppService;
import org.jeecg.modules.member.service.IMemberPaymentAppService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemberAppServiceImpl implements IMemberAppService {

    @Resource
    private IFdMemberPlanService planService;
    @Resource
    private IFdMemberSubscriptionService subscriptionService;
    @Resource
    private IMemberPaymentAppService paymentAppService;

    @Override
    public List<FdMemberPlan> listAvailablePlans(String platform) {
        LambdaQueryWrapper<FdMemberPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMemberPlan::getStatus, 1).orderByAsc(FdMemberPlan::getSortNo);
        if (platform != null && !platform.isBlank()) {
            wrapper.and(w -> w.eq(FdMemberPlan::getPlatformScope, "ALL")
                    .or().eq(FdMemberPlan::getPlatformScope, platform.toUpperCase()));
        }
        return planService.list(wrapper);
    }

    @Override
    public Map<String, Object> getMemberStatus(Long userId) {
        long now = System.currentTimeMillis();
        LambdaQueryWrapper<FdMemberSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FdMemberSubscription::getUserId, userId)
                .eq(FdMemberSubscription::getStatus, 1)
                .gt(FdMemberSubscription::getExpireTime, now)
                .orderByDesc(FdMemberSubscription::getExpireTime)
                .last("LIMIT 1");
        FdMemberSubscription sub = subscriptionService.getOne(wrapper, false);
        Map<String, Object> data = new HashMap<>();
        data.put("isMember", sub != null);
        data.put("subscription", sub);
        return data;
    }

    @Override
    public FdMemberOrder confirmPayIfAllowed(String orderNo, boolean allowClientConfirm) {
        if (!allowClientConfirm) {
            throw new JeecgBootException("请完成支付流程");
        }
        return paymentAppService.confirmPaid(orderNo);
    }
}
