package indi.yunherry.weather.hook;

import indi.yunherry.weather.AnimationController;
import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.renderer.ParticleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TickHandler {

    @SubscribeEvent
    public static void onTick(TickEvent.LevelTickEvent event) {
//        if (event.phase == TickEvent.Phase.START) {
//            GlobalContext.update();
//        }
    }
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
//        if (event.phase == TickEvent.Phase.START) {
//            AnimationController.tick(Minecraft.getInstance().level);
//        }
    }
}
