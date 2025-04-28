package indi.yunherry.weather.hook;

import indi.yunherry.weather.renderer.ParticleRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TickHandler {

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        ParticleRenderer.update();
    }
}
