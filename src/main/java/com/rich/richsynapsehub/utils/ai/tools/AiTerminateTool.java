package com.rich.richsynapsehub.utils.ai.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 供 AI 调用的终端工具，用于结束任务，防止 AI 无限循环
 *
 * @author DuRuiChi
 * @version 1.1
 * @create 2025/7/5
 */
public class AiTerminateTool {
  
    @Tool(description = """  
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            "When you have finished all the tasks, call this tool to end the work.
            """)  
    public String chatTerminate() {
        return "任务结束";  
    }  
}
