package indi.yunherry.weather.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;

public abstract class ParticleRenderer {
    public static int particleCount = 0;
    public static int maxParticleCount = 1500;
    public static BlockPos camPos;
    public static ClientLevel level;
    public static Minecraft mc;
    public abstract void tick();
    public abstract void render();
    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        ParticleRenderer.mc = mc;
        camPos = mc.gameRenderer.getMainCamera().getBlockPosition();
        level = mc.level;
    }
    public void randomTick() {

    }
    public boolean isRandomTick() {
        return false;
    }
    public boolean isRender() {
        return true;
    }
}
