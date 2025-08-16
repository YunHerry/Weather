package indi.yunherry.weather.mixin.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import indi.yunherry.weather.compact.create.ContraptionEntityAddon;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractContraptionEntity.class)
public class MixinAbstractContraptionEntity implements ContraptionEntityAddon {
	@Override
	public boolean weather$doParticleCollision() {
		return true;
	}
}
