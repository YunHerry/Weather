package indi.yunherry.weather.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ParentMark
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Renderer {
    public boolean isEnableRandomTick() default false;

    public boolean isConditionalRendering() default false;
}
