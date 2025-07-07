package com.rich.richsynapsehub.utils.ai.tools;

import cn.hutool.core.io.FileUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import static com.rich.richsynapsehub.constant.FilePathConstant.AI_TOOL_FILE_CREATE_DIR;

/**
 * 供 AI 调用的文件读写工具
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/6
 **/
public class AiFileWRTool {
    /**
     * AI 读取文件
     *
     * @param fileName
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/5
     **/
    @Tool(description = "Read the contents of the specified path file.")
    public String readFile(@ToolParam(description = "The name of the read file.") String fileName) {
        String fileDir = AI_TOOL_FILE_CREATE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(fileDir);
        } catch (Exception e) {
            return "File not found or read error.";
        }
    }

    /**
     * AI 写入文件
     *
     * @param fileName
     * @param content
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/5
     **/
    @Tool(description = "Write the contents of the specified path file.")
    public String writeFile(@ToolParam(description = "The name of the write file.") String fileName,
                            @ToolParam(description = "The contents of the write file.") String content) {
        String fileDir = AI_TOOL_FILE_CREATE_DIR + "/" + fileName;
        try {
            FileUtil.writeUtf8String(content, fileDir);
            return "The file was successfully written to “ " + fileName + " ”.";
        } catch (Exception e) {
            return "File write error.";
        }
    }
}
