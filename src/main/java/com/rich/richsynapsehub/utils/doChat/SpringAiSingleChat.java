package com.rich.richsynapsehub.utils.doChat;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;


/**
 * Spring AI 单次对话连
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/3
 **/
//@Component
public class SpringAiSingleChat implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) {
        AssistantMessage output = dashscopeChatModel.call(new Prompt("什么是 HTTP 协议？"))
                .getResult()
                .getOutput();
        System.out.println(output.getText());
    }
}
