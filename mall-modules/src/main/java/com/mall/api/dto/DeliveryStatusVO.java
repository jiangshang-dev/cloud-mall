package com.mall.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeliveryStatusVO {
    private String orderNo;
    private String orderStatus;
    private String taskStatus;
    private Integer progress;
    private String resultMsg;
}
