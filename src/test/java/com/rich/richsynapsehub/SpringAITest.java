package com.rich.richsynapsehub;

import cn.hutool.core.util.RandomUtil;
import com.rich.richsynapsehub.utils.doChat.SpringAiChat;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpringAITest {
    @Resource
    private SpringAiChat springAiChat;

    /**
     * 多轮对话测试
     *
     * @return void
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Test
    void doChat() {
        // 对话房间号
        String conversationId = RandomUtil.randomString(5);
        String q1 = "|";
        String q2 = "开始面试";
        String q3 = "我叫小明，是一个java开发工程师";
        // 指定同一个会话 ID
        String res1 = springAiChat.chat(q1, conversationId);
        if( res1.isEmpty() ) {
            throw new RuntimeException("res1 is empty");
        }
        String res2 = springAiChat.chat(q2, conversationId);
        if( res2.isEmpty() ) {
            throw new RuntimeException("res2 is empty");
        }
        String res3 = springAiChat.chat(q3, conversationId);
        if( res3.isEmpty() ) {
            throw new RuntimeException("res3 is empty");
        }
    }

    /**
     * 结构化输出测试
     *
     * @return void
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Test
    void doStructuredOutputChat() {
        String q1 = "你好，我是一个刚毕业的金融专业学生，学历是本科";
        // 指定同一个会话 ID
        springAiChat.structuredOutputChat(q1, "001");
    }
}
