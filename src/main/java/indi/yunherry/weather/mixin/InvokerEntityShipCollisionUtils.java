package indi.yunherry.weather.mixin;

import indi.yunherry.weather.impl.EntityShipCollisionUtilsInvoker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.valkyrienskies.core.apigame.collision.ConvexPolygonc;
import org.valkyrienskies.mod.common.util.EntityShipCollisionUtils;


import java.util.List;

@Mixin(EntityShipCollisionUtils.class)
public abstract class InvokerEntityShipCollisionUtils implements EntityShipCollisionUtilsInvoker {
    @Unique
    public List<ConvexPolygonc> getShipPolygonsCollidingWithEntity$weather(@Nullable Entity entity,
                                                                           Vec3 movement,
                                                                           AABB entityBoundingBox,
                                                                           ClientLevel world) {
        return getShipPolygonsCollidingWithEntity(entity,movement,entityBoundingBox,world);
    }

    @Shadow protected abstract List<ConvexPolygonc> getShipPolygonsCollidingWithEntity(Entity entity, Vec3 movement, AABB entityBoundingBox, Level world);
}
