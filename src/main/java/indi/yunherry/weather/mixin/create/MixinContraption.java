package indi.yunherry.weather.mixin.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import indi.yunherry.weather.compact.create.ContraptionAddon;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Based on CreateUtil from AsyncParticles by qu-an
 * <a href="https://github.com/Harveykang/AsyncParticles">github</a>
 */
@Mixin(Contraption.class)
public class MixinContraption implements ContraptionAddon {
    @Shadow(remap = false)
    protected Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks;
    @Shadow(remap = false)
    protected ContraptionWorld collisionLevel;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Shadow(remap = false)
    public Optional<List<AABB>> simplifiedEntityColliders;

    @Unique
    private volatile List<AABB> weather$aabbs;
    @Unique
    private final Object weather$lock = new Object();

    @Unique
    private static final VoxelShape EMPTY_SHAPE = net.minecraft.world.phys.shapes.Shapes.empty();


    @WrapOperation(method = "gatherBBsOffThread", remap = false, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> gatherBBsOffThread(CompletableFuture<?> instance, Consumer<?> action, Operation<CompletableFuture<Void>> original) {
        return original.call(instance, action).thenRun(() -> {
            weather$aabbs = null;
        });
    }

    @Override
    public List<AABB> weather$getAabbs() {
        if (simplifiedEntityColliders.isPresent()) {
            return simplifiedEntityColliders.get();
        }

        List<AABB> result = weather$aabbs;

        if (result != null) {
            return result;
        }

        synchronized (weather$lock) {
            result = weather$aabbs;
            if (result != null) {
                return result;
            }

            int estimatedSize = Math.max(16, blocks.size() / 4);
            List<AABB> aabbs = new ArrayList<>(estimatedSize);

            ContraptionWorld cachedWorld = this.collisionLevel;

            for (Map.Entry<BlockPos, StructureTemplate.StructureBlockInfo> entry : blocks.entrySet()) {
                StructureTemplate.StructureBlockInfo info = entry.getValue();
                BlockPos localPos = entry.getKey();

                net.minecraft.world.level.block.state.BlockState blockState = info.state();

                if (blockState.isAir()) {
                    continue;
                }

                VoxelShape collisionShape = blockState.getCollisionShape(cachedWorld, localPos, CollisionContext.empty());
                if (collisionShape != EMPTY_SHAPE && !collisionShape.isEmpty()) {
                    int x = localPos.getX();
                    int y = localPos.getY();
                    int z = localPos.getZ();

                    VoxelShape movedShape = collisionShape.move(x, y, z);
                    List<AABB> shapeAABBs = movedShape.toAabbs();
                    if (!shapeAABBs.isEmpty()) {
                        aabbs.addAll(shapeAABBs);
                    }
                }
            }
            if (aabbs.size() < aabbs.size() * 0.75) {
                aabbs = new ArrayList<>(aabbs);
            }

            return weather$aabbs = aabbs;
        }
    }
}