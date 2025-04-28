package indi.yunherry.weather;

import indi.yunherry.weather.client.particle.SnowParticle;
import indi.yunherry.weather.client.particle.WindParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static indi.yunherry.weather.ParticleRegistry.SNOW;
import static indi.yunherry.weather.ParticleRegistry.WIND;
import static indi.yunherry.weather.Weather.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class ParticleFactory {
    @SubscribeEvent
    public static void onClientSetup(RegisterParticleProvidersEvent event) {
        ParticleEngine engine = Minecraft.getInstance().particleEngine;
        engine.register(SNOW.get(), SnowParticle.Provider::new);
        engine.register(WIND.get(), WindParticle.Provider::new);
    }
}
