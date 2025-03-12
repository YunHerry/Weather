package indi.yunherry.weather.util;

import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ShaderUtils {
    private static final Logger log = LoggerFactory.getLogger(ShaderUtils.class);

    public static boolean isOptifineLoaded() {
        try {
            Class.forName("net.optifine.Config");
        } catch (ClassNotFoundException var2) {
            return false;
        }
            return true;
    }
    public static boolean areShadersRunning() {
        try {
            Method method;
            Class clazz;
            if (isOculusLoaded()) {
                clazz = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                method = clazz.getMethod("getInstance");
                Object irisApi = method.invoke((Object) null);
                return (Boolean) irisApi.getClass().getMethod("isShaderPackInUse").invoke(irisApi);
            } else if (isOptifineLoaded()) {
                clazz = Class.forName("net.optifine.Config");
                method = clazz.getMethod("isShaders");
                return (Boolean) method.invoke((Object) null);
            } else {
                return false;
            }
        } catch (IllegalArgumentException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException |
                 SecurityException | IllegalAccessException var3) {
            Exception e = var3;
log.error("Failed to check if shaders are enabled:");
            return false;
        }
    }
    public static boolean isOculusLoaded() {
        return ModList.get().isLoaded("oculus");
    }
    public static boolean isSodiumLoaded() {
        return ModList.get().isLoaded("rubidium");
    }

}