package indi.yunherry.weather.hook;

import indi.yunherry.weather.AnimationController;
import indi.yunherry.weather.GlobalContext;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.renderer.ParticleRenderer;
import indi.yunherry.weather.renderer.WeatherRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.time.LocalTime;

@Mod.EventBusSubscriber
public class TickHandler {

    @SubscribeEvent
    public static void onTick(TickEvent.LevelTickEvent event) {
//        if (event.phase == TickEvent.Phase.START) {
//            GlobalContext.update();
//        }
    }

}
