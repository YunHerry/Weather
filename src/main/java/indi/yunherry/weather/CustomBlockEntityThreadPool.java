package indi.yunherry.weather;

import indi.yunherry.weather.duck.ICustomTick;
import net.minecraft.client.Minecraft;
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
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber
public class CustomBlockEntityThreadPool {

    // 优化线程池：使用守护线程，避免阻塞 JVM 关闭
    private static final ExecutorService pool = new ThreadPoolExecutor(
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(500),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Weather-Scanner-" + count.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.DiscardOldestPolicy()
    );

    public static void init() {
        // 显式关闭
        Runtime.getRuntime().addShutdownHook(new Thread(CustomBlockEntityThreadPool::shutdown));
    }

    private static final Map<ChunkPos, Map<ICustomTick, Queue<TickBlockInfo>>> tickingBlockEntity = new ConcurrentHashMap<>();

    /**
     * 此方法应在主线程的 Tick 事件中调用 (例如 ClientTickEvent 或 RenderLevelStageEvent)
     */
    public static void customTicks() {
        ClientLevel level = GlobalContext.level;
        if (level == null) return;

        tickingBlockEntity.forEach((chunkPos, tickerMap) -> {
            // 检查区块是否仍然加载，防止对卸载区域进行操作
            if (!level.getChunkSource().hasChunk(chunkPos.x, chunkPos.z)) return;

            tickerMap.forEach((customTick, queue) -> {
                if (queue.isEmpty()) return;
                // 注意：如果 ticker 会修改集合，需考虑 ConcurrentLinkedQueue 的弱一致性迭代器
                for (TickBlockInfo tickingBlock : queue) {
                    customTick.weather$tick(level, tickingBlock);
                }
            });
        });
    }
    public static void submitTicker(ICustomTick tickerType, BlockPos pos, BlockState state) {
        submitTicker(tickerType, new ChunkPos(pos), pos, state);
    }
    public static void submitTicker(ICustomTick tickerType, ChunkPos chunkPos, BlockPos pos, BlockState state) {
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
        if (!pool.isShutdown()) {
            pool.shutdownNow();
        }
    }

    public static int count() {
        return ((ThreadPoolExecutor) pool).getQueue().size();
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        // 仅在客户端 ClientLevel 处理
        if (!(event.getLevel() instanceof ClientLevel level)) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        ChunkPos chunkPos = chunk.getPos();
        LevelChunkSection[] sections = chunk.getSections();
        int minBuildHeight = level.getMinBuildHeight();
        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        pool.submit(() -> {
            List<Candidate> candidates = new ArrayList<>();

            // 1. 异步遍历：只读 Section 里的数据，不碰 level 的任何方法
            for (int i = 0; i < sections.length; i++) {
                LevelChunkSection section = sections[i];
                if (section == null || section.hasOnlyAir()) continue;

                int secMinY = minBuildHeight + (i * 16);

                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            BlockState state = section.getBlockState(x, y, z);

                            // 粗筛选
                            if (state.is(Blocks.BUBBLE_COLUMN) || state.is(Blocks.TALL_SEAGRASS) || state.is(Blocks.SEAGRASS) || state.is(Blocks.KELP)|| state.is(Blocks.KELP_PLANT)) {
                                BlockPos pos = new BlockPos(chunkMinX + x, secMinY + y, chunkMinZ + z);
                                candidates.add(new Candidate(pos, state));
                            }
                        }
                    }
                }
            }

            // 2. 将找到的候选方块交还给主线程验证（解决多线程 RandomSource 崩溃的关键）
            if (!candidates.isEmpty()) {
                Minecraft.getInstance().execute(() -> {
                    // 确保玩家还在世界中且区块没被瞬间卸载
                    if (GlobalContext.level == null) return;

                    for (Candidate c : candidates) {
                        if (c.state.is(Blocks.BUBBLE_COLUMN)) {
                            if (c.state.getBlock() instanceof ICustomTick customTick &&
                                    level.getBlockState(c.pos.above()).isAir()) {
                                submitTicker(customTick, chunkPos, c.pos, c.state);
                            }
                        } else if (c.state.is(Blocks.TALL_SEAGRASS)) {
                            if (c.state.getBlock() instanceof ICustomTick customTick) {
                                submitTicker(customTick, chunkPos, c.pos, c.state);
                            }
                        } else if (c.state.is(Blocks.SEAGRASS)) {
                            if (c.state.getBlock() instanceof ICustomTick customTick) {
                                submitTicker(customTick, chunkPos, c.pos, c.state);
                            }
                        } else if (c.state.is(Blocks.KELP)) {
                            if (c.state.getBlock() instanceof ICustomTick customTick) {
                                submitTicker(customTick, chunkPos, c.pos, c.state);
                            }
                        }else if (c.state.is(Blocks.KELP_PLANT)) {
                            if (c.state.getBlock() instanceof ICustomTick customTick) {
                                submitTicker(customTick, chunkPos, c.pos, c.state);
                            }
                        }
                    }
                });
            }
        });
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide()) {
            tickingBlockEntity.remove(event.getChunk().getPos());
        }
    }

    /**
     * 临时记录类，用于跨线程传递数据
     */
    private record Candidate(BlockPos pos, BlockState state) {}
}
