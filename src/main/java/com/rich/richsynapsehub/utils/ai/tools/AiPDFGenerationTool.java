package com.rich.richsynapsehub.utils.ai.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.rich.richsynapsehub.constant.FilePathConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class AiPDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content")
    public String generatePDF(@ToolParam(description = "Name of the file to save the generated PDF") String fileName,
                              @ToolParam(description = "Content to be included in the PDF") String content) {
        String fileDir = FilePathConstant.AI_TOOL_FILE_PDF_DIR;
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                PdfFont font = PdfFontFactory.createFont(
                        "STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                );

                Paragraph paragraph = new Paragraph(content)
                        .setFont(font)
                        .setFontSize(12);

                document.add(paragraph);
            }
            return "PDF生成成功，请前往我的文件查看！";
        } catch (IOException e) {
            return "PDF生成错误: " + e.getMessage();
        }
    }
}
