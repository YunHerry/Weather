package indi.yunherry.weather.loader;

import com.google.gson.JsonObject;
import indi.yunherry.weather.utils.ColorMapUtils;
import indi.yunherry.weather.utils.ColorUtils;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

public class BiomeWaterFogColorLoader extends  AbstractLoader<SimpleBiomeColorConfigData.BiomeColorData> {
    public static final String BIOME_WATER_FOG_COLOR_LOADER = "BiomeWaterFogColorLoader";
    private SimpleBiomeColorConfigData config;

    public Integer getColorMapByString(String biomeId) {
        return colorMaps.getOrDefault(biomeId, ColorUtils.parseColor(config.defaultColor()));
    }

    private final Map<String, Integer> colorMaps = new HashMap<>();
    private BiomeWaterFogColorLoader() {
    }

    @Override
    public String getLoaderName() {
        return BIOME_WATER_FOG_COLOR_LOADER;
    }

    @Override
    public String getNamespace() {
        return "biome_water_color";
    }

    protected static AbstractLoader<?> register() {
        return new BiomeWaterFogColorLoader();
    }

    @Override
    public void process(JsonObject jsonObject) {
        try {
            config = gson.fromJson(jsonObject, SimpleBiomeColorConfigData.class);
            if (config != null) {
                // 为每个生物群系生成颜色映射
                config.data().forEach((biomeId, biomeColorData) -> {
                    int color = ColorUtils.parseColor(biomeColorData.color());
                    colorMaps.put(biomeId, color);
                    System.out.println("Generated color map for biome: " + biomeId);
                });
                System.out.println("命名空间: " + getNamespace());
            }

        } catch (Exception e) {
            System.err.println("Failed to process JSON: " + e.getMessage());
        }

    }

    @Override
    public float getYAxis(LoaderConfig loaderConfig) {
        return 0f;
    }

    @Override
    public Vector4f findColorByKey(String key, LoaderConfig loaderConfig) {
        return ColorMapUtils.int2Vector4fColor(colorMaps.getOrDefault(key, ColorUtils.parseColor(config.defaultColor())));
    }
}
