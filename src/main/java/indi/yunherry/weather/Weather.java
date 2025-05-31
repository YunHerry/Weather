package indi.yunherry.weather;

import com.mojang.logging.LogUtils;
import indi.yunherry.weather.annotation.FrameApplication;
import indi.yunherry.weather.factory.factory.Factory;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.slf4j.Log4jLogger;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
//TODO: 备忘,水草/荷叶靠近水面的时候会产生波纹
//TODO: 备忘,手上拿着火把会产生烟雾
@SuppressWarnings("deprecation")
@Mod(Weather.MOD_ID)
@FrameApplication
public class Weather {
    public static final String MOD_ID = "weather";
    private static ArtifactVersion version;
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
    public static boolean isDebugLevel() {
        return "debug".equals(System.getProperty("forge.logging.console.level"));
    }
    // Add the example block item to the building blocks tab

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
//        DebugCommand.registerCommand(event);
        // Do something when the server starts
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if (isDebugLevel()) {
                WorldContext.isDebugMode = true;
                Configurator.setLevel("org.valkyrienskies.core.impl.shadow.Ej", org.apache.logging.log4j.Level.OFF);
            }
        }
    }

}