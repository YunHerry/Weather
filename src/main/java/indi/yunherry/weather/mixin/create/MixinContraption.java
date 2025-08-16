package indi.yunherry.weather.mixin;

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

@Mixin(Contraption.class)
public class MixinContraption implements ContraptionAddon {
	@Shadow(remap = false)
	protected Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks;
	@Shadow(remap = false)
	protected ContraptionWorld world;
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Shadow(remap = false) public Optional<List<AABB>> simplifiedEntityColliders;
	@Unique
	private List<AABB> asyncparticles$aabbs;
	@Unique
	private final Object asyncparticles$lock = new Object();

	@Dynamic
	@WrapOperation(method = {
		"lambda$gatherBBsOffThread$17()Ljava/util/List;",
		"lambda$gatherBBsOffThread$25()Ljava/util/List;",
		"lambda$gatherBBsOffThread$26()Ljava/util/List;"
	}, at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/phys/shapes/VoxelShape;toAabbs()Ljava/util/List;"))
	private List<AABB> optimizeVoxelShape(VoxelShape instance, Operation<List<AABB>> original) {
		return original.call(instance);
	}

	@WrapOperation(method = "gatherBBsOffThread", remap = false, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenAccept(Ljava/util/function/Consumer;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<Void> gatherBBsOffThread(CompletableFuture<?> instance, Consumer<?> action, Operation<CompletableFuture<Void>> original) {
		return original.call(instance, action).thenRun(() -> {
			asyncparticles$aabbs = null;
		});
	}
	//给碰撞体混入了一个对象
	@Override
	public List<AABB> asyncparticles$getAabbs() {
		//如果存在简单碰撞体,就返回简单碰撞体
		if (simplifiedEntityColliders.isPresent()) {
			return simplifiedEntityColliders.get();
		}
		//不存在就返回自己计算的,前提是已经计算了
		if (asyncparticles$aabbs != null) {
			return asyncparticles$aabbs;
		}
		//加锁
		synchronized (asyncparticles$lock){
			//如果不为空就返回
			if (asyncparticles$aabbs != null) {
				return asyncparticles$aabbs;
			}
			//如果为空就进行计算
			List<AABB> aabbs = new ArrayList<>();
			// 获取精确碰撞形状
			for (Map.Entry<BlockPos, StructureTemplate.StructureBlockInfo> entry : blocks.entrySet()) {
				StructureTemplate.StructureBlockInfo info = entry.getValue();
				BlockPos localPos = entry.getKey();
				VoxelShape collisionShape = info.state().getCollisionShape(this.world, localPos, CollisionContext.empty());
				if (!collisionShape.isEmpty()) {
					aabbs.addAll(collisionShape.move(localPos.getX(), localPos.getY(), localPos.getZ()).toAabbs());
				}
			}
			return asyncparticles$aabbs = aabbs;
		}
	}
}
