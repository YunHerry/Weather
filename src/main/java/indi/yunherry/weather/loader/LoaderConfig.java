package indi.yunherry.weather.loader;

import net.minecraft.world.phys.Vec3;

public record LoaderConfig(
        double rain,
        double renderDistance,
        int skyLight,
        Vec3 camPos
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private double rain = 0.0;
        private double renderDistance = 16.0;
        private int skyLight = 15;
        private Vec3 camPos = new Vec3(0, 0, 0);

        public Builder rain(double rain) { this.rain = rain; return this; }
        public Builder renderDistance(double renderDistance) { this.renderDistance = renderDistance; return this; }
        public Builder skyLight(int skyLight) { this.skyLight = skyLight; return this; }
        public Builder camPos(Vec3 camPos) { this.camPos = camPos; return this; }

        public LoaderConfig build() {
            return new LoaderConfig(rain, renderDistance, skyLight, camPos);
        }
    }
}
