package com.mall.api.dto;

import com.mall.order.entity.MallOrder;
import com.mall.order.entity.MallOrderInstallment;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class OrderDetailVO {
    private MallOrder order;
    private List<MallOrderInstallment> installments;
    private String redeemUrl;
    private String nextExpectTip;
}
