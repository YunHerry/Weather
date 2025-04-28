package indi.yunherry.weather.factory.factory;

import indi.yunherry.weather.annotation.ParentMark;
import indi.yunherry.weather.constant.enums.ScanTypeEnum;
import indi.yunherry.weather.exception.InitFactoryException;
import indi.yunherry.weather.utils.ScanClassUntil;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author YunHerry
 */
public abstract class Factory {
    protected static Map<String, List<ModFileScanData.AnnotationData>> classes;

    public static void initFactory() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        classes = ModList.get().getAllScanData().stream().map(ModFileScanData::getAnnotations).flatMap(Collection::stream).filter(item -> {
            Type annotationType = item.annotationType();
            String className = annotationType.getClassName();
            try {
                Class<?> annotationClass = Class.forName(className);
                return annotationClass.isAnnotationPresent(ParentMark.class);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        }).collect(Collectors.groupingBy(item -> item.annotationType().getClassName()));
        getFactory();
        //            ModList.get().getAllScanData().stream().map(ModFileScanData::getAnnotations).flatMap(Collection::stream).filter(item ->
//                    item.annotationType().equals(Type.getType(indi.yunherry.weather.annotation.Factory.class))
//            ).map(item->item.clazz().)
//            ;
        //        Iterator<Class<?>> iterator = classList.iterator();
//        while (iterator.hasNext()) {
//            Class<?> clazz = iterator.next();
//            try {
//                if (clazz.isInterface() || clazz.isAnnotation()) {
//                    iterator.remove();
//                    continue;
//                }
//                clazz.getDeclaredConstructor().newInstance();
//            } catch (InstantiationException | NoSuchMethodException | IllegalAccessException |
//                     InvocationTargetException exception) {
//                iterator.remove();
//            }
//        }
//        classArrayList = classList;
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
//        System.out.println(Arrays.toString(classes.get("indi.yunherry.weather.annotation.Factory").toArray()));
        return classes.get("indi.yunherry.weather.annotation.Factory").stream()
        .map(item -> {
            try {
                return (Factory)(Class.forName(item.clazz().getClassName()).getDeclaredConstructor().newInstance());
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
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

    /**
     * @param model 1 只扫描注解,2只扫描继承类 3全部扫描
     */
    public <A extends Annotation> void finds(ArrayList<Class<?>> classes, ArrayList<Class<?>> trueList, Class<A> annotation, Class<?> fatherClass, ScanTypeEnum model) {
        Iterator<Class<?>> iterator = classes.iterator();
        while (iterator.hasNext()) {
            Class<?> clazz = iterator.next();
            if (model == ScanTypeEnum.SCAN_ONLY_ANNOTATION || model == ScanTypeEnum.SCAN_ONLY_FATHER_CLASS ? model == ScanTypeEnum.SCAN_ONLY_ANNOTATION ? ScanClassUntil.isSuitableAnnotation(annotation, clazz) : ScanClassUntil.isSuitableClass(clazz, fatherClass) : ScanClassUntil.isSuitableClass(annotation, clazz, fatherClass)) {
                trueList.add(clazz);
                iterator.remove();
            }
        }
    }

}
