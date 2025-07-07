package com.rich.richsynapsehub.enumeration;

/**
 * agent 执状态
 *
 * @author DuRuiChi
 * @return
 * @create 2025/7/7
 **/
public enum AgentExecutionState {

    /**
     * 空闲
     */
    IDLE,

    /**
     * 运行中
     */
    RUNNING,

    /**
     * 已完成
     */
    FINISHED,

    /**
     * 错误
     */
    ERROR
}
