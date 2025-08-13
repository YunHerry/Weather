package indi.yunherry.weather.utils;

import com.mojang.blaze3d.platform.NativeImage;
import indi.yunherry.weather.loader.BiomeColorConfigData;
import net.minecraft.client.Minecraft;
import org.joml.Vector4f;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;

public class ColorMapUtils {
    public static Vector4f getColorFromMap(int[] colorMap, float y) {
        if (colorMap == null || colorMap.length == 0) {
            return new Vector4f(0.141f, 0.141f, 0.141f, 1.0f); // 默认颜色
        }

        // y 是颜色坐标，需要映射到 colorMap 数组索引
        // 如果 y 超出 colorMap 长度，需要适当处理

        int index;
        if (y >= colorMap.length) {
            // 如果 y 坐标超出数组长度，映射到最后一个索引
            index = colorMap.length - 1;
        } else if (y < 0) {
            // 如果 y 坐标为负数，映射到第一个索引
            index = 0;
        } else {
            // 正常情况，直接使用 y 作为索引
            index = Math.round(y);
        }

        int color = colorMap[index];

        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        return new Vector4f(r, g, b, a);
    }

    /**
     * 生成调试图片 - 将每个生物群系的颜色映射输出为图片文件
     * @param colorMaps 颜色映射数据
     * @param namespace 命名空间，用于组织目录结构
     */
    public static void generateDebugImages(Map<String, int[]> colorMaps, String namespace) {
        if (colorMaps.isEmpty()) {
            System.out.println("No color maps to generate debug images for.");
            return;
        }

        try {
            // 创建调试目录，包含namespace子目录
            Path debugDir = createDebugDirectory(namespace);

            System.out.println("Generating debug images for " + colorMaps.size() + " biomes in namespace: " + namespace);

            colorMaps.forEach((biomeId, colorMap) -> {
                try {
                    generateBiomeColorImage(debugDir, biomeId, colorMap);
                } catch (Exception e) {
                    System.err.println("Failed to generate debug image for biome: " + biomeId + " - " + e.getMessage());
                }
            });

            System.out.println("Debug images generated successfully in: " + debugDir.toString());

        } catch (Exception e) {
            System.err.println("Failed to generate debug images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成调试图片 - 重载方法，使用默认namespace
     */
    public void generateDebugImages(Map<String, int[]> colorMaps) {
        generateDebugImages(colorMaps, "default");
    }

    /**
     * 创建调试目录，支持namespace子目录
     * @param namespace 命名空间
     * @return 创建的目录路径
     */
    private static Path createDebugDirectory(String namespace) throws IOException {
        // 获取minecraft游戏目录
        File gameDir = Minecraft.getInstance().gameDirectory;

        // 清理namespace名称，避免文件系统不支持的字符
        String cleanNamespace = sanitizeFileName(namespace);

        // 构建路径：game_dir/weather/debug/namespace
        Path debugPath = Paths.get(gameDir.getAbsolutePath(), "weather", "debug", cleanNamespace);

        // 创建目录（如果不存在）
        Files.createDirectories(debugPath);

        return debugPath;
    }

    /**
     * 创建调试目录 - 重载方法，使用默认namespace
     */
    private Path createDebugDirectory() throws IOException {
        return createDebugDirectory("default");
    }

    /**
     * 为单个生物群系生成颜色图片
     */
    private static void generateBiomeColorImage(Path debugDir, String biomeId, int[] colorMap) throws IOException {
        if (colorMap == null || colorMap.length == 0) {
            System.out.println("Skipping empty color map for biome: " + biomeId);
            return;
        }

        // 图片尺寸设置
        int width = 64;  // 图片宽度
        int height = colorMap.length; // 高度等于颜色映射长度

        // 创建图片
        NativeImage image = new NativeImage(width, height, false);

        try {
            // 填充颜色数据
            for (int y = 0; y < height; y++) {
                int color = colorMap[y];

                // 转换ARGB到ABGR (NativeImage使用ABGR格式)
                int argb = color;
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r;

                // 水平方向填充相同颜色
                for (int x = 0; x < width; x++) {
                    image.setPixelRGBA(x, y, abgr);
                }
            }

            // 生成文件名（替换特殊字符）
            String fileName = sanitizeFileName(biomeId) + ".png";
            Path filePath = debugDir.resolve(fileName);

            // 保存图片
            image.writeToFile(filePath.toFile());

            System.out.println("Generated debug image: " + fileName + " (" + width + "x" + height + ")");

        } finally {
            // 释放图片资源
            image.close();
        }
    }

    /**
     * 清理文件名中的特殊字符
     */
    private static String sanitizeFileName(String fileName) {
        // 替换命名空间分隔符和其他特殊字符
        return fileName.replace(":", "_")
                .replace("/", "_")
                .replace("\\", "_")
                .replace("<", "_")
                .replace(">", "_")
                .replace("|", "_")
                .replace("?", "_")
                .replace("*", "_")
                .replace("\"", "_")
                .replace(" ", "_");
    }

    /**
     * 生成水平颜色条图片（可选的替代格式）
     * @param colorMaps 颜色映射数据
     * @param namespace 命名空间
     */
    public void generateHorizontalColorBars(Map<String, int[]> colorMaps, String namespace) {
        if (colorMaps.isEmpty()) {
            return;
        }

        try {
            Path debugDir = createDebugDirectory(namespace);
            Path horizontalDir = debugDir.resolve("horizontal");
            Files.createDirectories(horizontalDir);

            System.out.println("Generating horizontal color bars for namespace: " + namespace);

            colorMaps.forEach((biomeId, colorMap) -> {
                try {
                    generateHorizontalColorBar(horizontalDir, biomeId, colorMap);
                } catch (Exception e) {
                    System.err.println("Failed to generate horizontal color bar for: " + biomeId);
                }
            });

            System.out.println("Horizontal color bars generated in: " + horizontalDir.toString());

        } catch (Exception e) {
            System.err.println("Failed to generate horizontal color bars: " + e.getMessage());
        }
    }

    /**
     * 生成水平颜色条图片 - 重载方法，使用默认namespace
     */
    public void generateHorizontalColorBars(Map<String, int[]> colorMaps) {
        generateHorizontalColorBars(colorMaps, "default");
    }

    /**
     * 生成水平颜色条
     */
    private void generateHorizontalColorBar(Path dir, String biomeId, int[] colorMap) throws IOException {
        int width = colorMap.length;
        int height = 32; // 固定高度

        NativeImage image = new NativeImage(width, height, false);

        try {
            for (int x = 0; x < width; x++) {
                int color = colorMap[x];

                // 转换颜色格式
                int argb = color;
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                int abgr = (a << 24) | (b << 16) | (g << 8) | r;

                // 垂直方向填充相同颜色
                for (int y = 0; y < height; y++) {
                    image.setPixelRGBA(x, y, abgr);
                }
            }

            String fileName = sanitizeFileName(biomeId) + "_horizontal.png";
            Path filePath = dir.resolve(fileName);
            image.writeToFile(filePath.toFile());

        } finally {
            image.close();
        }
    }

    /**
     * 手动触发调试图片生成（用于测试）
     * @param config 配置数据
     * @param colorMaps 颜色映射
     * @param namespace 命名空间
     */
    public void debugGenerateImages(BiomeColorConfigData config, Map<String, int[]> colorMaps, String namespace) {
        if (config != null && !colorMaps.isEmpty()) {
            System.out.println("Manually generating debug images for namespace: " + namespace);
            generateDebugImages(colorMaps, namespace);
            generateHorizontalColorBars(colorMaps, namespace);
        } else {
            System.out.println("No color maps available for debug image generation.");
        }
    }

    /**
     * 手动触发调试图片生成 - 重载方法，使用默认namespace
     */
    public void debugGenerateImages(BiomeColorConfigData config, Map<String, int[]> colorMaps) {
        debugGenerateImages(config, colorMaps, "default");
    }

    /**
     * 批量生成多个namespace的调试图片
     * @param namespacedColorMaps 按namespace组织的颜色映射数据
     */
    public void generateDebugImagesForNamespaces(Map<String, Map<String, int[]>> namespacedColorMaps) {
        if (namespacedColorMaps.isEmpty()) {
            System.out.println("No namespaced color maps to generate debug images for.");
            return;
        }

        System.out.println("Generating debug images for " + namespacedColorMaps.size() + " namespaces...");

        namespacedColorMaps.forEach((namespace, colorMaps) -> {
            try {
                System.out.println("Processing namespace: " + namespace + " (" + colorMaps.size() + " color maps)");
                generateDebugImages(colorMaps, namespace);
                generateHorizontalColorBars(colorMaps, namespace);
            } catch (Exception e) {
                System.err.println("Failed to generate debug images for namespace: " + namespace + " - " + e.getMessage());
            }
        });

        System.out.println("All namespace debug images generated successfully.");
    }

    /**
     * 清理指定namespace的调试目录
     * @param namespace 要清理的命名空间
     */
    public void cleanDebugDirectory(String namespace) {
        try {
            File gameDir = Minecraft.getInstance().gameDirectory;
            String cleanNamespace = sanitizeFileName(namespace);
            Path debugPath = Paths.get(gameDir.getAbsolutePath(), "weather", "debug", cleanNamespace);

            if (Files.exists(debugPath)) {
                // 递归删除目录及其内容
                Files.walk(debugPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);

                System.out.println("Cleaned debug directory for namespace: " + namespace);
            }
        } catch (Exception e) {
            System.err.println("Failed to clean debug directory for namespace: " + namespace + " - " + e.getMessage());
        }
    }

    /**
     * 获取指定namespace的调试目录路径（不创建目录）
     * @param namespace 命名空间
     * @return 调试目录路径
     */
    public Path getDebugDirectoryPath(String namespace) {
        File gameDir = Minecraft.getInstance().gameDirectory;
        String cleanNamespace = sanitizeFileName(namespace);
        return Paths.get(gameDir.getAbsolutePath(), "weather", "debug", cleanNamespace);
    }
}