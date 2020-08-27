package cn.hyg.client.job;

import cn.hyg.client.util.HyExceptionHandler;
import cn.hyg.client.view.TaskView;
import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * created by ${user} on ${date}
 * 消息队列：单例模式
 */
public class JobThreadPool {

    private static final int POOL_SIZE = 1;
    private ExecutorService executorService;

    private JobThreadPool() {
        executorService = Executors.newFixedThreadPool(POOL_SIZE);
    }

    private static class ThreadPoolHandlerFactory {
        private static JobThreadPool jobThreadPoolHandler = new JobThreadPool();
    }

    public static JobThreadPool getInstance() {
        return ThreadPoolHandlerFactory.jobThreadPoolHandler;
    }

    public void destroy() {
        if (null != executorService) {
            executorService.shutdown();
        }
    }

    public void addTask(JobInstance ji, TaskView taskView) {
        // 每次FTP下载时间间隔
        CompletableFuture.supplyAsync(ji, executorService).whenComplete((result, e) -> {
            Platform.runLater(() -> {
                taskView.initList();
            });
        }).exceptionally((e) -> {
            System.out.println("exception:" + e);
            return "exception";
        });

    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
