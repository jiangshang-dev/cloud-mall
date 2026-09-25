package com.mall.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI会话信息")
public class AiSessionVO {

    private String id;
    private String status;
    private String source;
    private String humanGreeting;
    private Long createTime;
}
