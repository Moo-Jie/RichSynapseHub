package com.rich.richsynapsehub.utils.aiUtils.tools;

import com.rich.richsynapsehub.utils.ai.tools.AiNetDownloadTool;
import org.junit.jupiter.api.Test;

class AiNetDownloadToolTest {

    @Test
    void download() {
        AiNetDownloadTool downloadTool = new AiNetDownloadTool();
        downloadTool.download("https://rich-tams.oss-cn-beijing.aliyuncs.com/RichInterview/68287d91edb5c5b7e1d9ad29.png",
                "User.png");
    }
}