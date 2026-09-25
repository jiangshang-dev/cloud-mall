package com.mall.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "AI对话会话摘要（fondia listAiSessions）")
public class AiSessionSummaryVO {

    private String id;
    private String lastMessage;
    private Long lastMessageTime;
    private String source;
}
