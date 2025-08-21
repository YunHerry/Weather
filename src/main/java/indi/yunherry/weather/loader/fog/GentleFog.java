package indi.yunherry.weather.loader.fog;

public class GentleFog extends AbstractFogStrategy{
    @Override
    public float getFogRadius(float rain, double playerY, int skyLight) {
        return (float) (1.0 - rain * 0.1
                + ((rain > 0 && playerY < 47) ? rain * 0.1 : 0)
                + (playerY < 47 ? 0.15 : 0)
                - ((playerY <= -54 && skyLight == 0) ? 0.75 : 0));
    }

    @Override
    public float getFogFade(float rain, double playerY, int renderDistance) {
        return (float) ((1.0 + (renderDistance / 64.0) * 0.5 + rain * 0.4
                - ((rain > 0 && playerY < 47) ? rain * 0.4 : 0)
                - (playerY < 47 ? 0.1 : 0))
                * (7.5 + (renderDistance / 64.0 * 2.5)));
    }
}
