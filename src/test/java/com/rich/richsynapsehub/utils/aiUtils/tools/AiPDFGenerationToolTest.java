package com.rich.richsynapsehub.utils.aiUtils.tools;

import com.rich.richsynapsehub.utils.ai.tools.AiPDFGenerationTool;
import org.junit.jupiter.api.Test;

class AiPDFGenerationToolTest {

    @Test
    void generatePDF() {
        AiPDFGenerationTool tool = new AiPDFGenerationTool();
        tool.generatePDF("tset01.pdf", "这是一个 PDF 文件的测试内容，这是一个 PDF 文件的测试内容，这是一个 PDF 文件的测试内容，这是一个 PDF 文件的测试内容，这是一个 PDF 文件的测试内容");
    }
}