package indi.yunherry.weather;

import com.mojang.blaze3d.platform.NativeImage;
import indi.yunherry.weather.client.particle.SnowParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.textures.ForgeTextureMetadata;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Math;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static indi.yunherry.weather.Weather.MOD_ID;
public class ParticleRegistry{
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MOD_ID);
    public static final RegistryObject<SimpleParticleType> SNOW = PARTICLES.register("snow", ()->new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> WIND = PARTICLES.register("wind", ()->new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> RIPPLE = PARTICLES.register("ripple", ()->new SimpleParticleType(true));
}
