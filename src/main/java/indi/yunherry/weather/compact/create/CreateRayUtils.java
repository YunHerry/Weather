package indi.yunherry.weather.compact.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionHandlerClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CreateRayUtils {

    private static final Logger log = LoggerFactory.getLogger(CreateRayUtils.class);


    public static BlockHitResult clipWithContraptions(ClientLevel level, Vec3 start, Vec3 end,
                                                      ClipContext.Block blockMode,
                                                      ClipContext.Fluid fluidMode) {
        ClipContext vanillaContext = new ClipContext(start, end, blockMode, fluidMode, null);
        BlockHitResult vanillaHit = level.clip(vanillaContext);
        double closestDistance = vanillaHit.getLocation().distanceToSqr(start);
        double shortestDistance = Double.MAX_VALUE;
        BlockHitResult contraptionResult = null;
        Vec3 contraptionHitPos = null;
        AbstractContraptionEntity hitContraption = null;

        for (Iterator<AbstractContraptionEntity> it = CreateUtils.forEachContraption(level); it.hasNext(); ) {
            AbstractContraptionEntity entity = it.next();

            AABB entityBb = entity.getBoundingBox();
            if (!entityBb.intersects(start, end)) {
                continue;
            }

            BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(start, end, entity);

            if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
                Vec3 worldHit = entity.toGlobalVector(hitResult.getLocation(), 1.0F);
                double hitDistance = start.distanceToSqr(worldHit);

                if (hitDistance < shortestDistance) {
                    shortestDistance = hitDistance;
                    contraptionResult = hitResult;
                    contraptionHitPos = worldHit;
                    hitContraption = entity;
                }
            }
        }

        if (contraptionResult != null && contraptionHitPos != null) {
            double contraptionDistance = contraptionHitPos.distanceToSqr(start);
            if (contraptionDistance < closestDistance) {
                BlockState actualBlockState = getActualBlockState(hitContraption, contraptionResult.getBlockPos());
                Vec3 contactPointMotion = hitContraption.getContactPointMotion(contraptionHitPos);

                return new ContraptionHitResult(
                        contactPointMotion,
                        contraptionHitPos,
                        contraptionResult.getDirection(),
                        CreateUtils.localToWorldBlockPos(hitContraption, contraptionResult.getBlockPos()),
                        contraptionResult.isInside(),
                        hitContraption,
                        contraptionResult.getLocation(),
                        actualBlockState
                );
            }
        }

        return vanillaHit;
    }

    public static BlockHitResult clipWithContraptions(ClientLevel level, Vec3 start, Vec3 end) {
        return clipWithContraptions(level, start, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE);
    }

    private static BlockState getActualBlockState(AbstractContraptionEntity contraptionEntity, BlockPos localPos) {
        try {
            Contraption contraption = contraptionEntity.getContraption();
            Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks = contraption.getBlocks();

            StructureTemplate.StructureBlockInfo blockInfo = blocks.get(localPos);
            if (blockInfo != null) {
                BlockState blockState = blockInfo.state();
                if (blockState != null && !blockState.isAir() && blockState.getBlock() != Blocks.VOID_AIR) {
                    return blockState;
                }
            }

            if (contraption.getContraptionWorld() != null) {
                BlockState worldState = contraption.getContraptionWorld().getBlockState(localPos);
                if (worldState != null && !worldState.isAir() && worldState.getBlock() != Blocks.VOID_AIR) {
                    return worldState;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("获取方块状态失败: {}",e.getMessage());
            return null;
        }
    }


}