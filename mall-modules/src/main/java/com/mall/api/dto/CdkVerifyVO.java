package com.mall.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class CdkVerifyVO {
    private String code;
    private String productId;
    private String productName;
    private String productType;
    private String category;
    private BigDecimal price;
    private Integer durationDays;
    private String message;
}
