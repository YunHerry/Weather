package indi.yunherry.weather.renderer;


import net.minecraft.client.renderer.LightTexture;

public abstract class WeatherRenderer extends ParticleRenderer {
    public abstract void renderWeather(LightTexture texture, float partialTick,int ticks);

}
