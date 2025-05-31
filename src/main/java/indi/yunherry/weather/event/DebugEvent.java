package indi.yunherry.weather.event;

import indi.yunherry.weather.WindDirectionType;
import indi.yunherry.weather.WorldContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DebugEvent {
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if(!WorldContext.isDebugMode) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null) return;

        Font font = mc.font;
        GuiGraphics guiGraphics = event.getGuiGraphics();

        Component text = Component.literal(String.format("风向: %s", WindDirectionType.getWindDirectionType(WorldContext.windDirection)));

        // 计算屏幕位置（物品栏上方居中）
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int yPos = event.getWindow().getGuiScaledHeight() - 42;

        // 渲染文本
        guiGraphics.drawString(
                font,
                text,
                (screenWidth - font.width(text)) / 2,
                yPos,
                0xFFFFFF,
                false
        );
    }
}
