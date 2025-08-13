package indi.yunherry.weather.utils;
//TODO: 直接使用自己的雾气渲染管理

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.joml.Vector3f;

import static com.mojang.blaze3d.shaders.FogShape.CYLINDER;


/**
 *
 * 失明效果
 * 水下效果
 * 位于传送门内效果
 * 细雪中的效果
 * 更加明亮的水
 * 在水下能够看见天空
 * 在水下,太阳照射会存在丁达尔效应,照射下一束束光
 * 白天的水会更加明亮,夜晚的水会更加的黑
 * 在水下的时候就显示水的颜色
 * 没有设置天空颜色,而是天空盒和接缝雾之间有过渡
 * 这个类应该做到的效果: 当天气发生改变的时候,渲染距离也会发生相应的变化,同时这种变化不是突兀的
 * 当y不断减小的时候,渲染距离会随着环境变小
 * 当群系发生改变的时候,雾气颜色和天空颜色发生相应的改变
 * 2025/7/14
 */
public class FogManager {
    /*
    * 这个方法应该由事件来触发,通过事件的变更来触发这个方法,进行场景过度的判断之类的操作
    *
    *
    * */
    public static void update(FogRenderer.FogMode type) {

    }
    public static void renderSky(Camera camera, float p_109020_, ClientLevel level, int p_109022_, float p_109023_) {
        Vector3f rgb = RenderUtils.getBiomeColor(level,camera.getBlockPosition());
        System.out.println(rgb);
        RenderSystem.clearColor(rgb.x, rgb.y, rgb.z, 0.0F);
    }
    public static void updateLava(FogRenderer.FogMode type) {

    }
    /**
     *
     * 通过这个方法进行分类处理渲染
     *
     * */
    public static void render(FogRenderer.FogMode type, Camera camera,float renderDistance) {
        FogRenderer.FogData fogData = new FogRenderer.FogData(type);
        //当渲染的是这里应该是接缝处
        if(type == FogRenderer.FogMode.FOG_SKY) {
           Vector3f rgb = RenderUtils.getBiomeColor(camera.getEntity().level(),camera.getBlockPosition());
            fogData.start = 0.0F;
            fogData.end = renderDistance;
            fogData.shape = CYLINDER;
        }
    }
}
