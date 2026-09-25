package com.mall.service;

import com.mall.domain.vo.AiMessageVO;
import com.mall.domain.vo.AiSessionSummaryVO;
import com.mall.domain.vo.AiSessionVO;
import com.mall.domain.vo.MessageDataVO;
import reactor.core.publisher.Flux;
import java.util.List;

public interface ChatService {


    List<AiSessionSummaryVO> historyList(String authorization, String source, Integer pageNo, Integer pageSize);

    /**
     * 新建AI对话会话
     */
    AiSessionVO createSession(String authorization, String source);

    /**
     * 获取用户当前（最近打开）的 AI 会话，用于 App 恢复聊天页
     */
    AiSessionVO activeSession(String authorization, String source);

    /**
     * AI会话历史消息
     * @param authorization token
     * @param id 会话ID
     * @return 消息列表
     */
    List<AiMessageVO> messagesById(String authorization, String id);

    /**
     * 停止对话
     * @param authorization
     * @param request
     */
    void stopChat(String authorization, ChatDtos.StopChatRequest request);

    Flux<SseResponse<MessageDataVO<Object>>> chat(String authorization, AiChatDTO dto);
}
