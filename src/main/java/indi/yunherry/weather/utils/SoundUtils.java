package indi.yunherry.weather.utils;

import indi.yunherry.weather.Sounds;
import indi.yunherry.weather.WindDirectionType;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class SoundUtils {
    /**
     * 在玩家指定方位播放风声(使用WindDirectionType)
     */
    public static void playWindInDirection(WindDirectionType windDirection, double distance, float volume, float pitch) {
        if (windDirection == WindDirectionType.NONE) {
            return; // 无风时不播放
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Vec3 playerPos = mc.player.position();
        Vec3 soundPos = calculateSoundPosition(playerPos, windDirection, distance);

        mc.level.playLocalSound(soundPos.x, soundPos.y, soundPos.z, Sounds.WIND.get(), SoundSource.AMBIENT, volume, pitch, false);
    }
    /**
     * 计算声音播放位置
     */
    /**
     * 计算声音播放位置(适配WindDirectionType)
     */
    private static Vec3 calculateSoundPosition(Vec3 playerPos, WindDirectionType windDirection, double distance) {
        double x = playerPos.x;
        double y = playerPos.y;
        double z = playerPos.z;

        switch (windDirection) {
            case NORTH -> z -= distance;
            case SOUTH -> z += distance;
            case EAST -> x += distance;
            case WEST -> x -= distance;
            case NONE -> {
                // 无风时返回玩家位置
                return playerPos;
            }
        }

        return new Vec3(x, y, z);
    }
}
