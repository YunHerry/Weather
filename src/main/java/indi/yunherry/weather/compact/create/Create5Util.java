package indi.yunherry.weather.compact.create;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionHandler;
import net.createmod.catnip.data.WorldAttached;
import net.minecraft.world.level.LevelAccessor;

import java.lang.ref.WeakReference;
import java.util.Map;

class Create5Util {
	static Map<Integer, WeakReference<AbstractContraptionEntity>> loadedContraptions(LevelAccessor level) {
		WorldAttached<Map<Integer, WeakReference<AbstractContraptionEntity>>>
			loadedContraptions = ContraptionHandler.loadedContraptions;
		return loadedContraptions.get(level);
	}
}
