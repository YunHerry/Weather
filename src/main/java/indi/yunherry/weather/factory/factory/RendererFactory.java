package indi.yunherry.weather.factory.factory;

import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.factory.bean.RendererEngine;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

//@TODO await refactor
@indi.yunherry.weather.annotation.Factory
public class RendererFactory extends Factory {
    private static final Logger log = LoggerFactory.getLogger(RendererFactory.class);

    @Override
    protected void create(Map<String, List<ModFileScanData.AnnotationData>> beans) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<RendererEngine> renderers = beans.get("indi.yunherry.weather.annotation.Renderer").stream().map(item -> {
            try {
                return new RendererEngine(item);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }).toList();
        WorldContext.renderers.addAll(renderers);
//        ScanTypeEnum scanTypeEnum = getScanRange(WorldContext.mainClass);
//        finds(beans, renderers, Renderer.class, indi.yunherry.weather.renderer.ParticleRenderer.class, scanTypeEnum);
    }
}
