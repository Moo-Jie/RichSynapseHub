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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        // 创建 SseEmitter ，设置 0.5 分钟超时
        SseEmitter emitter = new SseEmitter(30000L);

        // 使用线程异步处理，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            try {
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

                try {
                    // Agent Loop 执行循环 ：从而使智能体在没有更多用户上下文的情况下，重复推理出来延申内容
                    while (this.state == AgentExecutionState.RUNNING && currentStep < maxSteps) {
                        currentStep++;
                        String result = "Step " + "(" + currentStep + "/" + maxSteps + ") finished:" + step() + ".";
                        // 发送每一步的结果
                        emitter.send(result);
                    }

                    // 超出步骤限制结束
                    if (currentStep == maxSteps) {
                        this.state = AgentExecutionState.FINISHED;
                        emitter.send("当前步骤已达上限： " + "(" + currentStep + "/" + maxSteps + ")");
                    }

                    // 完成 SSE 流
                    emitter.complete();
                } catch (Exception e) {
                    // 执行失败
                    this.state = AgentExecutionState.ERROR;
                    log.error("执行过程中发生错误", e);
                    try {
                        emitter.send("执行错误: " + e.getMessage());
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                } finally {
                    log.info("Agent 执行结束");
                }
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            this.state = AgentExecutionState.ERROR;
            log.warn("SSE connection timed out");
        });

        emitter.onCompletion(() -> {
            if (this.state == AgentExecutionState.RUNNING) {
                this.state = AgentExecutionState.FINISHED;
            }
            log.info("Agent 执行结束");
            log.info("SSE connection completed");
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
