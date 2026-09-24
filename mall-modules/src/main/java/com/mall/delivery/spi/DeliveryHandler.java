package com.mall.delivery.spi;

/**
 * 交付扩展点。本期仅提供占位实现，不接入任何第三方会话/代充逻辑。
 */
public interface DeliveryHandler {

    boolean supports(String productType);

    /**
     * 执行交付。实现方可更新 task 的 progress/status/resultMsg。
     */
    void execute(DeliveryContext ctx);
}
