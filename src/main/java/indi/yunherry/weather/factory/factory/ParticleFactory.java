package indi.yunherry.weather.factory.factory;

import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.factory.bean.ParticleRegisterEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static indi.yunherry.weather.Weather.MOD_ID;
@indi.yunherry.weather.annotation.Factory
@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleFactory extends Factory {
    public static final String AnnoName = "indi.yunherry.weather.annotation.ParticleProvider";
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MOD_ID);
    private static final Logger log = LoggerFactory.getLogger(ParticleFactory.class);
    public static List<ParticleRegisterEngine> providers;

    @SubscribeEvent
    public static void onClientSetup(RegisterParticleProvidersEvent event) {
        ParticleEngine engine = Minecraft.getInstance().particleEngine;
        providers.forEach(item->{
            item.register(engine);
            WorldContext.particleBeans.put(item.getParticleName(), item.getRegistryObject());
        });
    }


    @Override
    protected void create(Map<String, List<ModFileScanData.AnnotationData>> beans) {
        List<ModFileScanData.AnnotationData> providerBeans = beans.get(AnnoName);
        providers = new ArrayList<>(providerBeans.size());
        Stream<ModFileScanData.AnnotationData> stream = providerBeans.stream();
        stream.forEach(annotationData -> {
            try {
                providers.add(new ParticleRegisterEngine(annotationData));
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                if (WorldContext.isDebugMode) {
                    log.error(e.getMessage());
                }
            }
        });
    }
}
