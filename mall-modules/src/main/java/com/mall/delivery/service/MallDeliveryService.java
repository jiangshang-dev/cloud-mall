package com.mall.delivery.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mall.api.dto.DeliveryStatusVO;
import com.mall.common.constant.MallDeliveryStatus;
import com.mall.common.constant.MallOrderStatus;
import com.mall.delivery.entity.MallDeliveryTask;
import com.mall.delivery.mapper.MallDeliveryTaskMapper;
import com.mall.delivery.spi.DeliveryContext;
import com.mall.delivery.spi.DeliveryHandler;
import com.mall.order.entity.MallOrder;
import com.mall.order.mapper.MallOrderMapper;
import com.mall.product.entity.MallProduct;
import com.mall.product.mapper.MallProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MallDeliveryService extends ServiceImpl<MallDeliveryTaskMapper, MallDeliveryTask> {

    private final List<DeliveryHandler> handlers;
    private final MallOrderMapper orderMapper;
    private final MallProductMapper productMapper;

    public MallDeliveryTask createTask(MallOrder order) {
        MallDeliveryTask task = new MallDeliveryTask()
                .setOrderId(order.getId())
                .setOrderNo(order.getOrderNo())
                .setStatus(MallDeliveryStatus.PENDING)
                .setProgress(0)
                .setRetryCount(0)
                .setResultMsg("等待交付")
                .setCreateTime(new Date());
        save(task);
        return task;
    }

    public DeliveryStatusVO statusByOrderNo(String orderNo) {
        MallOrder order = orderMapper.selectOne(new LambdaQueryWrapper<MallOrder>()
                .eq(MallOrder::getOrderNo, orderNo), false);
        if (order == null) {
            throw new JeecgBootException("订单不存在");
        }
        MallDeliveryTask task = getOne(new LambdaQueryWrapper<MallDeliveryTask>()
                .eq(MallDeliveryTask::getOrderNo, orderNo)
                .orderByDesc(MallDeliveryTask::getCreateTime)
                .last("LIMIT 1"), false);
        DeliveryStatusVO vo = new DeliveryStatusVO()
                .setOrderNo(orderNo)
                .setOrderStatus(order.getStatus());
        if (task != null) {
            vo.setTaskStatus(task.getStatus())
                    .setProgress(task.getProgress())
                    .setResultMsg(task.getResultMsg());
        }
        return vo;
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional(rollbackFor = Exception.class)
    public void tickPendingTasks() {
        List<MallDeliveryTask> tasks = list(new LambdaQueryWrapper<MallDeliveryTask>()
                .in(MallDeliveryTask::getStatus,
                        MallDeliveryStatus.PENDING,
                        MallDeliveryStatus.RUNNING)
                .last("LIMIT 20"));
        for (MallDeliveryTask task : tasks) {
            try {
                advance(task);
            } catch (Exception e) {
                log.warn("delivery tick failed orderNo={}", task.getOrderNo(), e);
                task.setRetryCount(task.getRetryCount() == null ? 1 : task.getRetryCount() + 1);
                if (task.getRetryCount() >= 5) {
                    task.setStatus(MallDeliveryStatus.FAILED);
                    task.setResultMsg("交付失败（占位）：" + e.getMessage());
                    MallOrder order = orderMapper.selectById(task.getOrderId());
                    if (order != null) {
                        order.setStatus(MallOrderStatus.PAID);
                        order.setUpdateTime(new Date());
                        orderMapper.updateById(order);
                    }
                }
                task.setUpdateTime(new Date());
                updateById(task);
            }
        }
    }

    private void advance(MallDeliveryTask task) {
        MallOrder order = orderMapper.selectById(task.getOrderId());
        if (order == null) {
            return;
        }
        MallProduct product = productMapper.selectById(order.getProductId());
        DeliveryHandler handler = handlers.stream()
                .filter(h -> h.supports(product == null ? "" : product.getProductType()))
                .findFirst()
                .orElseThrow(() -> new JeecgBootException("无可用交付处理器"));

        DeliveryContext ctx = new DeliveryContext()
                .setOrder(order)
                .setProduct(product)
                .setTask(task);
        handler.execute(ctx);

        task.setUpdateTime(new Date());
        updateById(task);

        if (MallDeliveryStatus.SUCCESS.equals(task.getStatus())) {
            order.setStatus(MallOrderStatus.COMPLETED);
            order.setUpdateTime(new Date());
            orderMapper.updateById(order);
        } else if (MallDeliveryStatus.RUNNING.equals(task.getStatus())
                || MallDeliveryStatus.PENDING_MANUAL.equals(task.getStatus())) {
            order.setStatus(MallOrderStatus.PROCESSING);
            order.setUpdateTime(new Date());
            orderMapper.updateById(order);
        }
    }
}
