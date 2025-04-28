package indi.yunherry.weather.factory.bean;

import indi.yunherry.weather.annotation.Renderer;
import indi.yunherry.weather.renderer.ParticleRenderer;
import net.minecraftforge.forgespi.language.ModFileScanData;

import java.lang.reflect.InvocationTargetException;

public class RendererEngine extends Engine {
    private boolean isEnableRandomTick;
    private boolean isConditionalRendering;
    private final ParticleRenderer renderer;
    public RendererEngine(ParticleRenderer renderer,boolean isConditionalRendering, boolean isEnableRandomTick) {
        this.isConditionalRendering = isConditionalRendering;
        this.renderer = renderer;
        this.isEnableRandomTick = isEnableRandomTick;
    }

    public RendererEngine(ModFileScanData.AnnotationData annotationData) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super();
        Class<ParticleRenderer> clazz = (Class<ParticleRenderer>) Class.forName(annotationData.clazz().getClassName());
        Renderer anno = clazz.getAnnotation(Renderer.class);
        this.isConditionalRendering = anno.isConditionalRendering();
        this.isEnableRandomTick = anno.isEnableRandomTick();
        renderer = clazz.getDeclaredConstructor().newInstance();
    }

    public boolean isEnableRandomTick() {
        return isEnableRandomTick;
    }

    public boolean isConditionalRendering() {
        return isConditionalRendering;
    }

    public ParticleRenderer getRenderer() {
        return renderer;
    }
}
