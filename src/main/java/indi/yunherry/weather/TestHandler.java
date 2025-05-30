package indi.yunherry.weather;

import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class TestHandler {
    @SubscribeEvent
    public void renderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            // 这是最接近原来的 RenderLevelLastEvent 的阶段
            // 此时实体、水、半透明等都渲染完了
            // 可以安全地做后处理、shader 效果
        }
    }
}
