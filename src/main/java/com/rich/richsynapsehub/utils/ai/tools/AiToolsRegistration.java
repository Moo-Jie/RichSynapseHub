package com.rich.richsynapsehub.utils.ai.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 供 AI 调用的工具集注册
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/6
 **/
@Configuration
public class AiToolsRegistration {

    @Resource
    private AiWebSearchTool aiWebSearchTool;

    /**
     * 注册所有工具
     *
     * @return org.springframework.ai.tool.ToolCallback[]
     * @author DuRuiChi
     * @create 2025/7/6
     **/
    @Bean
    public ToolCallback[] aiUseTools() {
        AiFileWRTool fileOperationTool = new AiFileWRTool();
        AiWebScrapingTool aiWebScrapingTool = new AiWebScrapingTool();
        AiNetDownloadTool aiNetDownloadTool = new AiNetDownloadTool();
        AiPDFGenerationTool aiPDFGenerationTool = new AiPDFGenerationTool();
        AiTerminateTool aiTerminateTool = new AiTerminateTool();
        AiTerminalOperationTool aiTerminalOperationTool = new AiTerminalOperationTool();
        return ToolCallbacks.from(fileOperationTool,
                aiWebScrapingTool,
                aiNetDownloadTool,
                // TODO 待解决 PDF 生成问题
//                aiPDFGenerationTool,
                aiTerminateTool,
                aiTerminalOperationTool,
                aiWebSearchTool);
    }
}
