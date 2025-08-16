package indi.yunherry.weather.compact.create;

import com.simibubi.create.content.contraptions.*;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.collision.Matrix3d;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.Math.*;

/**
 * See {@link ContraptionCollider}
 *
 * Based on CreateUtil from AsyncParticles by qu-an
 * <a href="https://github.com/Harveykang/AsyncParticles">github</a>
 *
 */
public class CreateUtils {
    private static final ThreadLocal<Vector3d> CACHED_VECTOR = ThreadLocal.withInitial(Vector3d::new);

    private static final double BOUNDS_INFLATE = 0.1;
    private static final double LOCAL_BB_INFLATE = 1.0E-7D;
    private static final double MOTION_THRESHOLD_SQR = 1e-12;

    public static Collection<WeakReference<AbstractContraptionEntity>> contraptions(LevelAccessor level) {
        return loadedContraptions(level).values();
    }

    @Nullable
    public static Vec3 collideMotionWithContraptions(ClientLevel level, Vec3 motion, AABB bounds) {
        if (motion.lengthSqr() < MOTION_THRESHOLD_SQR) {
            return null;
        }

        Vector3d result = CACHED_VECTOR.get();
        result.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);

        AABB finalBounds = bounds.inflate(BOUNDS_INFLATE);

        boolean hasCollision = false;

        for (Iterator<AbstractContraptionEntity> it = forEachContraption(level); it.hasNext(); ) {
            AbstractContraptionEntity entity = it.next();

            if (!(entity instanceof ContraptionEntityAddon)) {
                continue;
            }

            ContraptionEntityAddon addon = (ContraptionEntityAddon) entity;
            if (!addon.weather$doParticleCollision()) {
                continue;
            }

            Vec3 vec3 = collideMotionWithContraption(motion, finalBounds, entity, false);
            if (vec3 != null) {
                hasCollision = true;
                double newX = abs(result.x) < abs(vec3.x) ? result.x : vec3.x;
                double newY = abs(result.y) < abs(vec3.y) ? result.y : vec3.y;
                double newZ = abs(result.z) < abs(vec3.z) ? result.z : vec3.z;
                result.set(newX, newY, newZ);
            }
        }

        if (!hasCollision || result.x == Double.MAX_VALUE ||
                (motion.x == result.x && motion.y == result.y && motion.z == result.z)) {
            return null;
        }

        return new Vec3(result.x, result.y, result.z);
    }

    public static Iterator<AbstractContraptionEntity> forEachContraption(LevelAccessor level) {
        Iterator<WeakReference<AbstractContraptionEntity>> iterator = contraptions(level).iterator();
        return new Iterator<>() {
            private AbstractContraptionEntity next;

            @Override
            public boolean hasNext() {
                if (next != null) {
                    return true;
                }

                while (iterator.hasNext()) {
                    try {
                        AbstractContraptionEntity entity = iterator.next().get();
                        if (entity != null && entity.isAliveOrStale()) {
                            next = entity;
                            return true;
                        }
                    } catch (ConcurrentModificationException ignored) {
                        next = null;
                        return false;
                    }
                }
                next = null;
                return false;
            }

            @Override
            public AbstractContraptionEntity next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                AbstractContraptionEntity result = next;
                next = null;
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void forEachRemaining(Consumer<? super AbstractContraptionEntity> action) {
                while (hasNext()) {
                    action.accept(next);
                    next = null;
                }
            }
        };
    }

    public static Vec3 getWorldToLocalTranslation(Vec3 entityCenter,
                                                  Vec3 anchorVec,
                                                  Matrix3d rotationMatrix,
                                                  float yawOffset) {
        Vec3 position = ContraptionCollider.worldToLocalPos(entityCenter, anchorVec, rotationMatrix, yawOffset);
        return position.subtract(entityCenter);
    }

    @Nullable
    public static Vec3 collideMotionWithContraption(Vec3 originalMotion,
                                                    AABB entityBounds,
                                                    AbstractContraptionEntity contraptionEntity,
                                                    boolean estimate) {
        AABB entityBoundingBox;
        if (contraptionEntity instanceof CarriageContraptionEntity) {
            AABB bb0 = contraptionEntity.getBoundingBox();
            double xSize = bb0.getXsize();
            double zSize = bb0.getZsize();
            double ySize = bb0.getYsize();
            double inflateY = max(max(xSize, zSize) - ySize * 0.3, 0);
            entityBoundingBox = bb0.inflate(0, inflateY, 0);
        } else {
            entityBoundingBox = contraptionEntity.getBoundingBox();
        }

        if (!entityBounds.expandTowards(originalMotion).intersects(entityBoundingBox)) {
            return null;
        }

        AbstractContraptionEntity.ContraptionRotationState rotation = contraptionEntity.getRotationState();
        Matrix3d rotationMatrix = rotation.asMatrix();
        float yawOffset = rotation.getYawOffset();
        Vec3 anchorVec = contraptionEntity.getAnchorVec();

        Vec3 center = entityBounds.getCenter();
        Vec3 toLocalTranslation = getWorldToLocalTranslation(center, anchorVec, rotationMatrix, yawOffset);
        Vec3 contactPointMotion = contraptionEntity.getContactPointMotion(center);
        Vec3 localMotion = rotationMatrix.transform(originalMotion.subtract(contactPointMotion));
        AABB localBB = entityBounds.move(toLocalTranslation).inflate(LOCAL_BB_INFLATE);

        Contraption contraption = contraptionEntity.getContraption();

        List<AABB> collidableBBs;
        Optional<List<AABB>> collisionShapes = contraption.getSimplifiedEntityColliders();
        if (collisionShapes.isPresent()) {
            collidableBBs = collisionShapes.get();
        } else if (estimate) {
            return Vec3.ZERO;
        } else {
            collidableBBs = ((ContraptionAddon) contraption).weather$getAabbs();
        }

        Vec3 localCenter = localBB.getCenter();
        double localCenterX = localCenter.x;
        double localCenterY = localCenter.y;
        double localCenterZ = localCenter.z;

        double cx = localMotion.x;
        double cy = localMotion.y;
        double cz = localMotion.z;
        double sx = 0;
        double sy = 0;
        double sz = 0;
        boolean squeezed = false;

        double localXsize = localBB.getXsize();
        double localYsize = localBB.getYsize();
        double localZsize = localBB.getZsize();

        AABB localExpanded = localBB.expandTowards(localMotion);

        for (int i = 0, size = collidableBBs.size(); i < size; i++) {
            AABB bb = collidableBBs.get(i);

            if (!localExpanded.intersects(bb)) {
                continue;
            }

            if (localBB.intersects(bb)) {
                double bbCenterX = (bb.minX + bb.maxX) * 0.5;
                double bbCenterY = (bb.minY + bb.maxY) * 0.5;
                double bbCenterZ = (bb.minZ + bb.maxZ) * 0.5;

                squeezed = true;
                AABB intersect = localBB.intersect(bb);

                double intersectXsize = intersect.getXsize();
                double intersectYsize = intersect.getYsize();
                double intersectZsize = intersect.getZsize();

                Direction.Axis squeezedAxis;
                if (intersectXsize < intersectYsize) {
                    squeezedAxis = intersectXsize < intersectZsize ? Direction.Axis.X : Direction.Axis.Z;
                } else {
                    squeezedAxis = intersectYsize < intersectZsize ? Direction.Axis.Y : Direction.Axis.Z;
                }

                switch (squeezedAxis) {
                    case X -> {
                        double diff = localCenterX - bbCenterX;
                        double halfSize = intersectXsize * 0.5;
                        if (diff < -halfSize) {
                            sx = min(sx, -halfSize - diff);
                        } else if (diff > halfSize) {
                            sx = max(sx, halfSize - diff);
                        }
                    }
                    case Y -> {
                        double diff = localCenterY - bbCenterY;
                        double halfSize = intersectYsize * 0.5;
                        if (diff < -halfSize) {
                            sy = min(sy, -halfSize - diff);
                        } else if (diff > halfSize) {
                            sy = max(sy, halfSize - diff);
                        } else {
                            sy = cy > 0 ? cy : sy;
                        }
                    }
                    case Z -> {
                        double diff = localCenterZ - bbCenterZ;
                        double halfSize = intersectZsize * 0.5;
                        if (diff < -halfSize) {
                            sz = min(sz, -halfSize - diff);
                        } else if (diff > halfSize) {
                            sz = max(sz, halfSize - diff);
                        }
                    }
                }
            } else if (!squeezed) {
                double bbCenterX = (bb.minX + bb.maxX) * 0.5;
                double bbCenterY = (bb.minY + bb.maxY) * 0.5;
                double bbCenterZ = (bb.minZ + bb.maxZ) * 0.5;

                double relativeX = bbCenterX - localCenterX;
                double relativeY = bbCenterY - localCenterY;
                double relativeZ = bbCenterZ - localCenterZ;

                double halfXsum = (bb.getXsize() + localXsize) * 0.5;
                double halfYsum = (bb.getYsize() + localYsize) * 0.5;
                double halfZsum = (bb.getZsize() + localZsize) * 0.5;

                double sx_local = halfXsum - abs(relativeX);
                double sy_local = halfYsum - abs(relativeY);
                double sz_local = halfZsum - abs(relativeZ);

                Direction.Axis collidedAxis;
                if (sx_local < sy_local) {
                    collidedAxis = sx_local < sz_local ? Direction.Axis.X : Direction.Axis.Z;
                } else {
                    collidedAxis = sy_local < sz_local ? Direction.Axis.Y : Direction.Axis.Z;
                }

                switch (collidedAxis) {
                    case X -> {
                        double dx = relativeX > 0 ? relativeX - halfXsum : relativeX + halfXsum;
                        if (abs(cx) > abs(dx)) {
                            cx = dx;
                        }
                    }
                    case Y -> {
                        double dy = relativeY > 0 ? relativeY - halfYsum : relativeY + halfYsum;
                        if (abs(cy) > abs(dy)) {
                            cy = dy;
                        }
                    }
                    case Z -> {
                        double dz = relativeZ > 0 ? relativeZ - halfZsum : relativeZ + halfZsum;
                        if (abs(cz) > abs(dz)) {
                            cz = dz;
                        }
                    }
                }
            }
        }

        Vec3 clippedLocal;
        if (squeezed) {
            clippedLocal = new Vec3(sx, sy, sz);
        } else {
            clippedLocal = new Vec3(cx, cy, cz);
            if (localMotion.equals(clippedLocal)) {
                return null;
            }
        }

        Vec3 clipped = rotationMatrix.transpose().transform(clippedLocal);

        double contactX = contactPointMotion.x;
        double contactY = contactPointMotion.y;
        double contactZ = contactPointMotion.z;

        double x = (signum(contactX) != signum(originalMotion.x) || abs(clipped.x) < abs(contactX)) ?
                contactX * 3 : contactX;
        double y = (signum(contactY) != signum(originalMotion.y) || abs(clipped.y) < abs(contactY)) ?
                contactY * 3 : contactY;
        double z = (signum(contactZ) != signum(originalMotion.z) || abs(clipped.z) < abs(contactZ)) ?
                contactZ * 3 : contactZ;

        return clipped.add(x, y, z);
    }

    public static BlockPos localToWorldBlockPos(AbstractContraptionEntity contraption, BlockPos localPos) {
        Vec3 localVec = Vec3.atLowerCornerOf(localPos);
        Vec3 worldVec = contraption.toGlobalVector(localVec, 1.0F).add(0,1,0);
        return BlockPos.containing(worldVec);
    }

    public static Map<Integer, WeakReference<AbstractContraptionEntity>> loadedContraptions(LevelAccessor level) {
        return Create5Util.loadedContraptions(level);
    }
}