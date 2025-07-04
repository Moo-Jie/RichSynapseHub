package com.rich.richsynapsehub.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor 增强 ， 实现 CallAroundAdvisor 和 StreamAroundAdvisor 接口
 * 参考：https://docs.spring.io/spring-ai/reference/api/advisors.html#_core_components
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/4
 **/
@Slf4j
public class ChatLogAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    /**
     * 实现接口方法 aroundCall ，同步调用增强 advisor
     *
     * @param advisedRequest
     * @param chain
     * @return org.springframework.ai.chat.client.advisor.api.AdvisedResponse
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 调用前逻辑
        this.beforeInfo(advisedRequest);
        // 跳转后续 advisor ， AdvisorChain 是一个接口，用于管理和执行多个 Advisor 的调用链。
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        // 调用后逻辑
        this.afterInfo(advisedResponse);
        return advisedResponse;
    }

    /**
     * 实现接口方法 aroundStream ，流式调用增强 advisor
     *
     * @param advisedRequest
     * @param chain
     * @return reactor.core.publisher.Flux<org.springframework.ai.chat.client.advisor.api.AdvisedResponse>
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 调用前逻辑
        this.beforeInfo(advisedRequest);
        // 跳转后续 advisor ， AdvisorChain 是一个接口，用于管理和执行多个 Advisor 的调用链。
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        // 调用后逻辑 ，创建一个消息聚合器，调用 aggregateAdvisedResponse 把所有的响应聚合起来
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::afterInfo);
    }

    /**
     * 实现接口方法 getName，返回当前类名作为Advisor的唯一标识
     *
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 实现接口方法 getOrder，初始化执行顺序（数值越小优先级越高）
     *
     * @return int
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 前置逻辑，输出用户对话记录
     *
     * @param request
     * @return org.springframework.ai.chat.client.advisor.api.AdvisedRequest
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    private AdvisedRequest beforeInfo(AdvisedRequest request) {
        log.info("""
                
                ↓————————↓————————↓————————↓————————↓————————↓————————↓————————↓————————↓————————↓
                
                 User chat:
                 {}\s
                
                """, request.userText());
        return request;
    }

    /**
     * 后置逻辑，输出 AI 对话记录
     *
     * @param advisedResponse
     * @return void
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    private void afterInfo(AdvisedResponse advisedResponse) {
        log.info("""
                
                 AI chat:
                 {}\s
                
                ↑————————↑————————↑————————↑————————↑————————↑————————↑————————↑————————↑————————↑
                """, advisedResponse.response().getResult().getOutput().getText());
    }
}