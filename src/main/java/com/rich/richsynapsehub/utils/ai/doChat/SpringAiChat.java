package com.rich.richsynapsehub.utils.ai.doChat;

import com.rich.richsynapsehub.advisor.ChatLogAdvisor;
import com.rich.richsynapsehub.utils.ai.chatMeory.FileChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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

    @Resource
    private Advisor interviewCloudRagAdvisor;

    @Resource
    private ToolCallback[] aiUseTools;

    /**
     * 引入 ChatClient
     */
    private final ChatClient chatClient;

    /**
     * 系统提示词
     */
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个专业知识丰富的面试专家，擅长研究和回答各式各样的面试中遇到的问题，或面试题，同时你也喜欢和我聊天。" +
            "(  格式设定：遵循 Markdown 格式的分标题输出； 字数至少在 500 以上 ；在输出之前先打印：“你好 ！我是 AI 面试专家。”)";

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
        chatClient = ChatClient.builder(dashscopeChatModel).defaultSystem(DEFAULT_SYSTEM_PROMPT)
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
    public String chat(String message, String userId) {
        String text = chatClient
                .prompt()
                .user("现在我要问你一个面试题，请你以面试者的身份简明扼要地、总结性地、在短时间内快速地回答我的问题 " + message)
                // 设置会话 ID 和 检索历史上下文长度
                .advisors(spec -> spec
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 读取云 RAG 知识库
//                .advisors(interviewCloudRagAdvisor)
                .tools(aiUseTools)
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
                .user("现在我要问你一个面试题，请你以面试者的身份简明扼要地、总结性地、在短时间内快速地回答我的问题 " + message)
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

    /**
     * 参考上下文执行对话，传入消息 和 会话 ID 即可实现多轮对话 （流式输出）
     *
     * @param message
     * @param chatId
     * @return reactor.core.publisher.Flux<java.lang.String>
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user("现在我要问你一个面试题，请你以面试者的身份简明扼要地、总结性地、在短时间内快速地回答我的问题 " + message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

}
