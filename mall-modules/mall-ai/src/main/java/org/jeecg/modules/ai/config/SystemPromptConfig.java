package org.jeecg.modules.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 单体：从 classpath / 配置属性加载系统提示词，不依赖 Nacos。
 */
@Slf4j
@Getter
@Configuration
@RequiredArgsConstructor
public class SystemPromptConfig {

    private final AgentScopeProperties agentScopeProperties;
    private final AtomicReference<String> agentSystemMessage = new AtomicReference<>("");

    @PostConstruct
    public void init() {
        AgentScopeProperties.System.Chat chat = agentScopeProperties.getSystem().getChat();
        String content = chat.getContent();
        if (oConvertUtils.isEmpty(content)) {
            String location = oConvertUtils.getString(chat.getClasspath(), "config/agent-system-message.txt");
            try {
                ClassPathResource resource = new ClassPathResource(location);
                if (resource.exists()) {
                    content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                } else {
                    log.warn("系统提示词文件不存在: classpath:{}，使用空提示词", location);
                    content = "";
                }
            } catch (Exception e) {
                log.error("加载系统提示词失败: {}", location, e);
                content = "";
            }
        }
        agentSystemMessage.set(content);
        log.info("已加载 Agent 系统提示词，长度={}", content == null ? 0 : content.length());
    }
}
