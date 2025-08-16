package indi.yunherry.weather.compact.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ContraptionHitResult extends BlockHitResult {
    public final Vec3 contactPointMotion;
    public final AbstractContraptionEntity contraption;
    public final Vec3 localHitPosition;
    public final BlockState actualBlockState;

    public ContraptionHitResult(Vec3 contactPointMotion, Vec3 location, Direction direction,
                                BlockPos blockPos, boolean inside, AbstractContraptionEntity contraption,
                                Vec3 localHitPosition, BlockState actualBlockState) {
        super(location, direction, blockPos, inside);
        this.contactPointMotion = contactPointMotion;
        this.contraption = contraption;
        this.localHitPosition = localHitPosition;
        this.actualBlockState = actualBlockState;
    }
}