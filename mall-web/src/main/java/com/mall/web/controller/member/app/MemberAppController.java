package com.mall.web.controller.member.app;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.config.FondiaMemberPaymentProperties;
import org.jeecg.modules.member.entity.FdMemberOrder;
import org.jeecg.modules.member.entity.FdMemberPlan;
import org.jeecg.modules.member.service.IMemberAppService;
import org.jeecg.modules.member.service.IMemberOrderAppService;
import org.jeecg.modules.member.util.MemberAuthHelper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "App会员")
@RestController
@RequestMapping("/member")
public class MemberAppController {

    @Resource
    private IMemberAppService memberAppService;
    @Resource
    private IMemberOrderAppService orderAppService;
    @Resource
    private MemberAuthHelper authHelper;
    @Resource
    private FondiaMemberPaymentProperties memberPaymentProperties;

    @Operation(summary = "可购会员套餐列表")
    @GetMapping("/plans")
    public Result<List<FdMemberPlan>> plans(@RequestParam(required = false) String platform) {
        return Result.OK(memberAppService.listAvailablePlans(platform));
    }

    @Operation(summary = "当前用户会员状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> status(@RequestHeader("Authorization") String authorization) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(memberAppService.getMemberStatus(userId));
    }

    @Operation(summary = "创建会员订单")
    @PostMapping("/order/create")
    public Result<FdMemberOrder> createOrder(@RequestHeader("Authorization") String authorization,
                                             @RequestParam Long planId,
                                             @RequestParam(defaultValue = "ALIPAY") String payChannel,
                                             @RequestParam(required = false) String clientPlatform) {
        Long userId = authHelper.resolveUserId(authorization);
        return Result.OK(orderAppService.createOrder(userId, planId, payChannel, clientPlatform));
    }

    @Operation(summary = "支付成功确认（仅开发环境，生产请走 /payment/verify 或渠道回调）")
    @PostMapping("/order/pay/confirm")
    public Result<FdMemberOrder> confirmPay(@RequestHeader("Authorization") String authorization,
                                            @RequestParam String orderNo) {
        authHelper.resolveUserId(authorization);
        return Result.OK(memberAppService.confirmPayIfAllowed(orderNo, memberPaymentProperties.isAllowClientConfirm()));
    }
}
