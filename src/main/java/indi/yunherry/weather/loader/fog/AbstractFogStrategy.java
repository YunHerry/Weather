package indi.yunherry.weather.loader.fog;

public abstract class AbstractFogStrategy {
    public abstract float getFogRadius(float rain, double playerY, int skyLight);
    public abstract float getFogFade(float rain, double playerY, int renderDistance);
}
