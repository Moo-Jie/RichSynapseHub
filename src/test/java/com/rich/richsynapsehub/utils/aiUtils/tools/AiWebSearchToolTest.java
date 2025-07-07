package com.rich.richsynapsehub.utils.aiUtils.tools;

import com.rich.richsynapsehub.utils.ai.tools.AiWebSearchTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiWebSearchToolTest {

    @Autowired
    private AiWebSearchTool searchTools;

    @Test
    void searchWeb() {
        String result = searchTools.searchWeb("什么是快乐牌刀片?");
        System.out.println(result);
    }
}