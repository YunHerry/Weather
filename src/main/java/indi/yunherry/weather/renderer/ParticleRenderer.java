package indi.yunherry.weather.renderer;

import indi.yunherry.weather.GlobalContext;

public abstract class ParticleRenderer extends GlobalContext {
    public static int particleCount = 0;
    public static int maxParticleCount = 1500;

    public abstract void tick();

    public abstract void render();

    public void randomTick() {

    }

    public boolean isRandomTick() {
        return false;
    }

    public boolean isRender() {
        return true;
    }
}
