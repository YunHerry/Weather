package indi.yunherry.weather.biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import org.jline.utils.Log;

public record CustomBiomeSoundsModifier(HolderSet<Biome> biomes,
                                       Holder<SoundEvent> windSound) implements BiomeModifier {
    public static final Codec<CustomBiomeSoundsModifier> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Biome.LIST_CODEC.fieldOf("biomes")
                                    .forGetter(CustomBiomeSoundsModifier::biomes),
                            SoundEvent.CODEC.fieldOf("wind_sound")
                                    .forGetter(CustomBiomeSoundsModifier::windSound)
                    ).apply(instance, CustomBiomeSoundsModifier::new)
            );

    @Override
    public void modify(Holder<Biome> biome, BiomeModifier.Phase phase,
                       ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        // 在最后阶段修改，避免被其他 mod 覆盖
        if (phase == BiomeModifier.Phase.AFTER_EVERYTHING) {
            if (biomes.contains(biome)) {
                String biomeName = biome.unwrapKey()
                        .map(key -> key.location().toString())
                        .orElse("unknown");

                Log.info("=== ADDING WIND SOUND TO BIOME: {} ===", biomeName);
                Log.info("Wind sound: {}", windSound.unwrapKey()
                        .map(key -> key.location().toString())
                        .orElse("unknown"));

                builder.getSpecialEffects().ambientLoopSound(windSound);
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
