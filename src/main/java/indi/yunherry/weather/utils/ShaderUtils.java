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
    public static final boolean OPTIFINE_LOADED;

    static {
        boolean b;
        try {
            Class.forName("net.optifine.Config");
            b = true;
        } catch (ClassNotFoundException var2) {
            b = false;
        }
        OPTIFINE_LOADED = b;
    }

    public static final boolean OCULUS_LOADED = ModList.get().isLoaded("oculus");
    public static final boolean SODIUM_LOADED = ModList.get().isLoaded("rubidium");
    private static final MethodHandle IRIS_Api_isShaderPackInUse;
    private static final MethodHandle OPTIFINE_isShaders;

    static {
        try {
            if (OCULUS_LOADED) {
                Class<?> clazz = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                Method getInstance = clazz.getMethod("getInstance");
                Object irisApi = getInstance.invoke(null);
                Method isShaderPackInUse = irisApi.getClass().getMethod("isShaderPackInUse");
                IRIS_Api_isShaderPackInUse = MethodHandles.lookup().unreflect(isShaderPackInUse).bindTo(irisApi);
            } else {
                IRIS_Api_isShaderPackInUse = null;
            }
            if (OPTIFINE_LOADED) {
                Class<?> clazz = Class.forName("net.optifine.Config");
                Method method = clazz.getMethod("isShaders");
                OPTIFINE_isShaders = MethodHandles.lookup().unreflect(method);
            } else {
                OPTIFINE_isShaders = null;
            }
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    private static final BooleanSupplier shaderRunningPredicate;

    static {
        if (OCULUS_LOADED) {
            shaderRunningPredicate = () -> {
                try {
                    // 静态常量的MethodHandle可以触发JIT优化
                    return (boolean) IRIS_Api_isShaderPackInUse.invokeExact();
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