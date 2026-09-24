package com.mall.api.dto;

import lombok.Data;

@Data
public class CdkBatchCreateRequest {
    private String productId;
    private Integer count = 1;
    private Integer expireDays = 365;
}
