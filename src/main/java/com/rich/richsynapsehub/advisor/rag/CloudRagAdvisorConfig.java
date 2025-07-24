package com.rich.richsynapsehub.advisor.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置集成文档检索(Document Retriever) 的 advisor
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/24
 **/
@Configuration
public class CloudRagAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    /**
     * 知识库索引工厂方法，根据索引名称创建对应的知识库检索增强器
     *
     * @param knowledgeIndex
     * @return org.springframework.ai.chat.client.advisor.api.Advisor
     * @author DuRuiChi
     * @create 2025/7/24
     **/
    public Advisor createRagAdvisor(String knowledgeIndex) {
        // 创建 DashScope API 客户端实例（使用配置的 API Key）
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);

        // 构建文档检索器，绑定指定的知识库索引
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        // 设置知识库索引名称
                        .withIndexName(knowledgeIndex)
                        .build());

        // 创建 RAG Advisor ，将文档检索功能集成到 AI 对话流程
        return RetrievalAugmentationAdvisor.builder()
                // 注入文档检索器
                .documentRetriever(documentRetriever)
                .build();
    }

    /**
     * 默认知识库
     *
     * @return org.springframework.ai.chat.client.advisor.api.Advisor
     * @author DuRuiChi
     * @create 2025/7/24
     **/
    @Bean
    public Advisor defaultInterviewRagAdvisor() {
        return createRagAdvisor("面试专家知识库");
    }
}