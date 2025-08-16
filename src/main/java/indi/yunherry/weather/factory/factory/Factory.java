package indi.yunherry.weather.factory.factory;

import indi.yunherry.weather.Weather;
import indi.yunherry.weather.WorldContext;
import indi.yunherry.weather.annotation.ParentMark;
import indi.yunherry.weather.exception.InitFactoryException;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YunHerry
 */
public abstract class Factory {
    private static final Logger log = LoggerFactory.getLogger(Factory.class);
    public static final String PACKAGE_NAME = "indi.yunherry.weather";
    public static final String MIXIN_KEY_NAME = "mixin";
    protected static Map<String, List<ModFileScanData.AnnotationData>> classes;

    public static void initFactory() {
        classes = ModList.get().getAllScanData().stream().map(ModFileScanData::getAnnotations).flatMap(Collection::stream).filter(item -> {
            Type annotationType = item.annotationType();
            String className = annotationType.getClassName();
            if(!className.contains(PACKAGE_NAME) && className.contains(MIXIN_KEY_NAME)) return false;
            try {
                Class<?> annotationClass = Class.forName(className);
                return annotationClass.isAnnotationPresent(ParentMark.class);
            } catch (ClassNotFoundException e) {
                if (WorldContext.isDebugMode) {
                    log.error(e.getMessage());
                }
                return false;
            }
        }).collect(Collectors.groupingBy(item -> item.annotationType().getClassName()));
        getFactory();
        try {
            List<Factory> factoryArrayList = getFactory();
            for (Factory factory : factoryArrayList) {
                factory.create(classes);
            }
        } catch (Exception e) {
            throw new InitFactoryException(e);
        }
    }

    /**
     * 获取工厂具体实现类
     *
     * @return Factory 返回具体实现的工厂对象
     */
    private static List<Factory> getFactory() {
        return classes.get("indi.yunherry.weather.annotation.Factory").stream()
                .map(item -> {
                    try {
                        return (Factory) (Class.forName(item.clazz().getClassName()).getDeclaredConstructor().newInstance());
                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                             IllegalAccessException |
                             InvocationTargetException e) {
                        return null;
                    }
                }).filter(Objects::nonNull).toList();
    }

    /**
     * 该方法是用于工厂创建对应对象
     *
     * @param classes 传入Class对象,来判断是否是对应工厂的菜
     */
    protected abstract void create(Map<String, List<ModFileScanData.AnnotationData>> classes) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, NoSuchFieldException;

}
