package com.rich.richsynapsehub.advisor;

import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 官方提供的 Re2 Advisor 增强 ， 实现 CallAroundAdvisor 和 StreamAroundAdvisor 接口
 * 参考：https://docs.spring.io/spring-ai/reference/api/advisors.html#_core_components
 *
 * @return
 * @author DuRuiChi
 * @create 2025/7/4
 **/
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {


	/**
	 * 前置逻辑，对用户输入的问题进行二次确认，增强模型的理解能力
	 *
	 * @param advisedRequest
	 * @return org.springframework.ai.chat.client.advisor.api.AdvisedRequest
	 * @author DuRuiChi
	 * @create 2025/7/4
	 **/
	private AdvisedRequest before(AdvisedRequest advisedRequest) {
		// 用户参数
		Map<String, Object> advisedUserParams = new HashMap<>(advisedRequest.userParams());

		// 注入新参数：将原始问题文本存入 re2_input_query
		advisedUserParams.put("re2_input_query", advisedRequest.userText());

		// 构建新请求：使用文本块语法定义重复问题模板
		return AdvisedRequest.from(advisedRequest)
				.userText("""
                {re2_input_query}
                Read the question again: {re2_input_query}
                """)
				.userParams(advisedUserParams)
				.build();
	}

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
		return chain.nextAroundCall(this.before(advisedRequest));
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
		return chain.nextAroundStream(this.before(advisedRequest));
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
}