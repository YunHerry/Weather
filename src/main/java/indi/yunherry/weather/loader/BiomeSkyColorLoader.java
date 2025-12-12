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

    /**
     * 替换为基于时间主轴和环境修正的逻辑。
     * 同时，将关键计算值写入 GlobalContext.DEBUG_VALUES。
     */
    @Override
    public float getYAxis(LoaderConfig loaderConfig) {
        // 0. 健壮性检查：确保配置和世界对象已加载
        if (config == null || GlobalContext.level == null) {
            GlobalContext.DEBUG_VALUES.put("Sky: Error", "Config or Level is NULL");
            return 0.0f;
        }

        // 1. **主要轴：基于时间比率 (修正后的时间)**
        float rawTime = GlobalContext.level.getDayTime();

        // 关键修正：将时间轴偏移 6000 刻，使 18000 (午夜) 变为 0，6000 (正午) 变为 12000。
        float correctedTime = (rawTime + 6000) % 24000;

        // 修正后的时间比率
        float timeRatio = correctedTime / 24000.0f;

        // 2. 将时间比率映射到 colorMap 的全尺寸索引
        int N = config.step();
        float timeBaseIndex = timeRatio * (N - 1); // 映射到 colorMap 的全尺寸浮点索引 [0, N-1]

        // 3. 环境修正值 (Conditional Offset)
        Vec3 camPos = loaderConfig.camPos();
        int skyLight = loaderConfig.skyLight();
        double offset = 0.0;

        // a. 雨水修正：下雨时颜色向下修正 (最大 -10 步)
        offset -= loaderConfig.rain() * 10.0;

        // b. 地下/黑暗修正：天光为 0 时，将颜色强制拉向夜间 (索引 0 附近)
        if (skyLight == 0) {
            if (camPos.y > 0 && camPos.y <= 47) {
                // 在地面附近，越深偏移越大 (0 到 -50)
                offset -= ((47.0 - camPos.y) / 47.0) * 50.0;
            } else if (camPos.y <= 0) {
                // 彻底在地下 (基础修正 -50)
                offset -= 50.0;
            }
        }

        // 4. 组合与限制
        double finalIndex = timeBaseIndex + offset;
        float result = (float) Math.max(0.0, Math.min(N - 1.0, finalIndex));

        // 5. 调试渲染：将计算结果添加到 DEBUG_VALUES
        GlobalContext.DEBUG_VALUES.put("Sky: Raw Time (24000 Cycle)", String.format("%.0f", rawTime % 24000));
        GlobalContext.DEBUG_VALUES.put("Sky: Corrected Time (24000 Cycle)", String.format("%.0f", correctedTime));
        GlobalContext.DEBUG_VALUES.put("Sky: Time Ratio (Corrected)", String.format("%.4f", timeRatio));
        GlobalContext.DEBUG_VALUES.put("Sky: Time Base Index", String.format("%.2f", timeBaseIndex));
        GlobalContext.DEBUG_VALUES.put("Sky: Env Offset", String.format("%.2f", offset));
        GlobalContext.DEBUG_VALUES.put("Sky: Final Y Index (Result)", String.format("%.2f / %d", result, N - 1));

        return result;
    }

    @Override
    public Vector4f findColorByKey(String key, LoaderConfig loaderConfig) {
        return ColorMapUtils.getColorFromMap(getColorMapByString(key), getYAxis(loaderConfig), ColorUtils.parseColor(config.defaultColor()));
    }
}
