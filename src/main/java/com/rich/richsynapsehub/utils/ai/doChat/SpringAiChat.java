package com.rich.richsynapsehub.utils.ai.doChat;

import com.rich.richsynapsehub.advisor.ChatLogAdvisor;
import com.rich.richsynapsehub.advisor.rag.CloudRagAdvisorConfig;
import com.rich.richsynapsehub.constant.SystemPromptConstant;
import com.rich.richsynapsehub.utils.ai.chatMeory.FileChatMemory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.rich.richsynapsehub.constant.FilePathConstant.CHAT_FILE_SAVE_DIR;
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
     * 用于指定知识库索引的 advisor 缓存
     **/
    private final Map<String, Advisor> ragAdvisors = new ConcurrentHashMap<>();

    @Autowired
    private CloudRagAdvisorConfig advisorFactory;

    /**
     * 初始化知识库映射
     */
    @PostConstruct
    public void init() {
        ragAdvisors.put("interview", advisorFactory.createRagAdvisor("面试专家知识库"));
        ragAdvisors.put("shop", advisorFactory.createRagAdvisor("购物大师知识库"));
    }

    /**
     * 通过指定 RAG 知识库索引来获取对应的 RAG Advisor 实例
     *
     * @param knowledgeIndex
     * @return org.springframework.ai.chat.client.advisor.api.Advisor
     * @author DuRuiChi
     * @create 2025/7/24
     **/
    private Advisor getRagAdvisor(String knowledgeIndex) {
        return ragAdvisors.computeIfAbsent(knowledgeIndex, k -> advisorFactory.createRagAdvisor(knowledgeIndex));
    }

    /**
     * 系统提示词
     */
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个专业的智能体 AI，擅长回答各种问题，你擅长以 Markdown 格式返回结果";

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
        FileChatMemory chatMemory = new FileChatMemory(CHAT_FILE_SAVE_DIR);
        // 注册 ChatClient
        chatClient = ChatClient.builder(dashscopeChatModel)
                // 添加 Advisors ， 增强 ChatClient 的功能
                // TODO 实现数据库持久化 ChatMemory
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory),
//                        new ReReadingAdvisor(), // 自定义 Re2 增强 ，会消耗更多 token
                        new ChatLogAdvisor() // 自定义日志增强
                ).build();
    }

    /**
     * 参考上下文执行对话，传入消息 和 会话 ID 即可实现多轮对话 （响应式输出）
     *
     * @param message
     * @param userId
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/3
     **/
    public String chat(String message, String userId, String knowledgeIndex) {
        Advisor ragAdvisor = getRagAdvisor(knowledgeIndex);

        return chatClient.prompt()
                .user("问题：" + message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(ragAdvisor)
                .call().content();
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
        StructuredOutput entity = chatClient.prompt()
                .user("现在我要问你一个面试题，请你以面试者的身份简明扼要地、总结性地、在短时间内快速地回答我的问题 " + message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                // 指定返回的结果格式
                .entity(StructuredOutput.class);
        log.info("结构化后的结果：{}", entity);
        // 响应结构化后的结果
        return entity;
    }

    /**
     * 参考上下文执行对话实现多轮对话 （流式输出）
     *
     * @param message        消息
     * @param chatId         会话 ID
     * @param knowledgeIndex RAG 知识库类型
     * @return reactor.core.publisher.Flux<java.lang.String>
     * @author DuRuiChi
     * @create 2025/7/24
     **/
    public Flux<String> doChatByStream(String message, String chatId, String knowledgeIndex) {
        Advisor ragAdvisor = getRagAdvisor(knowledgeIndex);

        // 根据知识库类型设置不同的系统上下文
        String systemPrompt = switch (knowledgeIndex) {
            // 面试专家
            case "interview" -> SystemPromptConstant.INTERVIEW;
            // 资深购物策略专家
            case "shop" -> SystemPromptConstant.SHOP;
            // 资深学习策略专家
            case "study" -> SystemPromptConstant.STUDY;
            // 资深健身策略专家
            case "fitness" -> SystemPromptConstant.FITNESS;
            // 资深旅行策略专家
            case "travel" -> SystemPromptConstant.TRAVEL;
            // 资深开发策略专家
            case "code" -> SystemPromptConstant.CODE;
            // 资深情感策略专家
            case "emotion" -> SystemPromptConstant.EMOTION;
            // 默认上下文
            default -> DEFAULT_SYSTEM_PROMPT;
        };

        return chatClient.prompt()
                // 上下文设定
                .system(systemPrompt)
                .user(message)
                // advisors
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(ragAdvisor)
                // 流式输出
                .stream()
                .content();
    }
}