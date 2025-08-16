package indi.yunherry.weather.loader;

import com.google.gson.JsonObject;
import indi.yunherry.weather.utils.ColorMapUtils;
import indi.yunherry.weather.utils.ColorUtils;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BiomeWaterColorLoader extends  AbstractLoader<SimpleBiomeColorConfigData.BiomeColorData> {
    public static final String BIOME_WATER_COLOR_LOADER = "BiomeWaterColorLoader";
    private static final Logger log = LoggerFactory.getLogger(BiomeWaterColorLoader.class);
    private SimpleBiomeColorConfigData config;

    public Integer getColorMapByString(String biomeId) {
        return colorMaps.getOrDefault(biomeId,ColorUtils.parseColor(config.defaultColor()));
    }

    private final Map<String, Integer> colorMaps = new HashMap<>();
    private BiomeWaterColorLoader() {
    }

    @Override
    public String getLoaderName() {
        return BIOME_WATER_COLOR_LOADER;
    }

    @Override
    public String getNamespace() {
        return "biome_water_color";
    }

    protected static AbstractLoader<?> register() {
        return new BiomeWaterColorLoader();
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
                    log.info("Generated color map for biome: {}",biomeId);
                });
                log.debug("namespace: {}",getNamespace());
            }

        } catch (Exception e) {
            log.error("Failed to process JSON: " + e.getMessage());
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
