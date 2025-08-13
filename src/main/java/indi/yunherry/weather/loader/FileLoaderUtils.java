package indi.yunherry.weather.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FileLoaderUtils {
    private static final Logger log = LoggerFactory.getLogger(FileLoaderUtils.class);
    private static ResourceManager resourceManager;

    public static void init(ResourceManager resourceManager) {
        FileLoaderUtils.resourceManager = resourceManager;
    }


    /**
     * 加载并处理 JSON 文件
     *
     * @param namespace 命名空间路径
     * @param filterFn  过滤 JsonObject 的谓词函数
     * @param processor 处理 JsonObject 的函数
     * @return 处理后的结果映射
     */
    public static void loadJsonFiles(String namespace, Predicate<JsonObject> filterFn, Consumer<JsonObject> processor) {

        System.out.println("Loading JSON files from namespace: " + namespace);
        log.info("Loading JSON Files from Namespace: {}", namespace);

        // 首先获取所有资源文件
        Map<ResourceLocation, Resource> resources = resourceManager.listResources("biome", resourceLocation -> resourceLocation.getPath().endsWith(".json") // 只加载JSON文件
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try {
                // 读取并解析 JSON
                JsonObject jsonObject = parseJsonFromResource(entry.getValue());

                if (jsonObject != null && filterFn.test(jsonObject)) {
                    // 直接处理 JsonObject，无返回值
                    processor.accept(jsonObject);
                }
            } catch (Exception e) {
                log.warn("Failed to process JSON file: {}", entry.getKey(), e);
            }
        }
    }

    /**
     * 重载方法：允许自定义文件扩展名过滤
     */
    public static <T> Map<ResourceLocation, T> loadJsonFiles(String namespace, Predicate<ResourceLocation> resourceFilter, Predicate<JsonObject> jsonFilter, Function<Map.Entry<ResourceLocation, JsonObject>, T> processor) {

        Map<ResourceLocation, T> result = new HashMap<>();
        log.info("Loading JSON Files from Namespace: {}", namespace);

        // 使用自定义的资源过滤器
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(namespace, resourceFilter);

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try {
                JsonObject jsonObject = parseJsonFromResource(entry.getValue());

                if (jsonObject != null && jsonFilter.test(jsonObject)) {
                    Map.Entry<ResourceLocation, JsonObject> jsonEntry = new JsonEntry(entry.getKey(), jsonObject);

                    T processed = processor.apply(jsonEntry);
                    if (processed != null) {
                        result.put(entry.getKey(), processed);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to process JSON file: {}", entry.getKey(), e);
            }
        }

        return result;
    }

    /**
     * 从资源中解析 JsonObject
     */
    private static JsonObject parseJsonFromResource(Resource resource) {
        try (InputStreamReader reader = new InputStreamReader(resource.open())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | IllegalStateException e) {
            log.error("Failed to parse JSON from resource", e);
            return null;
        }
    }

    private static class JsonEntry implements Map.Entry<ResourceLocation, JsonObject> {
        private final ResourceLocation key;
        private JsonObject value;

        public JsonEntry(ResourceLocation key, JsonObject value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public ResourceLocation getKey() {
            return key;
        }

        @Override
        public JsonObject getValue() {
            return value;
        }

        @Override
        public JsonObject setValue(JsonObject value) {
            JsonObject oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
}
