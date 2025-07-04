package com.rich.richsynapsehub;

import com.rich.richsynapsehub.controller.test.SpringAiChat;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SpringAITest {
    @Resource
    private SpringAiChat springAiChat;

    @Test
    void doChat() {
        String q1 = "你好，我是一个刚毕业的金融专业学生，学历是本科";
        String q2 = "请你基于我的学历，为我做职业规划";
        String q3 = "最后，你进行简练的总结！";
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

        System.out.println("——" + q1 + "\n" + "——" + res1 + "\n\n\n" + "——" + q2 + "\n" + "——" + res2 + "\n\n\n" + "——" + q3 + "\n" + "——" + res3);
    }
}
