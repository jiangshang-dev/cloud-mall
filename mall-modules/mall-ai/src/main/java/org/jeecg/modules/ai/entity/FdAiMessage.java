package org.jeecg.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("fd_ai_message")
public class FdAiMessage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long sessionId;
    private Long userId;
    private String senderType;
    private String msgType;
    private String content;
    private String status;
    private Long createTime;
    private Long updateTime;
}
