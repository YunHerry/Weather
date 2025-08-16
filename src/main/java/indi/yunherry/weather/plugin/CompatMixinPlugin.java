package indi.yunherry.weather.plugin;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class CompatMixinPlugin implements IMixinConfigPlugin {
    public static final String VS = "valkyrienskies";
    public static final String VS_MIXIN_PARTICLE = "indi.yunherry.weather.mixin.create.MixinParticle";
    public static final String VS_MIXIN_ACCESS = "indi.yunherry.weather.mixin.InvokerEntityShipCollisionUtils";

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (VS_MIXIN_PARTICLE.equals(mixinClassName)) {

            return FMLLoader.getLoadingModList().getModFileById(VS) != null &&
                    FMLLoader.getLoadingModList().getModFileById("asyncparticles") == null;
        }
        if (VS_MIXIN_ACCESS.equals(mixinClassName)) {

            return FMLLoader.getLoadingModList().getModFileById(VS) != null;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
