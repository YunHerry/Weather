package indi.yunherry.weather.loader;

import com.google.gson.JsonObject;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.utils.ColorMapUtils;
import indi.yunherry.weather.utils.ColorUtils;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BiomeSkyColorLoader extends AbstractLoader<BiomeColorConfigData.BiomeColorData> {
    public static final String BIOME_SKY_COLOR_LOADER = "BiomeSkyColorLoader";
    private static final Logger log = LoggerFactory.getLogger(BiomeSkyColorLoader.class);
    private BiomeColorConfigData config;

    public int[] getColorMapByString(String biomeId) {
        return colorMaps.get(biomeId);
    }

    private final Map<String, int[]> colorMaps = new HashMap<>();

    private BiomeSkyColorLoader() {
    }

    @Override
    public String getLoaderName() {
        return BIOME_SKY_COLOR_LOADER;
    }

    @Override
    public String getNamespace() {
        return "biome_sky_color";
    }

    protected static AbstractLoader<?> register() {
        return new BiomeSkyColorLoader();
    }

    @Override
    public void process(JsonObject jsonObject) {
        try {
            config = gson.fromJson(jsonObject, BiomeColorConfigData.class);
            if (config != null) {
                // 为每个生物群系生成颜色映射
                config.data().forEach((biomeId, biomeColorData) -> {
                    int[] colorMap = ColorMapUtils.generateColorMap(biomeColorData, config.step());
                    colorMaps.put(biomeId, colorMap);
                    log.info("Generated color map for biome: {}", biomeId);
                });
                log.debug("namespace: {}", getNamespace());
                if (WorldContext.isDebugMode) {
                    ColorMapUtils.generateDebugImages(colorMaps, getNamespace());
                }

            }

        } catch (Exception e) {
            log.error("Failed to process JSON: " + e.getMessage());
        }

    }

    @Override
    public float getYAxis(LoaderConfig loaderConfig) {
        return (float) loaderConfig.rain();
    }

    @Override
    public Vector4f findColorByKey(String key, LoaderConfig loaderConfig) {
        return ColorMapUtils.getColorFromMap(getColorMapByString(key), getYAxis(loaderConfig), ColorUtils.parseColor(config.defaultColor()));
    }
}
