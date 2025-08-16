package indi.yunherry.weather.loader;

import com.google.gson.JsonObject;
import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.utils.ColorMapUtils;
import indi.yunherry.weather.utils.ColorUtils;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.Map;


public class BiomeFogColorLoader extends AbstractLoader<BiomeColorConfigData.BiomeColorData> {
    public static final String BIOME_FOG_COLOR_LOADER = "BiomeFogColorLoader";
    private static final Logger log = LoggerFactory.getLogger(BiomeFogColorLoader.class);
    private BiomeColorConfigData config;

    public int[] getColorMapByString(String biomeId) {
        return colorMaps.get(biomeId);
    }

    private final Map<String, int[]> colorMaps = new HashMap<>();
    private BiomeFogColorLoader() {
    }

    @Override
    public String getLoaderName() {
        return BIOME_FOG_COLOR_LOADER;
    }

    @Override
    public String getNamespace() {
        return "biome_fog_color";
    }

    protected static AbstractLoader<?> register() {
        return new BiomeFogColorLoader();
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
                    log.debug("Generated color map for biome: {}",biomeId);
                });
                log.debug("namespace: {}",getNamespace());
                if (WorldContext.isDebugMode) {
                    ColorMapUtils.generateDebugImages(colorMaps,getNamespace());
                }
            }

        } catch (Exception e) {
            log.error("Failed to process JSON: " + e.getMessage());
        }

    }

    @Override
    public float getYAxis(LoaderConfig loaderConfig) {
        Vec3 camPos = loaderConfig.camPos();
        int skyLight = loaderConfig.skyLight();
        double yAxis = 0.5
                + loaderConfig.rain() * 0.5 * (camPos.y > 47 ? 1 : 0)
                - ((skyLight == 0 && camPos.y > 0 && camPos.y <= 47)
                ? ((camPos.y - 47) / -46.0) * 0.25 : 0)
                - ((skyLight == 0 && camPos.y <= 0) ? 0.25 : 0)
                - ((skyLight == 0 && camPos.y <= 0) ? (camPos.y / -256.0) : 0) + 40;
        return (float) yAxis;
    }

    @Override
    public Vector4f findColorByKey(String key, LoaderConfig loaderConfig) {
        return ColorMapUtils.getColorFromMap(getColorMapByString(key),getYAxis(loaderConfig), ColorUtils.parseColor(config.defaultColor()));
    }
}
