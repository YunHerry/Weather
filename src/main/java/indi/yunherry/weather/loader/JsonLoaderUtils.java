package indi.yunherry.weather.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public class JsonLoaderUtils {
    private static ResourceManager resourceManager;

    public static void init(ResourceManager resourceManager) {
        JsonLoaderUtils.resourceManager = resourceManager;
    }
    /**
     * 从资源管理器中读取指定路径的 JSON 文件，解析为 JsonObject
     * @param resourceManager 资源管理器
     * @param jsonLocation JSON 文件的 ResourceLocation
     * @return 解析的 JsonObject，如果读取失败返回空 JsonObject
     */
    public static JsonObject loadJson(ResourceManager resourceManager, ResourceLocation jsonLocation) {
        Optional<Resource> optionalResource = resourceManager.getResource(jsonLocation);
        if (optionalResource.isEmpty()) {
            return new JsonObject();
        }
        Resource resource = optionalResource.get();
        try (InputStream inputStream = resource.open();
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }
}
