package com.mall.web.controller.member.app;

import com.alibaba.fastjson.JSONObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.member.service.IMemberInternalPaymentService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "App会员-支付内部")
@RestController
@RequestMapping("/member/internal")
public class MemberInternalPaymentController {

    public static final String INTERNAL_SECRET_HEADER = "X-Payment-Internal-Secret";

    @Resource
    private IMemberInternalPaymentService internalPaymentService;

    @GetMapping("/order")
    public Result<JSONObject> getOrder(@RequestParam String orderNo,
                                       @RequestHeader(INTERNAL_SECRET_HEADER) String secret) {
        return Result.OK(internalPaymentService.getOrderDetail(orderNo, secret));
    }

    @PostMapping("/order/fulfill")
    public Result<JSONObject> fulfill(@RequestParam String orderNo,
                                      @RequestParam String externalTradeNo,
                                      @RequestParam String payChannel,
                                      @RequestParam(required = false) String externalPayload,
                                      @RequestHeader(INTERNAL_SECRET_HEADER) String secret) {
        return Result.OK(internalPaymentService.fulfillOrder(
                orderNo, externalTradeNo, payChannel, externalPayload, secret));
    }
}
