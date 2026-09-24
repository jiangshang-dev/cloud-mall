package com.mall.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RedeemResultVO {
    private String orderNo;
    private String orderId;
    private String status;
    private String message;
}
