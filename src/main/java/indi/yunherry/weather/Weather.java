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
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static indi.yunherry.weather.factory.factory.ParticleFactory.PARTICLES;

//TODO: 备忘,水草/荷叶靠近水面的时候会产生波纹
//TODO: 备忘,手上拿着火把会产生烟雾
@SuppressWarnings("deprecation")
@Mod(Weather.MOD_ID)
@FrameApplication
public class Weather {
    public static final String MOD_ID = "weather";
    private static ArtifactVersion version;
    public Weather() {
        if (isDebugLevel())WorldContext.isDebugMode = true;
        Factory.initFactory();
        version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        PARTICLES.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(WorldContext.class);
        WorldContext.mainClass = Weather.class;
//        ClassLoader modClassLoader = Weather.class.getClassLoader();

    }

    private void commonSetup(RegisterEvent event) {

    }
    public static boolean isDebugLevel() {
        return "debug".equals(System.getProperty("forge.logging.console.level"));
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            if (isDebugLevel()) {
                Configurator.setLevel("org.valkyrienskies.core.impl.shadow.Ej", org.apache.logging.log4j.Level.OFF);
            }
        }
    }

}