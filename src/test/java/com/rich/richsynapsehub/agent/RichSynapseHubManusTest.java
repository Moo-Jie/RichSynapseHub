package com.rich.richsynapsehub.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RichSynapseHubManusTest {

    @Resource
    private RichSynapseHubManus richSynapseHubManus;

    @Test
    void run() {
        String userPrompt = "我要去聊城市高唐县游玩，请为我做一份旅游攻略，并将最终结果总结为 MD 格式文件 “旅游攻略.md”！";
        String res = richSynapseHubManus.run(userPrompt);
        System.out.println(res);
    }
}