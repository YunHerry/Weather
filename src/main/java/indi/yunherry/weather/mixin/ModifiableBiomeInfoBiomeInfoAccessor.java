package indi.yunherry.weather.mixin;

import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModifiableBiomeInfo.BiomeInfo.class)
public interface ModifiableBiomeInfoBiomeInfoAccessor {
    // 标记为 @Mutable 以便我们可以写入 final 字段
    @Mutable
    @Accessor("effects")
    void setEffects(BiomeSpecialEffects effects);
}