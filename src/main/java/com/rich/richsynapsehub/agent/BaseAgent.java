package com.rich.richsynapsehub.agent;

import com.itextpdf.styledxmlparser.jsoup.internal.StringUtil;
import com.rich.richsynapsehub.constant.AgentParam;
import com.rich.richsynapsehub.enumeration.AgentExecutionState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * agent 基础抽象类，包含基本属性通用方法
 * 参考 OpenManus：https://github.com/FoundationAgents/OpenManus
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/7
 **/
@Data
@Slf4j
public abstract class BaseAgent {

    // 维护 Agent 名称
    private String name;

    // 维护 Agent 本次对话上下文
    private String systemPrompt;
    private String nextStepPrompt;

    // 维护 Agent 执行状态
    private AgentExecutionState state = AgentExecutionState.IDLE;

    // 维护 Agent 思考步骤
    private int maxSteps = AgentParam.MAX_STEPS;
    private int currentStep = 0;

    // 设定 Agent 用到的 LLM
    private ChatClient chatClient;

    // 维护 Agent 会话列表
    private List<Message> messageList = new ArrayList<>();

    /**
     * 执行 agent 流程，循环执行 step 方法，直到达到最大步数或状态变为 FINISHED (同步输出)
     *
     * @param userPrompt
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    public String run(String userPrompt) {
        // 校验参数
        if (userPrompt == null || userPrompt.isEmpty()) {
            throw new IllegalArgumentException("用户对话不能为空。");
        }
        if (this.state != AgentExecutionState.IDLE) {
            throw new IllegalStateException("代理状态必须为 IDLE 才能执行。");
        }
        // 初始化状态
        state = AgentExecutionState.RUNNING;
        // 保存上下文列表
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            // Agent Loop 执行循环 ：从而使智能体在没有更多用户上下文的情况下，重复推理出来延申内容
            while (this.state == AgentExecutionState.RUNNING && currentStep < maxSteps) {
                currentStep++;
                String result = "Step " + "(" + currentStep + "/" + maxSteps + ") finished:" + step() + ".";
                results.add(result);
                log.info(result);
            }
            // 超出步骤限制结束
            if (currentStep == maxSteps) {
                this.state = AgentExecutionState.FINISHED;
                results.add("当前步骤已达上限： " + "(" + currentStep + "/" + maxSteps + ")");
            }
            // 返回结果列表
            return String.join("\n", results);
        } catch (Exception e) {
            // 执行失败
            this.state = AgentExecutionState.ERROR;
            results.add("执行过程中发生错误，请联系重新尝试 或 联系管理员：" + e.getMessage());
            log.error("执行过程中发生错误", e);
            return String.join("\n", results);
        } finally {
            log.info("Agent 执行结束");
        }
    }

    /**
     * 执行 agent 流程，循环执行 step 方法，直到达到最大步数或状态变为 FINISHED (异步输出)
     *
     * @param userPrompt
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    public SseEmitter runStream(String userPrompt) {
        SseEmitter emitter = new SseEmitter(30000L);
        AtomicBoolean isActive = new AtomicBoolean(true); // 跟踪连接状态

        // 连接状态监听器
        emitter.onTimeout(() -> {
            isActive.set(false);
            this.state = AgentExecutionState.ERROR;
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            isActive.set(false);
            if (this.state == AgentExecutionState.RUNNING) {
                this.state = AgentExecutionState.FINISHED;
            }
            log.info("SSE connection completed");
        });

        CompletableFuture.runAsync(() -> {
            try {
                // 参数校验（保持不变）
                if (userPrompt == null || userPrompt.isEmpty()) {
                    throw new IllegalArgumentException("用户对话不能为空。");
                }
                if (this.state != AgentExecutionState.IDLE) {
                    throw new IllegalStateException("代理状态必须为 IDLE 才能执行。");
                }

                state = AgentExecutionState.RUNNING;
                messageList.add(new UserMessage(userPrompt));

                while (state == AgentExecutionState.RUNNING &&
                        currentStep < maxSteps &&
                        isActive.get()) { // 检查连接状态

                    currentStep++;
                    String result = "执行轮次 (" + currentStep + "/" + maxSteps + ") ：" + step();

                    // 仅在连接活跃时发送
                    if (isActive.get()) {
                        try {
                            emitter.send(result);
                            emitter.send("本轮思考结果：" + messageList.getLast().getText());
                        } catch (IllegalStateException e) {
                            log.warn("发送失败：连接已关闭");
                            break;
                        }
                    }
                }

                // 仅在连接活跃时发送完成通知
                if (isActive.get()) {
                    if (currentStep >= maxSteps) {
                        this.state = AgentExecutionState.FINISHED;
                        emitter.send("当前步骤已达上限 (" + currentStep + "/" + maxSteps + ")");
                    }
                    emitter.complete();
                }
            } catch (Exception e) {
                this.state = AgentExecutionState.ERROR;
                log.error("执行错误: {}", e.getMessage());

                // 仅在连接活跃时发送错误
                if (isActive.get()) {
                    try {
                        emitter.send("执行错误: " + e.getMessage());
                        emitter.completeWithError(e);
                    } catch (IOException ex) {
                        log.warn("无法发送错误：连接已关闭");
                    }
                }
            } finally {
                log.info("Agent 执行结束");
            }
        });

        return emitter;
    }


    /**
     * 执行 agent 流程中的单次步骤，供子类实现
     *
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    public abstract String step();
}
