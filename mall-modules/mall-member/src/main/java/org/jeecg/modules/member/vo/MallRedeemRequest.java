package org.jeecg.modules.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "积分兑换")
public class MallRedeemRequest {
    private String productId;
    private Integer quantity;
    private String addressId;
}
