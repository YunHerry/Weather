package indi.yunherry.weather.loader;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * 生物群系颜色配置的记录类
 * 用于从 JSON 配置文件中反序列化颜色数据
 */
public record BiomeColorConfigData(
        @SerializedName("default_color") String defaultColor,
        @SerializedName("loader") String loader,
        @SerializedName("step") int step,
        @SerializedName("data") Map<String, BiomeColorData> data
) {

    /**
     * 内部记录类：生物群系颜色数据
     * 包含起始颜色、中间颜色和结束颜色
     */
    public record BiomeColorData(
            @SerializedName("startColor") String startColor,
            @SerializedName("midColor") String midColor
    ) {

        // 便捷方法：将颜色字符串转换为整数
        public int getStartColorAsInt() {
            return parseColorString(startColor);
        }

        public int getMidColorAsInt() {
            return parseColorString(midColor);
        }

        // 解析颜色字符串 (#RRGGBB) 为整数
        private int parseColorString(String colorStr) {
            if (colorStr == null || !colorStr.startsWith("#")) {
                return 0;
            }
            try {
                return Integer.parseInt(colorStr.substring(1), 16);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}