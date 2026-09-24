package com.mall.delivery.spi;

import com.mall.common.constant.MallDeliveryStatus;
import com.mall.delivery.entity.MallDeliveryTask;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 演示交付：自动推进进度并标记成功（非真实第三方充值）。
 */
@Component
@Order(100)
public class DemoAutoDeliveryHandler implements DeliveryHandler {

    @Override
    public boolean supports(String productType) {
        return true;
    }

    @Override
    public void execute(DeliveryContext ctx) {
        MallDeliveryTask task = ctx.getTask();
        int progress = task.getProgress() == null ? 0 : task.getProgress();
        if (progress < 30) {
            task.setStatus(MallDeliveryStatus.RUNNING);
            task.setProgress(30);
            task.setResultMsg("已接收交付请求，排队处理中");
        } else if (progress < 70) {
            task.setStatus(MallDeliveryStatus.RUNNING);
            task.setProgress(70);
            task.setResultMsg("正在开通套餐权益（占位）");
        } else {
            task.setStatus(MallDeliveryStatus.SUCCESS);
            task.setProgress(100);
            task.setResultMsg("交付完成（演示占位，未对接真实第三方）");
        }
    }
}
