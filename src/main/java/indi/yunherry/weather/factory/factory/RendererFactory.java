package indi.yunherry.weather.factory.factory;

import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.factory.bean.RendererEngine;
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
    public static final String AnnoName = "indi.yunherry.weather.annotation.Renderer";
    @Override
    protected void create(Map<String, List<ModFileScanData.AnnotationData>> beans) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<RendererEngine> renderers = beans.get(AnnoName).stream().map(item -> {
            try {
                return new RendererEngine(item);
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                if (WorldContext.isDebugMode) {
                    log.error(e.getMessage());
                }
                return null;
            }
        }).toList();
        WorldContext.renderers.addAll(renderers);
    }
}
