package indi.yunherry.weather.utils;

import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

public class ShaderUtils {
    private static final Logger log = LoggerFactory.getLogger(ShaderUtils.class);
    private static final MethodHandle IRIS_isShaderPackInUse;
    private static final MethodHandle OPTIFINE_isShaders;
    public static final boolean OPTIFINE_LOADED;
    public static final boolean OCULUS_LOADED = ModList.get().isLoaded("oculus");
    public static final boolean SODIUM_LOADED = ModList.get().isLoaded("rubidium");
    private static final BooleanSupplier shaderRunningPredicate;

    static {
        boolean optifineLoaded;
        try {
            Class.forName("net.optifine.Config");
            optifineLoaded = true;
        } catch (ClassNotFoundException var2) {
            optifineLoaded = false;
        }
        OPTIFINE_LOADED = optifineLoaded;
        try {
            if (OCULUS_LOADED) {
                Class<?> clazz = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                Method getInstance = clazz.getMethod("getInstance");
                Object irisApi = getInstance.invoke(null);
                Method isShaderPackInUse = irisApi.getClass().getMethod("isShaderPackInUse");
                IRIS_isShaderPackInUse = MethodHandles.lookup().unreflect(isShaderPackInUse).bindTo(irisApi);
                OPTIFINE_isShaders = null;
            } else if (OPTIFINE_LOADED) {
                IRIS_isShaderPackInUse = null;
                Class<?> clazz = Class.forName("net.optifine.Config");
                Method method = clazz.getMethod("isShaders");
                OPTIFINE_isShaders = MethodHandles.lookup().unreflect(method);
            } else {
                IRIS_isShaderPackInUse = null;
                OPTIFINE_isShaders = null;
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
        if (OCULUS_LOADED) {
            shaderRunningPredicate = () -> {
                try {
                    // 静态常量的MethodHandle可以触发JIT优化
                    return (boolean) IRIS_isShaderPackInUse.invokeExact();
                } catch (Throwable t) {
                    log.error("Failed to check if shaders are enabled:", t);
                    throw new RuntimeException(t);
                }
            };
        } else if (OPTIFINE_LOADED) {
            shaderRunningPredicate = () -> {
                try {
                    // 静态常量的MethodHandle可以触发JIT优化
                    return (boolean) OPTIFINE_isShaders.invokeExact();
                } catch (Throwable t) {
                    log.error("Failed to check if shaders are enabled:", t);
                    throw new RuntimeException(t);
                }
            };
        } else {
            shaderRunningPredicate = () -> false;
        }
    }

    public static boolean isOptifineLoaded() {
        return OPTIFINE_LOADED;
    }

    public static boolean areShadersRunning() {
        return shaderRunningPredicate.getAsBoolean();
    }

    public static boolean isOculusLoaded() {
        return OCULUS_LOADED;
    }

    public static boolean isSodiumLoaded() {
        return SODIUM_LOADED;
    }

}