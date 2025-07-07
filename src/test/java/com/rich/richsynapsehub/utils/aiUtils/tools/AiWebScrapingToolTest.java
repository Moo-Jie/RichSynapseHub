package com.rich.richsynapsehub.utils.aiUtils.tools;

import com.rich.richsynapsehub.utils.ai.tools.AiWebScrapingTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiWebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        AiWebScrapingTool tool = new AiWebScrapingTool();
        String res = tool.scrapeWebPage("https://github.com/");
        System.out.println(res);
    }
}