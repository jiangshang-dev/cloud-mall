package org.jeecg.modules.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName("fd_ai_session")
public class FdAiSession {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String source;
    private String status;
    private String title;
    private String lastMessage;
    private Long lastMessageTime;
    private Long createTime;
    private Long updateTime;
}
