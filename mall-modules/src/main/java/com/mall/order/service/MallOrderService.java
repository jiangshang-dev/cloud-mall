package com.mall.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.api.dto.OrderDetailVO;
import com.mall.api.dto.RedeemResultVO;
import com.mall.cdk.entity.MallCdk;
import com.mall.cdk.service.MallCdkService;
import com.mall.common.constant.MallOrderStatus;
import com.mall.common.constant.MallOrderType;
import com.mall.delivery.service.MallDeliveryService;
import com.mall.order.entity.MallOrder;
import com.mall.order.entity.MallOrderInstallment;
import com.mall.order.mapper.MallOrderInstallmentMapper;
import com.mall.order.mapper.MallOrderMapper;
import com.mall.product.entity.MallProduct;
import com.mall.product.mapper.MallProductMapper;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MallOrderService extends ServiceImpl<MallOrderMapper, MallOrder> {

    private final MallCdkService cdkService;
    private final MallProductMapper productMapper;
    private final MallDeliveryService deliveryService;
    private final MallOrderInstallmentMapper installmentMapper;

    /**
     * 商城下单（支付占位直接成功）→ 创建分期计划 → 立即发放第 1 期卡密。
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderDetailVO createSaleOrder(String productId, String userId) {
        MallProduct product = productMapper.selectById(productId);
        if (product == null || !"ON".equals(product.getStatus())) {
            throw new JeecgBootException("商品不存在或已下架");
        }
        int periods = product.getPeriods() == null || product.getPeriods() < 1 ? 1 : product.getPeriods();

        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        Date now = new Date();
        MallOrder order = new MallOrder()
                .setOrderNo(orderNo)
                .setOrderType(MallOrderType.SALE)
                .setUserId(userId)
                .setProductId(product.getId())
                .setProductName(product.getName())
                .setProductType(product.getProductType())
                .setCategory(product.getCategory())
                .setAmount(product.getPrice())
                .setStatus(MallOrderStatus.PAID)
                .setPeriods(periods)
                .setIssuedPeriods(0)
                .setCreateTime(now);
        save(order);

        List<MallOrderInstallment> installments = new ArrayList<>();
        for (int i = 1; i <= periods; i++) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            cal.add(Calendar.DAY_OF_MONTH, 30 * (i - 1));
            MallOrderInstallment inst = new MallOrderInstallment()
                    .setOrderId(order.getId())
                    .setOrderNo(orderNo)
                    .setPeriodNo(i)
                    .setStatus("PENDING")
                    .setExpectTime(cal.getTime())
                    .setRemark("系统自动发货")
                    .setCreateTime(now);
            installmentMapper.insert(inst);
            installments.add(inst);
        }

        // 立即发放第 1 期
        issuePeriod(order, installments.get(0), userId);
        order.setIssuedPeriods(1);
        order.setInstallmentTip("分期发放：已完成 1/" + periods + " 期");
        order.setCdkCode(installments.get(0).getCdkCode());
        order.setCdkId(installments.get(0).getCdkId());
        order.setUpdateTime(new Date());
        updateById(order);

        return buildDetail(order, product);
    }

    private void issuePeriod(MallOrder order, MallOrderInstallment inst, String userId) {
        MallCdk cdk = cdkService.pickUnused(order.getProductId());
        if (cdk == null) {
            throw new JeecgBootException("库存卡密不足，请稍后重试或联系客服");
        }
        int n = cdkService.reserveById(cdk.getId(), userId);
        if (n != 1) {
            throw new JeecgBootException("卡密锁定失败，请重试");
        }
        cdk = cdkService.getById(cdk.getId());
        Date now = new Date();
        inst.setCdkId(cdk.getId());
        inst.setCdkCode(cdk.getCode());
        inst.setStatus("ISSUED");
        inst.setIssueTime(now);
        inst.setUpdateTime(now);
        installmentMapper.updateById(inst);
    }

    @Transactional(rollbackFor = Exception.class)
    public RedeemResultVO redeem(String rawCode, String userId) {
        String code = MallCdkService.normalize(rawCode);
        cdkService.verify(code);

        int occupied = cdkService.occupy(code, userId);
        if (occupied != 1) {
            throw new JeecgBootException("卡密已被使用或不存在，请重试");
        }

        MallCdk cdk = cdkService.getByCode(code);
        MallProduct product = productMapper.selectById(cdk.getProductId());

        String orderNo = "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        MallOrder order = new MallOrder()
                .setOrderNo(orderNo)
                .setOrderType(MallOrderType.ACTIVATE)
                .setUserId(userId)
                .setProductId(product.getId())
                .setProductName(product.getName())
                .setProductType(product.getProductType())
                .setCategory(product.getCategory())
                .setCdkId(cdk.getId())
                .setCdkCode(code)
                .setAmount(product.getPrice())
                .setStatus(MallOrderStatus.PAID)
                .setPeriods(1)
                .setIssuedPeriods(1)
                .setCreateTime(new Date());
        save(order);

        // 标记售卖订单分期为已激活
        MallOrderInstallment shipped = installmentMapper.selectOne(new LambdaQueryWrapper<MallOrderInstallment>()
                .eq(MallOrderInstallment::getCdkCode, code)
                .eq(MallOrderInstallment::getStatus, "ISSUED")
                .last("LIMIT 1"), false);
        if (shipped != null) {
            shipped.setStatus("ACTIVATED");
            shipped.setUpdateTime(new Date());
            installmentMapper.updateById(shipped);
        }

        deliveryService.createTask(order);

        return new RedeemResultVO()
                .setOrderId(order.getId())
                .setOrderNo(orderNo)
                .setStatus(order.getStatus())
                .setMessage("激活提交成功，已创建交付任务（占位）");
    }

    public IPage<MallOrder> pageByUser(String userId, String category, String orderType, long pageNo, long pageSize) {
        LambdaQueryWrapper<MallOrder> q = new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getUserId, userId)
                .orderByDesc(MallOrder::getCreateTime);
        if (oConvertUtils.isNotEmpty(category)) {
            q.eq(MallOrder::getCategory, category);
        }
        if (oConvertUtils.isNotEmpty(orderType)) {
            q.eq(MallOrder::getOrderType, orderType);
        }
        return page(new Page<>(pageNo, pageSize), q);
    }

    public OrderDetailVO detailVo(String orderNo, String userId) {
        MallOrder order = detail(orderNo, userId);
        MallProduct product = productMapper.selectById(order.getProductId());
        return buildDetail(order, product);
    }

    public MallOrder detail(String orderNo, String userId) {
        MallOrder order = getOne(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getOrderNo, orderNo)
                .eq(MallOrder::getUserId, userId), false);
        if (order == null) {
            throw new JeecgBootException("订单不存在");
        }
        return order;
    }

    private OrderDetailVO buildDetail(MallOrder order, MallProduct product) {
        List<MallOrderInstallment> list = installmentMapper.selectList(new LambdaQueryWrapper<MallOrderInstallment>()
                .eq(MallOrderInstallment::getOrderId, order.getId())
                .orderByAsc(MallOrderInstallment::getPeriodNo));
        String redeemUrl = product != null && oConvertUtils.isNotEmpty(product.getRedeemUrl())
                ? product.getRedeemUrl()
                : "http://127.0.0.1:5173/recharge";
        String nextTip = null;
        if (order.getPeriods() != null && order.getIssuedPeriods() != null
                && order.getIssuedPeriods() < order.getPeriods()) {
            MallOrderInstallment next = list.stream()
                    .filter(i -> "PENDING".equals(i.getStatus()))
                    .findFirst().orElse(null);
            if (next != null && next.getExpectTime() != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy/M/d");
                nextTip = "下一期预计 " + sdf.format(next.getExpectTime());
            }
        }
        return new OrderDetailVO()
                .setOrder(order)
                .setInstallments(list)
                .setRedeemUrl(redeemUrl)
                .setNextExpectTip(nextTip);
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(String orderNo, String userId) {
        MallOrder order = detail(orderNo, userId);
        if (MallOrderStatus.REFUNDED.equals(order.getStatus())
                || MallOrderStatus.REFUNDING.equals(order.getStatus())
                || MallOrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new JeecgBootException("当前状态不可退款");
        }
        order.setStatus(MallOrderStatus.REFUNDING);
        order.setUpdateTime(new Date());
        updateById(order);
    }
}
