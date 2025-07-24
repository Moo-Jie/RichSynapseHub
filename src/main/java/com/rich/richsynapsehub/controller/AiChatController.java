package com.rich.richsynapsehub.controller;

import cn.hutool.core.util.RandomUtil;
import com.rich.richsynapsehub.agent.RichSynapseHubManus;
import com.rich.richsynapsehub.utils.ai.doChat.SpringAiChat;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/doChat")
public class AiChatController {

    @Resource
    private SpringAiChat springAiChat;

    @Resource
    private ToolCallback[] aiUseTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 普通 AI 对话 （响应式接口）
     *
     * @param message   消息
     * @param chatId    会话 ID
     * @param knowledgeIndex    RAG 知识库类型
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    @GetMapping("/sync")
    public String doChatBySync(String message, String chatId, String knowledgeIndex) {
        if (chatId == null) {
            // 随机生成 chatId
            chatId = RandomUtil.randomString(5);
        }
        return springAiChat.chat(message, chatId,knowledgeIndex);
    }

    /**
     * 普通 AI 对话 （Flux 流式接口）
     *
     * @param message   消息
     * @param chatId    会话 ID
     * @param knowledgeIndex    RAG 知识库类型
     * @return reactor.core.publisher.Flux<java.lang.String>
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    @GetMapping(value = "/stream")
    public Flux<String> doChatByStream(
            @RequestParam() String message,
            @RequestParam()  String knowledgeIndex,
            @RequestParam(required = false) String chatId) {
        if (StringUtils.isBlank(message)) {
            return Flux.error(new IllegalArgumentException("消息内容不能为空"));
        }
        if (chatId == null) {
            // 随机生成 chatId
            chatId = RandomUtil.randomString(5);
        }
        return springAiChat.doChatByStream(message, chatId,knowledgeIndex);
    }

    /**
     * 与自主规划智能体 AI 对话 （SseEmitter 流式接口）
     *
     * @param message
     * @return org.springframework.web.servlet.mvc.method.annotation.SseEmitter
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    @GetMapping("/manus/stream")
    public SseEmitter doChatWithManus(@RequestParam() String message) {
        RichSynapseHubManus richSynapseHubManus = new RichSynapseHubManus(aiUseTools, dashscopeChatModel);
        return richSynapseHubManus.runStream(message);
    }
}
