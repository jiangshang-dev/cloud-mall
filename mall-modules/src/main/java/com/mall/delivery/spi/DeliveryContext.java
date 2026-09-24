package com.mall.delivery.spi;

import com.mall.delivery.entity.MallDeliveryTask;
import com.mall.order.entity.MallOrder;
import com.mall.product.entity.MallProduct;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeliveryContext {
    private MallOrder order;
    private MallProduct product;
    private MallDeliveryTask task;
}
