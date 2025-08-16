package indi.yunherry.weather;

import java.util.concurrent.*;

public class RayThreadPool {
    private static final ExecutorService pool = new ThreadPoolExecutor(
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(20),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

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
