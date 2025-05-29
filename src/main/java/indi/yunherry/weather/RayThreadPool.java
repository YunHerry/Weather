package indi.yunherry.weather;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class RayThreadPool {
    private static final ExecutorService pool = Executors.newFixedThreadPool(4);
    public static void submitTask(Runnable task) {
        pool.submit(task);
    }

    public static void shutdown() {
        pool.shutdown();
    }
    public static int count() {
        return ((ThreadPoolExecutor)pool).getQueue().size();
    }
}
