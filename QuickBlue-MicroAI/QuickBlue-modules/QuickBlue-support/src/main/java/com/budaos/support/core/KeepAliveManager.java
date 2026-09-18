package com.budaos.support.core;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 心跳核心调度管理器
 *
 * @author budaos
 */
public class KeepAliveManager {

    private static final String THREAD_NAME_PREFIX = "quickblue-heart-beat";
    private static final int THREAD_COUNT = 1;
    private static final long INITIAL_DELAY = 60 * 1000L;

    private ScheduledThreadPoolExecutor threadPoolExecutor;

    /**
     * 服务状态持久化处理类
     */
    private IKeepAliveRecordHandler heartBeatRecordHandler;

    /**
     * 调度配置信息
     */
    private long intervalMilliseconds;

    /**
     * 构造函数
     *
     * @param intervalMilliseconds    间隔执行时间(毫秒)
     * @param heartBeatRecordHandler 心跳记录处理器
     */
    public KeepAliveManager(Long intervalMilliseconds,
                            IKeepAliveRecordHandler heartBeatRecordHandler) {
        this.intervalMilliseconds = intervalMilliseconds;
        this.heartBeatRecordHandler = heartBeatRecordHandler;
        //使用守护线程去处理
        this.threadPoolExecutor = new ScheduledThreadPoolExecutor(THREAD_COUNT, r -> {
            Thread t = new Thread(r, THREAD_NAME_PREFIX);
            if (!t.isDaemon()) {
                t.setDaemon(true);
            }
            return t;
        });
        // 开始心跳
        this.beginHeartBeat();
    }

    /**
     * 开启心跳
     */
    private void beginHeartBeat() {
        KeepAliveRunnable heartBeatRunnable = new KeepAliveRunnable(heartBeatRecordHandler);
        threadPoolExecutor.scheduleWithFixedDelay(heartBeatRunnable, INITIAL_DELAY, intervalMilliseconds, TimeUnit.MILLISECONDS);
    }
}
