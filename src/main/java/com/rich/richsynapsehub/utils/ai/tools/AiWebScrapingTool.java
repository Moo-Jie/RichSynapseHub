package com.rich.richsynapsehub.utils.ai.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 供 AI 调用的网页抓取工具，通过SearchAPI集成百度搜索引擎
 *
 * @return
 * @author DuRuiChi
 * @create 2025/7/6
 **/
public class AiWebScrapingTool {
    @Tool(description = "Crawl webpage content.")
    public String scrapeWebPage(@ToolParam(description = "The URL of the webpage to be crawled.") String ScrapUrl) {
        try {
            Document doc = Jsoup.connect(ScrapUrl).get();
            return doc.html();
        } catch (IOException e) {
            return "Web crawling failed: " + e.getMessage();
        }
    }
}
