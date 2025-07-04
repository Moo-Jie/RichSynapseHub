package com.rich.richsynapsehub.utils.doChat;

import com.rich.richsynapsehub.advisor.ChatLogAdvisor;
import com.rich.richsynapsehub.utils.chatMeory.FileChatMemory;
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
 * 基于 Spring AI 特性实现了各类对话方式，如多轮对话、结构化输出、advisor 增强、持久化存储等
 * 参考：https://docs.spring.io/spring-ai/reference/index.html
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
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个专业的金融大亨，你擅长回答金融相关的问题。" +
            "回答格式为：标题+内容。标题字数小于10字，内容字数小于50字。";

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     * @return
     * @author DuRuiChi
     * @create 2025/7/3
     **/
    public SpringAiChat(ChatModel dashscopeChatModel) {
        // 内存存储 ChatMemory，重启后丢失
//        ChatMemory chatMemory = new InMemoryChatMemory();
        // 自定义文件持久化 ChatMemory
        String fileDir = System.getProperty("user.dir") + "/chatMemory";
        FileChatMemory chatMemory = new FileChatMemory(fileDir);
        // 注册 ChatClient
        chatClient = ChatClient.builder(dashscopeChatModel).defaultSystem(DEFAULT_SYSTEM_PROMPT)
                // 添加 Advisors ， 增强 ChatClient 的功能
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory),
//                        new ReReadingAdvisor(), // 自定义 Re2 增强
                        new ChatLogAdvisor() // 自定义日志增强
                ).build();
    }

    /**
     * 参考上下文执行对话，传入消息 和 会话 ID 即可实现多轮对话 （非流式输出）
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
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();
        return text;
    }

    /**
     * 定义返回的结果格式，前提：AI 必须遵循标题 + 内容的格式
     *
     * @author DuRuiChi
     * @create 2025/7/3
     **/
    record StructuredOutput(String title, String content) {
    }

    /**
     * 参考上下文执行对话，传入消息 和 会话 ID 即可实现多轮对话 （结构化输出）
     *
     * @param message
     * @param userId
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/3
     **/
    public StructuredOutput structuredOutputChat(String message, String userId) {
        StructuredOutput entity = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                // 指定返回的结果格式
                .entity(StructuredOutput.class);
        log.info("结构化后的结果：{}", entity);
        // 响应结构化后的结果
        return entity;
    }
}
