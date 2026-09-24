package com.mall.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.api.dto.*;
import com.mall.api.util.MallUserUtils;
import com.mall.cdk.service.MallCdkService;
import com.mall.common.constant.MallOrderType;
import com.mall.delivery.service.MallDeliveryService;
import com.mall.order.entity.MallOrder;
import com.mall.order.service.MallOrderService;
import com.mall.product.entity.MallProduct;
import com.mall.product.mapper.MallProductMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "ChatGPT-C端")
@RestController
@RequestMapping("/mall/chatgpt")
@RequiredArgsConstructor
public class ChatgptCdkController {

    private final MallCdkService cdkService;
    private final MallOrderService orderService;
    private final MallDeliveryService deliveryService;
    private final MallProductMapper productMapper;

    @Operation(summary = "读取本站登录状态")
    @GetMapping("/login/status")
    public Result<Map<String, Object>> loginStatus() {
        Map<String, Object> map = new HashMap<>();
        map.put("loggedIn", MallUserUtils.isLoggedIn());
        map.put("userId", MallUserUtils.currentUserId());
        map.put("username", MallUserUtils.currentUsername());
        map.put("tip", MallUserUtils.isLoggedIn()
                ? "已读取本站登录状态"
                : "当前为演示模式（未登录），将使用 demo 用户");
        return Result.OK(map);
    }

    @Operation(summary = "上架商品列表（商城）")
    @GetMapping("/product/list")
    public Result<List<MallProduct>> productList() {
        return Result.OK(productMapper.selectList(new LambdaQueryWrapper<MallProduct>()
                .eq(MallProduct::getStatus, "ON")
                .orderByAsc(MallProduct::getPrice)));
    }

    @Operation(summary = "下单购买（支付占位，立即发第1期卡密）")
    @PostMapping("/order/create")
    public Result<OrderDetailVO> createSale(@RequestBody CreateSaleOrderRequest req) {
        return Result.OK(orderService.createSaleOrder(req.getProductId(), MallUserUtils.currentUserId()));
    }

    @Operation(summary = "验证卡密")
    @PostMapping("/cdk/verify")
    public Result<CdkVerifyVO> verify(@RequestBody CdkVerifyRequest req) {
        return Result.OK(cdkService.verify(req.getCode()));
    }

    @Operation(summary = "激活站：确认兑换并创建交付任务")
    @PostMapping("/order/redeem")
    public Result<RedeemResultVO> redeem(@RequestBody CdkRedeemRequest req) {
        return Result.OK(orderService.redeem(req.getCode(), MallUserUtils.currentUserId()));
    }

    @Operation(summary = "查询交付进度")
    @GetMapping("/delivery/status/{orderNo}")
    public Result<DeliveryStatusVO> deliveryStatus(@PathVariable String orderNo) {
        return Result.OK(deliveryService.statusByOrderNo(orderNo));
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/order/list")
    public Result<IPage<MallOrder>> orderList(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "orderType", required = false) String orderType,
            @RequestParam(name = "pageNo", defaultValue = "1") long pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") long pageSize) {
        // 商城默认看售卖单
        if (orderType == null || orderType.isEmpty()) {
            orderType = MallOrderType.SALE;
        }
        return Result.OK(orderService.pageByUser(MallUserUtils.currentUserId(), category, orderType, pageNo, pageSize));
    }

    @Operation(summary = "订单详情（含分期卡密）")
    @GetMapping("/order/{orderNo}")
    public Result<OrderDetailVO> orderDetail(@PathVariable String orderNo) {
        return Result.OK(orderService.detailVo(orderNo, MallUserUtils.currentUserId()));
    }

    @Operation(summary = "申请退款")
    @PostMapping("/order/{orderNo}/refund")
    public Result<String> refund(@PathVariable String orderNo) {
        orderService.applyRefund(orderNo, MallUserUtils.currentUserId());
        return Result.OK("已提交退款申请");
    }
}
