package indi.yunherry.weather;

import indi.yunherry.weather.duck.ICustomTick;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jline.utils.Log;

import java.util.*;
import java.util.concurrent.*;

@Mod.EventBusSubscriber
public class CustomBlockEntityThreadPool {
    private static final ExecutorService pool = new ThreadPoolExecutor(
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(200),
            new ThreadPoolExecutor.DiscardOldestPolicy()
    ) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);

            // 如果 t 为空，说明没有直接抛出异常，但如果是 FutureTask，异常可能藏在里面
            if (t == null && r instanceof Future<?>) {
                try {
                    Object result = ((Future<?>) r).get();
                } catch (CancellationException ce) {
                    t = ce;
                } catch (ExecutionException ee) {
                    t = ee.getCause(); // 获取真正的异常
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                }
            }

            // 如果捕获到了异常，打印到 Minecraft 日志中
            if (t != null) {
                Log.error("线程池异步任务发生严重错误!", t);
                // 这里的 t 就是你要捕获的错误，包含了完整的堆栈信息
            }
        }
    };

    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!pool.isShutdown()) {
                pool.shutdownNow();
            }
        }));
    }

    private static final HashMap<ICustomTick, Queue<TickBlockInfo>> tickingBlockEntity = new HashMap<ICustomTick, Queue<TickBlockInfo>>();

    public static void customTicks() {
        ClientLevel level = GlobalContext.level;
        for (Map.Entry<ICustomTick, Queue<TickBlockInfo>> entry : tickingBlockEntity.entrySet()) {
            ICustomTick customTick = entry.getKey();
            Iterator<TickBlockInfo> iterator = tickingBlockEntity.computeIfAbsent(customTick, k -> new ConcurrentLinkedQueue<>()).iterator();

            while (iterator.hasNext()) {
                TickBlockInfo tickingBlock = iterator.next();
                if (!GlobalContext.level.isLoaded(tickingBlock.pos())) {
                    iterator.remove();
                    return;
                }
                customTick.weather$tick(level,tickingBlock);
            }
        }
    }

    public static void submitTicker(ICustomTick tickerType, BlockPos pos, BlockState state) {
        Queue<TickBlockInfo> tickBlockInfoList = tickingBlockEntity.computeIfAbsent(tickerType, k -> new ConcurrentLinkedQueue<>());
        tickBlockInfoList.add(new TickBlockInfo(pos, state));
    }

    public static void shutdown() {
        pool.shutdown();
    }

    public static int count() {
        return ((ThreadPoolExecutor) pool).getQueue().size();
    }

    //跑图的时候对帧率有影响?
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ClientLevel level)) return;
        if (!event.isNewChunk()) {
            pool.submit(() -> {
                LevelChunk chunk = (LevelChunk) event.getChunk();
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                            pos.set(
                                    chunk.getPos().getMinBlockX() + x,
                                    y,
                                    chunk.getPos().getMinBlockZ() + z
                            );

                            BlockState state = chunk.getBlockState(pos);
                            if (state.is(Blocks.BUBBLE_COLUMN)) {
                                if (state.getBlock() instanceof ICustomTick customTick && level.getBlockState(pos.above()).isAir()) {
                                    submitTicker(customTick, pos.immutable(), state);
                                }
                            } else if (state.is(Blocks.TALL_SEAGRASS)) {
                                if (state.getBlock() instanceof ICustomTick customTick) {
                                    submitTicker(customTick, pos.immutable(), state);
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}

