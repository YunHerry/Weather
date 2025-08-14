package indi.yunherry.weather.loader;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Optional;

public class LoaderManager {
    private static ArrayList<AbstractLoader<?>> loaders = new ArrayList<>();

    public static void init() {
        //将所有的loader注册进去
        register();
        //根据json文件中的loader配置项识别是否是可用json文件,否则无视
        loaders.forEach(item -> {
            item.loadLoader();
            FileLoaderUtils.loadJsonFiles(item.getNamespace(), item::isValid, item::process);
        });
    }

    private static void register() {
        loaders.add(BiomeFogColorLoader.register());
        loaders.add(BiomeSkyColorLoader.register());
        loaders.add(BiomeWaterColorLoader.register());
//        loaders.add(BiomeLavaColorLoader.register());
        loaders.add(BiomeWaterFogColorLoader.register());
    }

    /**
     * 根据 loaderName 获取对应的 loader（Optional版本）
     *
     * @param loaderName loader的名称
     * @return Optional包装的loader
     */
    public static Optional<AbstractLoader<?>> getLoaderOptional(String loaderName) {
        return loaders.stream()
                .filter(loader -> loader.getLoaderName().equals(loaderName))
                .findFirst();
    }

    /**
     * 根据 loaderName 获取对应的 loader（泛型版本 - 修复类型转换警告）
     *
     * @param loaderName loader的名称
     * @param clazz      loader的类型
     * @return 指定类型的loader，如果没找到或类型不匹配返回null
     */
    public static <T extends AbstractLoader<?>> T getLoader(String loaderName, Class<T> clazz) {
        return loaders.stream()
                .filter(loader -> loader.getLoaderName().equals(loaderName))
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * 检查是否存在指定名称的loader
     *
     * @param loaderName loader的名称
     * @return 如果存在返回true，否则返回false
     */
    public static boolean hasLoader(String loaderName) {
        return loaders.stream()
                .anyMatch(loader -> loader.getLoaderName().equals(loaderName));
    }
}
