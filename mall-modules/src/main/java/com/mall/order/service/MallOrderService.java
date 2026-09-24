package com.mall.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.api.dto.RedeemResultVO;
import com.mall.cdk.entity.MallCdk;
import com.mall.cdk.service.MallCdkService;
import com.mall.common.constant.MallOrderStatus;
import com.mall.delivery.service.MallDeliveryService;
import com.mall.order.entity.MallOrder;
import com.mall.order.mapper.MallOrderMapper;
import com.mall.product.entity.MallProduct;
import com.mall.product.mapper.MallProductMapper;
import lombok.RequiredArgsConstructor;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MallOrderService extends ServiceImpl<MallOrderMapper, MallOrder> {

    private final MallCdkService cdkService;
    private final MallProductMapper productMapper;
    private final MallDeliveryService deliveryService;

    @Transactional(rollbackFor = Exception.class)
    public RedeemResultVO redeem(String rawCode, String userId) {
        String code = MallCdkService.normalize(rawCode);
        // 先校验商品可用
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
                .setUserId(userId)
                .setProductId(product.getId())
                .setProductName(product.getName())
                .setProductType(product.getProductType())
                .setCategory(product.getCategory())
                .setCdkId(cdk.getId())
                .setCdkCode(code)
                .setAmount(product.getPrice())
                .setStatus(MallOrderStatus.PAID)
                .setInstallmentTip(null)
                .setCreateTime(new Date());
        save(order);

        deliveryService.createTask(order);

        return new RedeemResultVO()
                .setOrderId(order.getId())
                .setOrderNo(orderNo)
                .setStatus(order.getStatus())
                .setMessage("兑换成功，已创建交付任务");
    }

    public IPage<MallOrder> pageByUser(String userId, String category, long pageNo, long pageSize) {
        LambdaQueryWrapper<MallOrder> q = new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getUserId, userId)
                .orderByDesc(MallOrder::getCreateTime);
        if (oConvertUtils.isNotEmpty(category)) {
            q.eq(MallOrder::getCategory, category);
        }
        return page(new Page<>(pageNo, pageSize), q);
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
