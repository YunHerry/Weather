package indi.yunherry.weather.event;

import indi.yunherry.weather.loader.FileLoaderUtils;
import indi.yunherry.weather.loader.JsonLoaderUtils;
import indi.yunherry.weather.loader.LoaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import indi.yunherry.weather.loader.FileLoaderUtils;
import indi.yunherry.weather.loader.JsonLoaderUtils;
import indi.yunherry.weather.loader.LoaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;
import java.util.Map;

import static indi.yunherry.weather.Weather.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LoaderInitEvent {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        Minecraft mc = Minecraft.getInstance();

        ResourceManager resourceManager = mc.getResourceManager();
        FileLoaderUtils.init(resourceManager);
        JsonLoaderUtils.init(resourceManager);
        LoaderManager.init();

    }
}
