package com.rich.richsynapsehub.advisor.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置集成文档检索(Document Retriever) 的 advisor
 * 源：https://java2ai.com/docs/1.0.0-M6.1/tutorials/retriever/?spm=4347728f.55f6a24a.0.0.4bd96266NehCvR
 * @return
 * @author DuRuiChi
 * @create 2025/7/5
 **/
@Configuration
@Slf4j
class InterviewCloudRagAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashScopeApiKey;

    /**
     * 声明 InterviewCloudRagAdvisor 的 Bean，用于配置文档检索器后开启查询云 RAG 知识库的能力。
     *
     * @return org.springframework.ai.chat.client.advisor.api.Advisor
     * @author DuRuiChi
     * @create 2025/7/5
     **/
    @Bean
    public Advisor InterviewCloudRagAdvisor() {
        // 创建 DashScopeApi 实例
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);
        final String KNOWLEDGE_INDEX = "模拟面试知识库";
        // 构建文档检索器
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                // 传入Runtime Options来覆盖默认配置
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());
        // 使用 RetrievalAugmentationAdvisor 构建查询云知识库 Advisor
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }
}
