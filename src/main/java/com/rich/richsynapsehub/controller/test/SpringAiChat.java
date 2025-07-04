package com.rich.richsynapsehub.controller.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * Spring AI 单次对话连接测试
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/3
 **/
@Component
@Slf4j
public class SpringAiChat {

    /**
     * 引入 ChatClient
     */
    private final ChatClient chatClient;

    /**
     * 系统提示词
     */
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个专业的金融大亨，你擅长回答金融相关的问题。";

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     * @return
     * @author DuRuiChi
     * @create 2025/7/3
     **/
    public SpringAiChat(ChatModel dashscopeChatModel) {
        // 内存存储历史对话上下文
        ChatMemory chatMemory = new InMemoryChatMemory();
        // 注册 ChatClient
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
    }

    /**
     * 参考上下文执行对话
     *
     * @param message
     * @param userId
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/3
     **/
    public String chat(String message, String userId) {
        String text = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
//        log.info("AI content: {}", text);
        return text;
    }
}
