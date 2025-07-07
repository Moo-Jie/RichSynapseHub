package com.rich.richsynapsehub.utils.aiUtils.tools;

import com.rich.richsynapsehub.utils.ai.tools.AiFileWRTool;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class AiFileWRToolTest {

    @Test
    void readFile() {
        AiFileWRTool aiFileWRTool = new AiFileWRTool();
        String res = aiFileWRTool.readFile("JAVA_21的特性文档.txt");
        System.out.println(res);
    }

    @Test
    void writeFile() throws IOException {
        AiFileWRTool aiFileWRTool = new AiFileWRTool();
        String res = aiFileWRTool.writeFile("JAVA_21的特性文档.txt", "JAVA 21 的特性有： 1. 记录模式匹配 2. 模式匹配的解构 3. 文本块 4. 弃用的类和方法 5. 其他特性 6. 其他特性 7. 其他特性 8. 其他特性");
        System.out.println(res);
    }
}