package indi.yunherry.weather.utils;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

public class ColorUtils {
    /**
     * 解析十六进制颜色字符串为RGB整数
     */
    public static int parseColor(String hexColor) {
        if (hexColor == null || !hexColor.startsWith("#")) {
            throw new IllegalArgumentException("Invalid color format: " + hexColor);
        }

        try {
            return Integer.parseInt(hexColor.substring(1), 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid color format: " + hexColor, e);
        }
    }
    public static int vec3Color2Int(Vec3 color) {
        // 将0.0-1.0的浮点值转换为0-255的整数值
        int r = Math.round((float)color.x * 255.0f);
        int g = Math.round((float)color.y * 255.0f);
        int b = Math.round((float)color.z * 255.0f);

        // 确保值在有效范围内 (0-255)
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        // 组合成RGB格式（Alpha = 255，完全不透明）
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}