package com.rich.richsynapsehub.agent;

/**
 * ReAct 模式下的 Agent 抽象类 ， 继承 BaseAgent 接口
 * Reasoning and Acting ，即思考和行动的循环模式
 * 参考 OpenManus：https://github.com/FoundationAgents/OpenManus
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/7
 **/
public abstract class ReActModeAgent extends BaseAgent {
    /**
     * Reasoning：执行当前任务的思考过程，并决定是否需要执行行动
     *
     * @return boolean
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    public abstract boolean think();

    /**
     * Acting：执行行动并返回执行结果
     *
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    public abstract String act();

    /**
     * 对于 Agent Loop 中某次的 Step，都要分解为 ReAct 模式下的思考和行动流程
     *
     * @return java.lang.String
     * @author DuRuiChi
     * @create 2025/7/7
     **/
    @Override
    public String step() {
        try {
            // Reasoning：执行当前任务的思考过程，并决定是否需要执行行动
            boolean isAct = think();
            if (isAct) {
                // Acting：执行行动并返回执行结果
                return act();
            }
            return "本次思考完成，无需行动。\n ";
        } catch (Exception e) {
            // 执行失败
            return "当前 Step 执行失败：" + e.getMessage();
        }
    }
}
