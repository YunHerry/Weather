package indi.yunherry.weather.utils;

import org.joml.Vector4f;

public class ColorUtils {

    /**
     * 在起始颜色和结束颜色之间做线性渐变。
     *
     * @param startColor 起始颜色（Vector4f，rgba）
     * @param endColor   结束颜色（Vector4f，rgba）
     * @param steps      总步数（>= 1）
     * @param index      当前步（0 <= index <= steps）
     * @return 当前插值颜色
     */
    public static Vector4f interpolateColor(Vector4f startColor, Vector4f endColor, int steps, int index) {
        if (steps <= 0) return new Vector4f(startColor);
        float t = Math.max(0.0f, Math.min(1.0f, index / (float) steps));

        float r = startColor.x + (endColor.x - startColor.x) * t;
        float g = startColor.y + (endColor.y - startColor.y) * t;
        float b = startColor.z + (endColor.z - startColor.z) * t;
        float a = startColor.w + (endColor.w - startColor.w) * t;

        return new Vector4f(r, g, b, a);
    }

}