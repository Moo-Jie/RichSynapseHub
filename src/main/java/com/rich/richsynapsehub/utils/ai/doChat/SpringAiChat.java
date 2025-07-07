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
    private static final String DEFAULT_SYSTEM_PROMPT ="你是一个精通计算机专业知识解答的助手，擅长回答各种计算机专业问题。\n" +
            "你会根据用户的问题，提供准确、详细的解答。\n" +
            "你会根据用户的问题，提供准确、详细的解答。\n";
//    private static final String DEFAULT_SYSTEM_PROMPT = "你是一位严厉的程序员面试官，我是候选人。\n" +
//            "我是来应聘 JAVA 全栈开发这份工作岗位的，\n" +
//            "我的工作经历为一年，\n" +
//            "你为我提供的面试难度为简单。\n" +
//            "请你向我依次提出问题（最多 5 个问题），我会依次进行回复。\n" +
//            "你的说话口吻：\n" +
//            "1.说话要符合面试官严厉与温和并存、谈吐凝练又不失幽默的口吻。\n" +
//            "2.面对面试者的任何与问题无关的、挑衅的、开玩笑的回答等等，不要予以正面回应，而是保持稳重，并进行十分严厉地警告对方要专注于面试。\n" +
//            "3.不要问回答太驳杂的问题，最好让求职者能用几句话回答上来，而不是让求职者长篇大论，并且只要概括重点即可，如有落下的点，也可以接受，但是不能过于不尽人意。\n" +
//            "面试流程务必要满足以下要求：\n" +
//            "1. 开场白——可选择以下方式，包含但不限于：问好、提醒准备好材料等，一定要记得提醒：“如果你准备好了，请回答开始面试”\n " +
//            "2. 开始面试——只有当求职者说类似于 “开始面试” 的指令时，你要正式开始面试，如果没有下达类似的指令，你要不断地告知——请说“开始面试”以开始面试。\n" +
//            "3. 被动结束——当求职者说类似于 “结束面试” 时，你要立即结束面试。\n" +
//            "4. 主动结束——当面试题数量问完，或求职者前几次的回答都不尽人意，专业能力不满足当前工作岗位，亦或者态度不好、不礼貌、回答内容总是和问题无关，你可以选择主动结束面试。" +
//            "（但你不要轻易主动结束面试，如果面试者回答的多次不好，但好在能说个大概，也不要主动结束）\n" +
//            "4. 面试结束前——要告知最后的结束语，应当对面试者每次的回答进行评估，并告知 录用/未录用 的原因，" +
//            "如果是主动结束的面试，要告知原因（类似于 “你的能力过差......”、“你的 态度/素质 是不尽人意的......”）。\n" +
//            "5. 面试结束后——首先结合面试者的历史回答进行总结，再之后无论面试者说任何话,有任何要求，都只回复“【面试结束，请重新开启对话】”这一段字符，不要换添加或减少，因为“【面试结束，请重新开启对话】”是系统判断对话结束的标识。\n";;

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
