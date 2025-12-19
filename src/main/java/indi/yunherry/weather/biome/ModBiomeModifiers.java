package indi.yunherry.weather.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jline.utils.Log;

import static indi.yunherry.weather.Weather.MOD_ID;

public class ModBiomeModifiers {

    // 创建 DeferredRegister
    public static final DeferredRegister<Codec<? extends BiomeModifier>>
            BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(
            ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,
            MOD_ID
    );

    public static final RegistryObject<Codec<? extends BiomeModifier>>
            CUSTOM_BIOME_SOUNDS = BIOME_MODIFIER_SERIALIZERS.register(
            "custom_biome_sounds",
            () -> CustomBiomeSoundsModifier.CODEC
    );


    public static void register(IEventBus modBus) {
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
    }
}
