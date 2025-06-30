package indi.yunherry.weather;

import indi.yunherry.weather.renderer.ParticleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

public abstract class GlobalContext {
    public static BlockPos camPos;
    public static ClientLevel level;
    public static Minecraft mc;

    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        ParticleRenderer.mc = mc;
        camPos = mc.gameRenderer.getMainCamera().getBlockPosition();
        level = mc.level;
        AnimationController.update(level);
    }
}
