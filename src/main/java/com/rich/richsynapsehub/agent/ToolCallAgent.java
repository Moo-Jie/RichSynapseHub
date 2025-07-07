package com.rich.richsynapsehub.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.rich.richsynapsehub.enumeration.AgentExecutionState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 实现 think 和 act 方法来来完善工具调用能力 ，继承 ReActModeAgent 接口
 * 参考 OpenManus：https://github.com/FoundationAgents/OpenManus
 *
 * @author DuRuiChi
 * @create 2025/7/7
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActModeAgent {
    // 工具调用后响应的相关信息
    private ChatResponse useToolCallChatResponse;

    // 工具调管理类
    private final ToolCallingManager toolCallingManager;

    // 供 AI 使用的可用工具集
    private final ToolCallback[] aiAvailableTools;

    // 自定义 ChatOptions ，覆盖 Spring AI 默认的 ChatOptions
    private final ChatOptions chatOptions;

    /**
     * 初始化工具调用 Agent
     *
     * @param aiAvailableTools
     * @return
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    public ToolCallAgent(ToolCallback[] aiAvailableTools) {
        super();
        // 初始化为自定义工具集
        this.aiAvailableTools = aiAvailableTools;
        // 初始化工具调用管理类
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 自定义 ChatOptions ，覆盖 Spring AI 默认的 ChatOptions
        this.chatOptions = DashScopeChatOptions.builder()
                // 开启工具调用
                .withProxyToolCalls(true).build();
    }

    /**
     * Reasoning：调用工具执行当前任务的思考过程，并决定是否需要执行行动
     *
     * @return boolean
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    @Override
    public boolean think() {
        // 校验下一思考内容
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            getMessageList().add(new UserMessage(getNextStepPrompt()));
        }
        // 加入消息历史列表
        List<Message> messageList = getMessageList();
        // 用历史消息 以及 预设参数 构造 Prompt
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            // 设定 Prompt 与 系统预设 ， 执行并记录对话
            ChatResponse chatResponse = getChatClient().prompt(prompt).system(getSystemPrompt())
                    // 指定 AI 调用的工具集
                    .tools(aiAvailableTools)
                    .call()
                    .chatResponse();
            // 保存工具调用后响应的相关信息
            this.useToolCallChatResponse = chatResponse;
            // 获取 AI 助手响应给用户的数据
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取 AI 助手响应给用户的具体消息
            String result = assistantMessage.getText();
            // 获取工具调用列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // 响应日志
            log.info("{}的思考结果: {}", getName(), result);
            log.info("{}使用了 {} 个工具", getName(), toolCallList.size());
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s ；工具参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // 通过判断是否有工具调用，来决定是否需要执行行动
            if (toolCallList.isEmpty()) {
                // 若未调用工具，需要手动指定助手消息
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 调用工具会自动记录助手消息
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }


    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!useToolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        // Spring AI toolCallingManager 执行工具调用
        List<Message> toolCallmessages = toolCallingManager.executeToolCalls(prompt, useToolCallChatResponse).conversationHistory();
        // 把工具调用结果加入到消息列表中
        setMessageList(toolCallmessages);
        // 拼装工具调用结果信息，返回给用户
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolCallmessages);
        String results = toolResponseMessage.getResponses().stream().map(response -> response.name() + "工具执行完毕，运行结果: " + response.responseData()).collect(Collectors.joining("\n"));
        // 若 AI 调用了终止工具，则结束对话
        boolean terminateToolCalled = toolResponseMessage.getResponses()
                .stream()
                .anyMatch(response -> "chatTerminate".equals(response.name()));
        if (terminateToolCalled) {
            setState(AgentExecutionState.FINISHED);
        }
        log.info(results);
        return results;
    }

}
