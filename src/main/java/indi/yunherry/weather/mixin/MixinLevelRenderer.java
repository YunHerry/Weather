package indi.yunherry.weather.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.client.Precipitation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(value = LevelRenderer.class,priority = 1001)
public class MixinLevelRenderer {
    private static final Logger log = LoggerFactory.getLogger(MixinLevelRenderer.class);
    @Shadow
    private @Nullable ClientLevel level;
    @Shadow private int ticks;
    @Shadow @Final
    private Minecraft minecraft;
    @Shadow private int rainSoundTime;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    public void weather$injectCustomWeatherRendering_renderLevel(PoseStack stack, float partialTick, long l, boolean flag, Camera camera, GameRenderer renderer, LightTexture texture, Matrix4f projMat, CallbackInfo ci)
    {
        WorldContext.renderer.renderWeather(texture, partialTick, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
    }
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    public void weather$overrideRainRendering_renderSnowAndRain(LightTexture texture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci)
    {
            ci.cancel();
    }
    @Inject(method = "tick", at = @At("HEAD"))
    public void weather$tickCloudRenderer_tick(CallbackInfo ci)
    {
        WorldContext.renderer.tick();
    }
//    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true)
    @Redirect(method = "tickRain",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;tickRain(Lnet/minecraft/client/multiplayer/ClientLevel;ILnet/minecraft/client/Camera;)Z"))
    private boolean tickWeather(DimensionSpecialEffects instance, ClientLevel clientLevel, int i, Camera camera)
    {
        RandomSource randomsource = RandomSource.create((long)this.ticks * 312987231L);
        if (!WorldContext.renderer.getPrecipitationQuads().isEmpty()  && randomsource.nextInt(3) < this.rainSoundTime++) {
            this.rainSoundTime = 0;
            AtomicReference<Float> nearDistance = new AtomicReference<>((float) 10000);
            AtomicReference<BlockPos> blockPos = new AtomicReference<>();
            BlockPos playerPos = this.minecraft.player.blockPosition();
            List<Precipitation> rainQuads = WorldContext.renderer.getQuadsByPrecipitation().get(Biome.Precipitation.RAIN);
            rainQuads.forEach(item->{
                float val = Math.min((float) Math.sqrt(item.getBlockPos().distSqr(playerPos)), nearDistance.get());
                if (val != nearDistance.get()) {
                    nearDistance.set(val);
                    blockPos.set(item.getDownBlockPos());
                }
            });
            if(blockPos.get() == null) return false;
            boolean isAbovePlayer = blockPos.get().getY() > playerPos.getY() + 1;
            boolean hasCeiling = blockPos.get().getY() > playerPos.getY();

            SoundEvent sound = isAbovePlayer && hasCeiling ?
                    SoundEvents.WEATHER_RAIN_ABOVE :
                    SoundEvents.WEATHER_RAIN;

            //大雨0.4 中雨 0.2 小雨 0.01
            float volume = Mth.clampedMap(nearDistance.get(), 4.0F, 18.0F, 0.0f, 0.4F);

            this.minecraft.level.playLocalSound(
                    blockPos.get(),
                    sound,
                    SoundSource.WEATHER,
                    volume,
                    isAbovePlayer ? 0.5F : 1.0F,
                    false
            );
        }
        return false;
    }
}
