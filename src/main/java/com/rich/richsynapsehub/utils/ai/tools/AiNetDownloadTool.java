package com.rich.richsynapsehub.utils.ai.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

import static com.rich.richsynapsehub.constant.FilePathConstant.AI_TOOL_FILE_DOWNLOAD_DIR;

/**
 * 供 AI 调用的网络内容下载工具
 *
 * @return
 * @author DuRuiChi
 * @create 2025/7/6
 **/
public class AiNetDownloadTool {

    @Tool(description = "Download content by specifying the download URL.")
    public String download(@ToolParam(description = "The download URL address for the content.") String url,
                           @ToolParam(description = "Save the file name of the downloaded content.") String fileName) {
        String fileDir = AI_TOOL_FILE_DOWNLOAD_DIR;
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);
            HttpUtil.downloadFile(url, new File(filePath));
            return "Successfully downloaded to: " + filePath;
        } catch (Exception e) {
            return "Download failed: " + e.getMessage();
        }
    }
}
