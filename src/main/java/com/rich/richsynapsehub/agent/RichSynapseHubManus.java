package com.rich.richsynapsehub.agent;

import com.rich.richsynapsehub.advisor.ChatLogAdvisor;
import com.rich.richsynapsehub.constant.AgentParam;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 * RichSynapseHubManus 综合型 AI 智能体，具备多种工具调用能力、自主思考规划能力、多轮对话能力、日志记录能力等
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/7
 **/
@Component
public class RichSynapseHubManus extends ToolCallAgent {

    /**
     * 实例化 manus ，供其他模块调用
     *
     * @param aiUseTools
     * @param dashscopeChatModel
     * @return
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    public RichSynapseHubManus(ToolCallback[] aiUseTools, ChatModel dashscopeChatModel) {
        // 初始化未自定义的 AI 工具包
        super(aiUseTools);
        // 设置 agent 名
        this.setName("RichSynapseHubManus");
        // 指定 agent 的系统提示词
        // 参考自 OpenManus：https://github.com/FoundationAgents/OpenManus
        String SYSTEM_PROMPT = """  
                You are RichSynapseHubManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests. 
                When responding, try to search for relevant information as much as possible and refer to the searched content appropriately to construct results. 
                If the user does not explicitly mention it, do not generate a file (if it is generated, please inform "我的文档" to search for it), 
                just build accurate text output.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If you want to stop the interaction at any point, use the `chatTerminate` tool/function call.  
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        // 初始化最大思考步数
        this.setMaxSteps(AgentParam.MAX_STEPS);
        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new ChatLogAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}
