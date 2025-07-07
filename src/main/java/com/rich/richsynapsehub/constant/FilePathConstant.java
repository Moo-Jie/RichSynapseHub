package com.rich.richsynapsehub.constant;

/**
 * AI 工具生成文件保存路径常量
 *
 * @return
 * @author DuRuiChi
 * @create 2025/7/7
 **/
public interface FilePathConstant {

    /**
     * 对话记录本地保存路径
     */
    String CHAT_FILE_SAVE_DIR = System.getProperty("user.dir") + "/sysFiles/chatMemory";

    /**
     * AI 调用工具生成文件保存路径
     */
    String AI_TOOL_FILE_CREATE_DIR = System.getProperty("user.dir") + "/sysFiles/aiFiles/create";

    /**
     * AI 调用工具下载文件保存路径
     */
    String AI_TOOL_FILE_DOWNLOAD_DIR = System.getProperty("user.dir") + "/sysFiles/aiFiles/download";

    /**
     * AI PDF 生成保存路径
     */
    String AI_TOOL_FILE_PDF_DIR = System.getProperty("user.dir") + "/sysFiles/aiFiles/pdf";
}
