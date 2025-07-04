package com.rich.richsynapsehub;

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
        String q1 = "你好，我是一个刚毕业的金融专业学生，学历是本科";
        String q2 = "请你基于我的学历，为我做职业规划";
        String q3 = "最后，你进行简练的总结！";
        // 指定同一个会话 ID
        String res1 = springAiChat.chat(q1, "001");
        if( res1.isEmpty() ) {
            throw new RuntimeException("res1 is empty");
        }
        String res2 = springAiChat.chat(q2, "001");
        if( res2.isEmpty() ) {
            throw new RuntimeException("res2 is empty");
        }
        String res3 = springAiChat.chat(q3, "001");
        if( res3.isEmpty() ) {
            throw new RuntimeException("res3 is empty");
        }
    }

    /**
     * 再次执行对话，测试是否会记忆上次的对话
     *
     * @return void
     * @author DuRuiChi
     * @create 2025/7/4
     **/
    @Test
    void doChatAgain() {
        String q1 = "请你再次基于我的学历，为我做职业规划";
        // 指定同一个会话 ID
        String res1 = springAiChat.chat(q1, "001");
        if( res1.isEmpty() ) {
            throw new RuntimeException("res1 is empty");
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
