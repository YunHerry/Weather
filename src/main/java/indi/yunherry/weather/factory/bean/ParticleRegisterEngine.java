package indi.yunherry.weather.factory.bean;

import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.annotation.ParticleProvider;
import indi.yunherry.weather.renderer.ParticleRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

import static indi.yunherry.weather.factory.factory.ParticleFactory.PARTICLES;


public class ParticleRegisterEngine extends Engine {
    private static final Logger log = LoggerFactory.getLogger(ParticleRegisterEngine.class);
    private final String particleName;
    private final Class<? extends net.minecraft.client.particle.ParticleProvider<SimpleParticleType>> providerClazz;
    private final RegistryObject<SimpleParticleType> registryObject;
    @SuppressWarnings("unchecked")
    public ParticleRegisterEngine(ModFileScanData.AnnotationData annotationData) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException  {
        super();
        Class<? extends net.minecraft.client.particle.ParticleProvider<SimpleParticleType>> clazz = (Class<net.minecraft.client.particle.ParticleProvider<SimpleParticleType>>) Class.forName(annotationData.clazz().getClassName());
        ParticleProvider anno = clazz.getAnnotation(ParticleProvider.class);
        this.particleName = anno.particleName();
        this.providerClazz = clazz;
        this.registryObject = PARTICLES.register(this.particleName, ()->new SimpleParticleType(true));
    }
    public void register(ParticleEngine engine) {
        engine.register(this.registryObject.get(),this::createProvider);
    }
    private net.minecraft.client.particle.ParticleProvider<SimpleParticleType> createProvider(SpriteSet sprite) {
        try {
            return providerClazz.getDeclaredConstructor(SpriteSet.class).newInstance(sprite);
        } catch (Exception e) {
            if (WorldContext.isDebugMode) {
                log.error(e.getMessage());
            }
            return null;
        }
    }
    public RegistryObject<SimpleParticleType> getRegistryObject() {
        return registryObject;
    }

    public String getParticleName() {
        return particleName;
    }
}
