package indi.yunherry.weather;

import indi.yunherry.weather.duck.ICustomTick;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;
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

    private static final Map<ChunkPos, Map<ICustomTick, Queue<TickBlockInfo>>> tickingBlockEntity = new ConcurrentHashMap<>();

    public static void customTicks() {
        ClientLevel level = GlobalContext.level;
        if (level == null) return;

        tickingBlockEntity.forEach((chunkPos, tickerMap) -> {
            if (!level.getChunkSource().hasChunk(chunkPos.x, chunkPos.z)) return;

            tickerMap.forEach((customTick, queue) -> {
                if (queue.isEmpty()) return;
                for (TickBlockInfo tickingBlock : queue) {
                    customTick.weather$tick(level, tickingBlock);
                }
            });
        });
    }

    public static void submitTicker(ICustomTick tickerType, ChunkPos chunkPos, BlockPos pos, BlockState state) {
        Map<ICustomTick, Queue<TickBlockInfo>> tickBlockInfoChunkList = tickingBlockEntity.computeIfAbsent(chunkPos, k -> new HashMap<>());
        Queue<TickBlockInfo> tickBlockInfoList = tickBlockInfoChunkList.computeIfAbsent(tickerType, k -> new ConcurrentLinkedQueue<>());
        tickBlockInfoList.add(new TickBlockInfo(pos, state));
    }

    public static void submitTicker(ICustomTick tickerType, BlockPos pos, BlockState state) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Map<ICustomTick, Queue<TickBlockInfo>> tickBlockInfoChunkList =
                tickingBlockEntity.computeIfAbsent(chunkPos, k -> new ConcurrentHashMap<>());
        Queue<TickBlockInfo> tickBlockInfoList =
                tickBlockInfoChunkList.computeIfAbsent(tickerType, k -> new ConcurrentLinkedQueue<>());
        tickBlockInfoList.add(new TickBlockInfo(pos, state));
    }

    public static void removeTickerAt(BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        Map<ICustomTick, Queue<TickBlockInfo>> chunkTickers = tickingBlockEntity.get(chunkPos);
        if (chunkTickers != null) {
            chunkTickers.values().forEach(queue -> queue.removeIf(info -> info.pos().equals(pos)));
        }
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
        pool.submit(() -> {
            LevelChunk chunk = (LevelChunk) event.getChunk();
            ChunkPos chunkPos = chunk.getPos();
            LevelChunkSection[] sections = chunk.getSections();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int minBuildHeight = level.getMinBuildHeight();
            int chunkMinX = chunk.getPos().getMinBlockX();
            int chunkMinZ = chunk.getPos().getMinBlockZ();
            for (int i = 0; i < sections.length; i++) {
                LevelChunkSection section = sections[i];

                if (section.hasOnlyAir()) continue;
                int secMinY = minBuildHeight + (i * 16);

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 16; y++) {
                            BlockState state = section.getBlockState(x, y, z);
                            if (state.isAir()) continue;
                            if (state.is(Blocks.BUBBLE_COLUMN) || state.is(Blocks.TALL_SEAGRASS)) {
                                pos.set(chunkMinX + x, secMinY + y, chunkMinZ + z);
                                if (state.is(Blocks.BUBBLE_COLUMN)) {
                                    if (state.getBlock() instanceof ICustomTick customTick && level.getBlockState(pos.above()).isAir()) {
                                        submitTicker(customTick, chunkPos, pos.immutable(), state);
                                    }
                                } else if (state.is(Blocks.TALL_SEAGRASS)) {
                                    if (state.getBlock() instanceof ICustomTick customTick) {
                                        submitTicker(customTick, chunkPos, pos.immutable(), state);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            tickingBlockEntity.remove(event.getChunk().getPos());
        }
    }
}

