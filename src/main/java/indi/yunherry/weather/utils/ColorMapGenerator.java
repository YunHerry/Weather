package indi.yunherry.weather.utils;

import indi.yunherry.weather.loader.BiomeColorConfigData;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorMapGenerator {

    /**
     * 根据起始颜色、中间颜色、结束颜色和步数生成颜色映射
     * @param startColor 起始颜色 (hex格式，如 "#1e1f22")
     * @param midColor 中间颜色 (hex格式，如 "#e29649")
     * @param endColor 结束颜色 (hex格式，如 "#1e1f22")
     * @param step 颜色步数
     * @return 颜色映射数组，包含step个颜色值
     */
    public static int[] generateColorMap(String startColor, String midColor, String endColor, int step) {
        if (step <= 0) {
            throw new IllegalArgumentException("Step must be greater than 0");
        }

        if (step == 1) {
            return new int[]{parseColor(midColor)};
        }

        // 解析颜色
        int startRgb = parseColor(startColor);
        int midRgb = parseColor(midColor);
        int endRgb = parseColor(endColor);

        // 提取RGB分量
        Color startColorObj = new Color(startRgb);
        Color midColorObj = new Color(midRgb);
        Color endColorObj = new Color(endRgb);

        int[] colorMap = new int[step];

        // 如果只有2步，直接返回起始和结束颜色
        if (step == 2) {
            colorMap[0] = startRgb;
            colorMap[1] = endRgb;
            return colorMap;
        }

        // 计算中点位置
        int midPoint = step / 2;

        // 生成从起始到中间的颜色渐变
        for (int i = 0; i <= midPoint; i++) {
            float ratio = (float) i / midPoint;
            colorMap[i] = interpolateColor(startColorObj, midColorObj, ratio);
        }

        // 生成从中间到结束的颜色渐变
        for (int i = midPoint + 1; i < step; i++) {
            float ratio = (float) (i - midPoint) / (step - 1 - midPoint);
            colorMap[i] = interpolateColor(midColorObj, endColorObj, ratio);
        }

        return colorMap;
    }

    /**
     * 生成颜色映射（返回Color对象列表）
     */
    public static List<Color> generateColorMapAsColors(String startColor, String midColor, String endColor, int step) {
        int[] rgbArray = generateColorMap(startColor, midColor, endColor, step);
        List<Color> colors = new ArrayList<>();

        for (int rgb : rgbArray) {
            colors.add(new Color(rgb));
        }

        return colors;
    }

    /**
     * 生成颜色映射（返回十六进制字符串列表）
     */
    public static List<String> generateColorMapAsHex(String startColor, String midColor, String endColor, int step) {
        int[] rgbArray = generateColorMap(startColor, midColor, endColor, step);
        List<String> hexColors = new ArrayList<>();

        for (int rgb : rgbArray) {
            hexColors.add(String.format("#%06X", rgb & 0xFFFFFF));
        }

        return hexColors;
    }

    /**
     * 解析十六进制颜色字符串为RGB整数
     */
    private static int parseColor(String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#")) {
            throw new IllegalArgumentException("Invalid color format: " + hexColor);
        }

        try {
            return Integer.parseInt(hexColor.substring(1), 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid color format: " + hexColor, e);
        }
    }

    /**
     * 在两个颜色之间进行线性插值
     */
    private static int interpolateColor(Color color1, Color color2, float ratio) {
        // 确保比例在0-1之间
        ratio = Math.max(0, Math.min(1, ratio));

        int red = (int) (color1.getRed() + ratio * (color2.getRed() - color1.getRed()));
        int green = (int) (color1.getGreen() + ratio * (color2.getGreen() - color1.getGreen()));
        int blue = (int) (color1.getBlue() + ratio * (color2.getBlue() - color1.getBlue()));

        return new Color(red, green, blue).getRGB();
    }

    /**
     * 从 ColorConfigData.BiomeColorData 生成颜色映射
     */
    public static int[] generateColorMap(BiomeColorConfigData.BiomeColorData biomeColorData, int step) {
        return generateColorMap(
                biomeColorData.startColor(),
                biomeColorData.midColor(),
                biomeColorData.endColor(),
                step
        );
    }

    /**
     * 从 ColorConfigData 生成特定生物群系的颜色映射
     */
    public static int[] generateColorMapForBiome(BiomeColorConfigData config, String biomeId) {
        BiomeColorConfigData.BiomeColorData biomeData = config.data().get(biomeId);
        if (biomeData == null) {
            throw new IllegalArgumentException("Biome not found: " + biomeId);
        }

        return generateColorMap(biomeData, config.step());
    }
}
