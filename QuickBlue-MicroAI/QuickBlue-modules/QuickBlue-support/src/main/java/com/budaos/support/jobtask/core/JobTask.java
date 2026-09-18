package com.budaos.support.jobtask.core;

/**
 * 定时任务 执行接口
 * 所有定时任务都需要实现此接口
 *
 * @author budaos
 */
public interface JobTask {

    /**
     * 默认方法
     * 获取当前任务类名
     *
     * @return 类名
     */
    default String getClassName() {
        return this.getClass().getName();
    }

    Integer getJobId();

    /**
     * 执行定时任务
     *
     * @param param 可选参数 任务不需要时不用管
     * @return 执行结果
     */
    String run(String param);
}
