package indi.yunherry.weather.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import indi.yunherry.weather.RayThreadPool;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.renderer.ParticleRenderer;
import indi.yunherry.weather.renderer.WeatherRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = LevelRenderer.class,priority = 1001)
public abstract class MixinLevelRenderer {

    @Shadow public abstract int getTicks();

    @Shadow @Final private float[] rainSizeX;

    @Shadow @Final private float[] rainSizeZ;

    @Shadow public abstract void tick();

    @Shadow private int ticks;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    public void weather$injectCustomWeatherRendering_renderLevel(PoseStack stack, float partialTick, long l, boolean flag, Camera camera, GameRenderer renderer, LightTexture texture, Matrix4f projMat, CallbackInfo ci)
    {
//        WorldContext.renderer.renderWeather(texture, partialTick, (float) camera.getPosition().x, (float) camera.getPosition().y, (float) camera.getPosition().z,this.getTicks(),this.rainSizeX,this.rainSizeZ);
        WorldContext.renderers.forEach(item->{
            if (item.isConditionalRendering()) {
                if(item.getRenderer().isRender()) {
                    item.getRenderer().render();
                }
            } else {
                item.getRenderer().render();
            }
            if(item.getRenderer() instanceof WeatherRenderer) {
                ((WeatherRenderer) item.getRenderer()).renderWeather(texture,partialTick,ticks);
            }
        });
    }
    //渲染天气的render
    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true)
    public void weather$overrideRainRendering_renderSnowAndRain(LightTexture texture, float partialTick, double camX, double camY, double camZ, CallbackInfo ci)
    {
            ci.cancel();
    }
    @Inject(method = "allChanged", at = @At("HEAD"))
    public void weather$allChanged(CallbackInfo ci)
    {
        ParticleRenderer.update();
    }
    @Inject(method = "tick", at = @At("HEAD"))
    public void weather$tickCloudRenderer_tick(CallbackInfo ci)
    {
        ParticleRenderer.update();
        WorldContext.renderers.forEach(item->{
            item.getRenderer().tick();
            if(item.isEnableRandomTick() && item.getRenderer().isRandomTick()) {
                item.getRenderer().randomTick();
            }
        });
    }
//    @Inject(method = "tickRain", at = @At("HEAD"), cancellable = true)
//    public void weather$tickRain(Camera p_109694_, CallbackInfo ci) {
////        ci.cancel();
//    }
//    @Redirect(method = "tickRain",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;tickRain(Lnet/minecraft/client/multiplayer/ClientLevel;ILnet/minecraft/client/Camera;)Z"))
//    private boolean tickWeather(DimensionSpecialEffects instance, ClientLevel clientLevel, int i, Camera camera)
//    {
//        RandomSource randomsource = RandomSource.create((long)this.ticks * 312987231L);
//        if (!WorldContext.renderer.getPrecipitationQuads().isEmpty()  && randomsource.nextInt(3) < this.rainSoundTime++) {
//            this.rainSoundTime = 0;
//            AtomicReference<Float> nearDistance = new AtomicReference<>((float) 10000);
//            AtomicReference<BlockPos> blockPos = new AtomicReference<>();
//            BlockPos playerPos = this.minecraft.player.blockPosition();
//            List<Precipitation> rainQuads = WorldContext.renderer.getQuadsByPrecipitation().get(Biome.Precipitation.RAIN);
//            rainQuads.forEach(item->{
//                float val = Math.min((float) Math.sqrt(item.getBlockPos().distSqr(playerPos)), nearDistance.get());
//                if (val != nearDistance.get()) {
//                    nearDistance.set(val);
//                    blockPos.set(item.getDownBlockPos());
//                }
//            });
//            if(blockPos.get() == null) return false;
//            boolean isAbovePlayer = blockPos.get().getY() > playerPos.getY() + 1;
//            boolean hasCeiling = blockPos.get().getY() > playerPos.getY();
//
//            SoundEvent sound = isAbovePlayer && hasCeiling ?
//                    SoundEvents.WEATHER_RAIN_ABOVE :
//                    SoundEvents.WEATHER_RAIN;
//
//            //大雨0.4 中雨 0.2 小雨 0.01
//            float volume = Mth.clampedMap(nearDistance.get(), 4.0F, 18.0F, 0.0f, 0.4F);
//
//            this.minecraft.level.playLocalSound(
//                    blockPos.get(),
//                    sound,
//                    SoundSource.WEATHER,
//                    volume,
//                    isAbovePlayer ? 0.5F : 1.0F,
//                    false
//            );
//        }
//        return false;
//    }
}
