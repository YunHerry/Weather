package indi.yunherry.weather.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import indi.yunherry.weather.impl.EntityShipCollisionUtilsInvoker;
import kotlin.Pair;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.apigame.collision.ConvexPolygonc;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.util.EntityShipCollisionUtils;
import org.valkyrienskies.mod.common.util.IEntityDraggingInformationProvider;

import java.util.List;

import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toJOML;
import static org.valkyrienskies.mod.common.util.VectorConversionsMCKt.toMinecraft;

@Mixin(Particle.class)
public abstract class MixinParticleByVS {
    @Shadow
    protected boolean removed;

    @Shadow
    @Final
    protected ClientLevel level;

    @Shadow
    public abstract Vec3 getPos();

    @Shadow
    protected double x;

    @Shadow
    protected double y;

    @Shadow
    protected double z;

    @WrapOperation(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collideBoundingBox(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Lnet/minecraft/world/level/Level;Ljava/util/List;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 wrapCollideBoundingBox(Entity entity, Vec3 movement, AABB box, Level level, List<VoxelShape> context, Operation<Vec3> original) {
        double baseInflation = (entity instanceof Player) ? 0.5 : 0.1;
        double minSize = Math.min(Math.min(box.getXsize(), box.getYsize()), box.getZsize());
        double miss = Math.max(0.1 - minSize, 0.0);
        double inflation = baseInflation + miss;

        double step = (entity != null) ? entity.maxUpStep() : 0.0;
        Vec3 inflatedMove = movement.add(0.0, Math.max(step - inflation, 0.0), 0.0);
        AABB inflatedBox = box.inflate(inflation);

        List<ConvexPolygonc> polys = ((EntityShipCollisionUtilsInvoker) (Object) EntityShipCollisionUtils.INSTANCE).getShipPolygonsCollidingWithEntity$weather(entity, inflatedMove, inflatedBox, (ClientLevel) level);
        Vec3 finalMove = movement;
        if (!polys.isEmpty()) {
            Pair<Vector3dc, Long> result = ValkyrienSkiesMod.vsCore.getEntityPolygonCollider().adjustEntityMovementForPolygonCollisions(toJOML(inflatedMove), toJOML(inflatedBox), step, polys);
            Vector3dc jomlMove = result.getFirst();
            Long shipId = result.getSecond();

            if (shipId != null && entity instanceof IEntityDraggingInformationProvider prov) {
                prov.getDraggingInformation().setLastShipStoodOn(shipId);
            }

            finalMove = toMinecraft(jomlMove);
        }

        return original.call(entity, finalMove, box, level, context);
    }

}
