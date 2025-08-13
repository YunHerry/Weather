package indi.yunherry.weather;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Sounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Weather.MOD_ID);

    // 注册wind声音
    public static final RegistryObject<SoundEvent> WIND = SOUND_EVENTS.register("wind",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Weather.MOD_ID, "wind")));
}