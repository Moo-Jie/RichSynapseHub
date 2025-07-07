package com.rich.richsynapsehub.utils.ai.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import static com.rich.richsynapsehub.constant.FilePathConstant.AI_TOOL_FILE_PDF_DIR;

/**
 * 供 AI 调用的 PDF 生成工具
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/6
 **/
public class AiPDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(@ToolParam(description = "Name of the file to save the generated PDF") String fileName,
                              @ToolParam(description = "Content to be included in the PDF") String content) {
        // 文件名后缀检查
        if (!fileName.toLowerCase().endsWith(".pdf")) {
            fileName += ".pdf";
        }
        // 构建文件路径
        String fileDir = AI_TOOL_FILE_PDF_DIR;
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);
            // 创建PDF文档
            try (PdfWriter writer = new PdfWriter(filePath); PdfDocument pdf = new PdfDocument(writer)) {

                // 设置 PDF 文档元数据
                PdfDocumentInfo info = pdf.getDocumentInfo();
                info.setTitle("AI Generated PDF");
                info.setAuthor("RichSynapseHub System");

                // 创建文档对象
                try (Document document = new Document(pdf)) {
                    // 中文字体
                    PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H");
                    document.setFont(font);

                    // 设置段落内容与格式
                    Paragraph paragraph = new Paragraph(content).setTextAlignment(TextAlignment.JUSTIFIED).setFontSize(12).setMarginTop(20);
                    // 写入文档
                    document.add(paragraph);
                }
            }
            return "PDF successfully generated to: " + filePath;
        } catch (Exception e) {
            e.printStackTrace();
            return "PDF generation failed: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }
}
