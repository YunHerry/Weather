package indi.yunherry.weather;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RayThreadPool {
    private static final ExecutorService pool = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors() - 1));

    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!pool.isShutdown()) {
                pool.shutdownNow();
            }
        }));
    }

    public static void submitTask(Runnable task) {
        pool.submit(task);
    }

    public static void shutdown() {
        pool.shutdown();
    }

    public static int count() {
        return ((ThreadPoolExecutor) pool).getQueue().size();
    }
}
