package com.rich.richsynapsehub.utils.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 供 AI 调用的终端操作工具
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/7
 **/
public class AiTerminalOperationTool {

    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(
            @ToolParam(description = "Command to execute in the terminal") String command) {

        // 使用 StringBuilder 高效拼接输出结果
        StringBuilder output = new StringBuilder();

        try {
            // 创建进程构建器（Windows 环境）
            //   "cmd.exe": Windows 命令行解释器
            //   "/c": 执行后关闭终端
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", command);

            // 启动进程并执行命令
            Process process = builder.start();

            // 读取命令的标准输出流（使用 try-with-resources 自动关闭流）
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                // 逐行读取输出内容，每行后添加换行符保持格式
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待命令执行完成（阻塞直至进程结束）
            int exitCode = process.waitFor();

            // 检查退出状态码（0 表示正常退出）
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ")
                        .append(exitCode);
            }

        } catch (IOException e) {
            // 处理 I/O 异常（如无法创建进程/读取流）
            output.append("Error executing command: ")
                    .append(e.getMessage());
        } catch (InterruptedException e) {
            // 处理进程等待被中断的情况
            output.append("Command execution interrupted: ")
                    .append(e.getMessage());
            // 恢复中断状态（保持线程中断标志）
            Thread.currentThread().interrupt();
        }

        return output.toString();
    }
}
