package indi.yunherry.weather;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Weather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE,value = Dist.CLIENT)
public class GameHandler {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RayThreadPool.init();
    }
}
