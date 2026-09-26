package org.jeecg.modules.support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.support.dto.SendMessageDTO;
import org.jeecg.modules.support.dto.SessionCreateDTO;
import org.jeecg.modules.support.entity.FdCsConfig;
import org.jeecg.modules.support.entity.FdCsMessage;
import org.jeecg.modules.support.entity.FdCsSession;
import com.mall.common.constant.CsSenderType;
import com.mall.common.constant.CsSessionStatus;
import org.jeecg.modules.support.mapper.FdCsMessageMapper;
import org.jeecg.modules.support.mapper.FdCsSessionMapper;
import org.jeecg.modules.support.service.IFdCsConfigService;
import org.jeecg.modules.support.service.IFdCsMessageService;
import org.jeecg.modules.support.service.IFdCsSessionService;
import org.jeecg.modules.support.service.impl.FdCsMessageServiceImpl;
import org.jeecg.modules.support.vo.AiSessionSummaryVO;
import org.jeecg.modules.support.vo.CsMessageVO;
import org.jeecg.modules.support.vo.CsSessionAdminVO;
import org.jeecg.modules.support.vo.CsSessionVO;
import org.jeecg.modules.user.service.IFdUserService;
import org.jeecg.modules.user.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FdCsSessionServiceImpl extends ServiceImpl<FdCsSessionMapper, FdCsSession> implements IFdCsSessionService {

    @Resource
    private IFdUserService userService;

    @Resource
    private IFdCsConfigService configService;

    @Resource
    private IFdCsMessageService messageService;

    @Resource
    private FdCsMessageMapper messageMapper;

    public static final String AI_TAB_SOURCE = "ai_tab";
    public static final String CS_GENERAL_SOURCE = "general";

    @Override
    public Long resolveUserId(String authorization) {
        UserInfoVO info = userService.getUserInfo(authorization);
        if (info == null || info.getId() == null) {
            throw new JeecgBootException("用户未登录或Token无效");
        }
        return info.getId();
    }

    @Override
    public CsSessionVO getOrCreateAiSession(String authorization, String source) {
        Long userId = resolveUserId(authorization);
        String channel = oConvertUtils.isEmpty(source) ? AI_TAB_SOURCE : source;
        FdCsSession session = findActiveSession(userId, channel);
        if (session == null) {
            session = createSession(userId, channel, CsSessionStatus.AI);
        }
        return toSessionVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsSessionVO transferToHuman(String authorization, SessionCreateDTO dto) {
        Long userId = resolveUserId(authorization);
        FdCsConfig config = configService.getConfigEntity();
        String channel = resolveChannelSource(dto != null ? dto.getSource() : null);
        FdCsSession session = findActiveSession(userId, channel);
        if (session != null && AI_TAB_SOURCE.equals(session.getSource())) {
            session = null;
        }
        if (session == null) {
            session = createSession(userId, channel, CsSessionStatus.WAITING);
        } else {
            session.setStatus(CsSessionStatus.WAITING);
            updateById(session);
        }
        insertHumanGreeting(session, config);
        return toSessionVO(session, config.getHumanGreeting());
    }

    @Override
    public CsSessionVO getActiveSession(String authorization) {
        return getActiveSession(authorization, CS_GENERAL_SOURCE);
    }

    @Override
    public CsSessionVO getActiveSession(String authorization, String source) {
        Long userId = resolveUserId(authorization);
        String channel = resolveChannelSource(source);
        FdCsSession session = findActiveSession(userId, channel);
        if (session == null) {
            return null;
        }
        FdCsConfig config = configService.getConfigEntity();
        return toSessionVO(session, config.getHumanGreeting());
    }

    @Override
    public List<AiSessionSummaryVO> listAiSessions(String authorization, String source, Integer pageNo, Integer pageSize) {
        Long userId = resolveUserId(authorization);
        String src = oConvertUtils.isEmpty(source) ? AI_TAB_SOURCE : source;
        Page<FdCsSession> page = new Page<>(pageNo, pageSize);
        IPage<FdCsSession> result = page(page, new LambdaQueryWrapper<FdCsSession>()
                .eq(FdCsSession::getUserId, userId)
                .eq(FdCsSession::getSource, src)
                .orderByDesc(FdCsSession::getLastMessageTime));
        return result.getRecords().stream().map(this::toAiSessionSummary).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsSessionVO createAiSession(String authorization, String source) {
        Long userId = resolveUserId(authorization);
        String src = oConvertUtils.isEmpty(source) ? AI_TAB_SOURCE : source;
        FdCsSession active = findActiveSession(userId, src);
        if (active != null) {
            active.setStatus(CsSessionStatus.CLOSED);
            active.setCloseTime(System.currentTimeMillis());
            updateById(active);
        }
        FdCsSession session = createSession(userId, src, CsSessionStatus.AI);
        return toSessionVO(session);
    }

    @Override
    public List<CsMessageVO> listMessages(String authorization, Long sessionId, Integer pageNo, Integer pageSize) {
        Long userId = resolveUserId(authorization);
        FdCsSession session = getById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new JeecgBootException("会话不存在");
        }
        return queryMessages(sessionId, pageNo, pageSize);
    }

    @Override
    public List<CsMessageVO> listMessagesForAdmin(Long sessionId, Integer pageNo, Integer pageSize) {
        FdCsSession session = getById(sessionId);
        if (session == null) {
            throw new JeecgBootException("会话不存在");
        }
        return queryMessages(sessionId, pageNo, pageSize);
    }

    private List<CsMessageVO> queryMessages(Long sessionId, Integer pageNo, Integer pageSize) {
        Page<FdCsMessage> page = new Page<>(pageNo, pageSize);
        IPage<FdCsMessage> result = messageMapper.selectPage(page,
                new LambdaQueryWrapper<FdCsMessage>()
                        .eq(FdCsMessage::getSessionId, sessionId)
                        .orderByAsc(FdCsMessage::getCreateTime));
        return result.getRecords().stream().map(this::toMessageVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsMessageVO sendUserMessage(String authorization, Long sessionId, SendMessageDTO dto) {
        Long userId = resolveUserId(authorization);
        FdCsSession session = getById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new JeecgBootException("会话不存在");
        }
        if (CsSessionStatus.CLOSED.equals(session.getStatus())) {
            throw new JeecgBootException("会话已关闭");
        }
        return saveAndDispatchUserMessage(session, userId, dto.getContent(), dto.getClientMsgId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CsMessageVO sendAgentMessage(Long sessionId, String agentId, SendMessageDTO dto) {
        FdCsSession session = getById(sessionId);
        if (session == null) {
            throw new JeecgBootException("会话不存在");
        }
        if (CsSessionStatus.CLOSED.equals(session.getStatus())) {
            throw new JeecgBootException("会话已关闭");
        }
        if (session.getAgentId() == null) {
            session.setAgentId(agentId);
        }
        session.setStatus(CsSessionStatus.CHATTING);
        FdCsMessage saved = messageService.saveMessage(
                sessionId, CsSenderType.AGENT, agentId, dto.getContent(), dto.getClientMsgId());
        session.setLastMessage(trimSummary(dto.getContent()));
        session.setLastMessageTime(System.currentTimeMillis());
        updateById(session);
        messageService.pushToUser(saved, sessionId);
        return toMessageVO(saved);
    }

    private CsMessageVO saveAndDispatchUserMessage(FdCsSession session, Long userId, String content, String clientMsgId) {
        FdCsMessage saved = messageService.saveMessage(
                session.getId(), CsSenderType.USER, String.valueOf(userId), content, clientMsgId);
        session.setLastMessage(trimSummary(content));
        session.setLastMessageTime(System.currentTimeMillis());
        if (session.getAgentId() != null) {
            session.setAgentUnread((session.getAgentUnread() == null ? 0 : session.getAgentUnread()) + 1);
            messageService.pushToAgent(saved, session.getAgentId());
        } else {
            messageService.pushToAllAgents(saved, session.getId());
        }
        updateById(session);
        return toMessageVO(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeSession(String authorization, Long sessionId) {
        Long userId = resolveUserId(authorization);
        FdCsSession session = getById(sessionId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new JeecgBootException("会话不存在");
        }
        session.setStatus(CsSessionStatus.CLOSED);
        session.setCloseTime(System.currentTimeMillis());
        updateById(session);
    }

    @Override
    public IPage<CsSessionAdminVO> pageForAdmin(FdCsSession query, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<FdCsSession> wrapper = new LambdaQueryWrapper<>();
        if (query != null) {
            if (oConvertUtils.isNotEmpty(query.getStatus())) {
                wrapper.eq(FdCsSession::getStatus, query.getStatus());
            }
            if (query.getUserId() != null) {
                wrapper.eq(FdCsSession::getUserId, query.getUserId());
            }
        }
        wrapper.orderByDesc(FdCsSession::getLastMessageTime).orderByDesc(FdCsSession::getCreateTime);
        IPage<FdCsSession> page = page(new Page<>(pageNo, pageSize), wrapper);
        return page.convert(this::toAdminVO);
    }

    private CsSessionAdminVO toAdminVO(FdCsSession session) {
        CsSessionAdminVO vo = new CsSessionAdminVO();
        vo.setId(String.valueOf(session.getId()));
        vo.setUserId(String.valueOf(session.getUserId()));
        vo.setAgentId(session.getAgentId());
        vo.setStatus(session.getStatus());
        vo.setSource(session.getSource());
        vo.setLastMessage(session.getLastMessage());
        vo.setLastMessageTime(session.getLastMessageTime());
        vo.setUserUnread(session.getUserUnread());
        vo.setAgentUnread(session.getAgentUnread());
        vo.setCreateTime(session.getCreateTime());
        vo.setCloseTime(session.getCloseTime());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptSession(String sessionId, String agentId) {
        FdCsSession session = getById(Long.valueOf(sessionId));
        if (session == null) {
            throw new JeecgBootException("会话不存在");
        }
        session.setAgentId(agentId);
        session.setStatus(CsSessionStatus.CHATTING);
        session.setAgentUnread(0);
        updateById(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminCloseSession(String sessionId) {
        FdCsSession session = getById(Long.valueOf(sessionId));
        if (session == null) {
            throw new JeecgBootException("会话不存在");
        }
        session.setStatus(CsSessionStatus.CLOSED);
        session.setCloseTime(System.currentTimeMillis());
        updateById(session);
    }

    private String resolveChannelSource(String source) {
        if (AI_TAB_SOURCE.equals(source)) {
            return AI_TAB_SOURCE;
        }
        if (oConvertUtils.isEmpty(source)) {
            return CS_GENERAL_SOURCE;
        }
        return source;
    }

    private FdCsSession findActiveSession(Long userId, String channel) {
        LambdaQueryWrapper<FdCsSession> wrapper = new LambdaQueryWrapper<FdCsSession>()
                .eq(FdCsSession::getUserId, userId)
                .in(FdCsSession::getStatus,
                        CsSessionStatus.AI, CsSessionStatus.WAITING, CsSessionStatus.CHATTING)
                .orderByDesc(FdCsSession::getLastMessageTime)
                .last("LIMIT 1");
        if (AI_TAB_SOURCE.equals(channel)) {
            wrapper.eq(FdCsSession::getSource, AI_TAB_SOURCE);
        } else if (CS_GENERAL_SOURCE.equals(channel)) {
            wrapper.ne(FdCsSession::getSource, AI_TAB_SOURCE);
        } else {
            wrapper.eq(FdCsSession::getSource, channel);
        }
        return getOne(wrapper);
    }

    private FdCsSession createSession(Long userId, String source, String status) {
        long now = System.currentTimeMillis();
        FdCsSession session = new FdCsSession()
                .setUserId(userId)
                .setStatus(status)
                .setSource(oConvertUtils.isEmpty(source) ? CS_GENERAL_SOURCE : source)
                .setUserUnread(0)
                .setAgentUnread(0)
                .setCreateTime(now)
                .setLastMessageTime(now);
        save(session);
        return session;
    }

    private FdCsMessage insertHumanGreeting(FdCsSession session, FdCsConfig config) {
        String greeting = config.getHumanGreeting();
        FdCsMessage existing = messageMapper.selectOne(new LambdaQueryWrapper<FdCsMessage>()
                .eq(FdCsMessage::getSessionId, session.getId())
                .eq(FdCsMessage::getSenderType, CsSenderType.SYSTEM)
                .last("LIMIT 1"));
        if (existing != null) {
            return existing;
        }
        return messageService.saveMessage(session.getId(), CsSenderType.SYSTEM, "SYSTEM", greeting, null);
    }

    private String trimSummary(String text) {
        if (text == null) {
            return null;
        }
        return text.length() > 200 ? text.substring(0, 200) : text;
    }

    private CsSessionVO toSessionVO(FdCsSession session) {
        return toSessionVO(session, null);
    }

    private CsSessionVO toSessionVO(FdCsSession session, String humanGreeting) {
        CsSessionVO vo = new CsSessionVO();
        vo.setId(String.valueOf(session.getId()));
        vo.setStatus(session.getStatus());
        vo.setSource(session.getSource());
        vo.setHumanGreeting(humanGreeting);
        vo.setCreateTime(session.getCreateTime());
        return vo;
    }

    private CsMessageVO toMessageVO(FdCsMessage message) {
        CsMessageVO vo = new CsMessageVO();
        BeanUtils.copyProperties(message, vo);
        vo.setId(String.valueOf(message.getId()));
        vo.setSessionId(String.valueOf(message.getSessionId()));
        vo.setRecipes(FdCsMessageServiceImpl.parseRecipesFromExtra(message.getExtraJson()));
        return vo;
    }

    private AiSessionSummaryVO toAiSessionSummary(FdCsSession session) {
        AiSessionSummaryVO vo = new AiSessionSummaryVO();
        vo.setId(String.valueOf(session.getId()));
        vo.setLastMessage(session.getLastMessage());
        vo.setLastMessageTime(session.getLastMessageTime());
        vo.setSource(session.getSource());
        return vo;
    }
}
