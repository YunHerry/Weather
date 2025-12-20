package indi.yunherry.weather.mixin;

import indi.yunherry.weather.CustomBlockEntityThreadPool;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public class MixinLevelByTicker {
    @Inject(
            method = "tickBlockEntities",
            at = @At("RETURN")
    )
    private void beforeTickBlockEntities(CallbackInfo ci) {
        CustomBlockEntityThreadPool.customTicks();
    }
}
