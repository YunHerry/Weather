package indi.yunherry.weather;

import com.mojang.logging.LogUtils;
import indi.yunherry.weather.annotation.FrameApplication;
import indi.yunherry.weather.annotation.ParentMark;
import indi.yunherry.weather.annotation.Renderer;
import indi.yunherry.weather.command.DebugCommand;
import indi.yunherry.weather.event.DebugEvent;
import indi.yunherry.weather.factory.factory.Factory;
import indi.yunherry.weather.renderer.WindRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@SuppressWarnings("deprecation")
@Mod(Weather.MOD_ID)
@FrameApplication
public class Weather {

    public static final String MOD_ID = "weather";
    private static ArtifactVersion version;
    private static final Logger LOGGER = LogUtils.getLogger();

    public Weather() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, ClassNotFoundException {
        version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ParticleRegistry.PARTICLES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(WorldContext.class);
        WorldContext.mainClass = Weather.class;
        Factory.initFactory();
        ClassLoader modClassLoader = Weather.class.getClassLoader();
//        System.out.println("modClassLoader: " + modClassLoader.getDefinedPackage());
//        System.out.println(Class.forName("indi.yunherry.weather.Weather").getName());
//        var result = Arrays.stream(modClassLoader.getDefinedPackages()).filter(item -> {
//            return item.getName().startsWith("indi.yunherry.weather");
//        });
        // 2. 获取基准类的 URL（例如主类）
//        result.forEach(item->{
//            System.out.println(loadClassesInPackage(item,modClassLoader).toString());;
//        });
//        ModFileScanData.AnnotationData[] items = ModList.get().getAllScanData().stream().map(ModFileScanData::getAnnotations) // 获取所有注解
//                .flatMap(Collection::stream) // 扁平化注解流
//                .filter(item -> item.annotationType().equals(Renderer.class)  // 比较注解类型
//                ).toArray(ModFileScanData.AnnotationData[]::new); // 返回 AnnotationData 数组
//        ;
//
    }

    private static Set<Class<?>> loadClassesInPackage(Package pkg, ClassLoader classLoader) {
        try {
            // 反射调用 LaunchClassLoader 的 getLoadedClasses 方法（Forge 内部 API）
            Method getLoadedClasses = classLoader.getClass().getDeclaredMethod("getLoadedClasses");
            getLoadedClasses.setAccessible(true);

            Class<?>[] loadedClasses = (Class<?>[]) getLoadedClasses.invoke(classLoader);
            return Arrays.stream(loadedClasses).filter(clazz -> clazz.getPackage().equals(pkg)).collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RuntimeException("Failed to access LaunchClassLoader", e);
        }
    }

    private void commonSetup(RegisterEvent event) {

    }

    // Add the example block item to the building blocks tab

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        DebugCommand.registerCommand(event);
        // Do something when the server starts
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

}