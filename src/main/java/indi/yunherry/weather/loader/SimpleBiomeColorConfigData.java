package indi.yunherry.weather.loader;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public record SimpleBiomeColorConfigData(
        @SerializedName("default_color") String defaultColor,
        @SerializedName("loader") String loader,
        @SerializedName("data") Map<String, SimpleBiomeColorConfigData.BiomeColorData> data
) {
    public record BiomeColorData(
            @SerializedName("color") String color
    ) {

        // 便捷方法：将颜色字符串转换为整数
        public int getStartColorAsInt() {
            return parseColorString(color);
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
