package indi.yunherry.weather.impl;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.valkyrienskies.core.internal.collision.VsiConvexPolygonc;

import java.util.List;

public interface EntityShipCollisionUtilsInvoker {
    abstract List<VsiConvexPolygonc> getShipPolygonsCollidingWithEntity$weather(@Nullable Entity entity,
                                                                                Vec3 movement,
                                                                                AABB entityBoundingBox,
                                                                                ClientLevel world);
}
