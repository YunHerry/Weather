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
    );

    public static void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!pool.isShutdown()) {
                pool.shutdownNow();
            }
        }));
    }

    private static final HashMap<ICustomTick, Queue<TickBlockInfo>> tickingBlockEntity = new HashMap<ICustomTick, Queue<TickBlockInfo>>();

    public static void customTicks() {

        for (Map.Entry<ICustomTick, Queue<TickBlockInfo>> entry : tickingBlockEntity.entrySet()) {
            ICustomTick customTick = entry.getKey();
            Iterator<TickBlockInfo> iterator = tickingBlockEntity.computeIfAbsent(customTick, k -> new ConcurrentLinkedQueue<>()).iterator();

            while (iterator.hasNext()) {
                TickBlockInfo tickingBlock = iterator.next();
                if (!GlobalContext.level.isLoaded(tickingBlock.pos())) {
                    iterator.remove();
                    return;
                }
                customTick.weather$tick(tickingBlock);
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
                            }
                        }
                    }
                }
            });
        }
    }
}

