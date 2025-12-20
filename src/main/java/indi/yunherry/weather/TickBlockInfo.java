package indi.yunherry.weather;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record TickBlockInfo(BlockPos pos,
                            BlockState state) {
}
