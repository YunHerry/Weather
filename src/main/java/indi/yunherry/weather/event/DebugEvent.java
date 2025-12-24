package indi.yunherry.weather.event;

import indi.yunherry.weather.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;


@Mod.EventBusSubscriber
public class DebugEvent {
    @SubscribeEvent
    public static void onRenderWindDirection(RenderGuiEvent.Post event) {
        if (!WorldContext.isDebugMode) return;
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

    //2.0可以认为在沙子上
    @SubscribeEvent
    public static void onRenderBiomeInfo(RenderGuiEvent.Post event) {
        if (!WorldContext.isDebugMode) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null) return;
        Font font = mc.font;
        GuiGraphics guiGraphics = event.getGuiGraphics();
        LocalPlayer player = mc.player;
        if (player == null) return;
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        Component text = Component.literal(String.format("群系温度: %s", level.getBiome(pos).value().getBaseTemperature()));
        // 计算屏幕位置（物品栏上方居中）
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int yPos = event.getWindow().getGuiScaledHeight() - 52;

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

    @SubscribeEvent
    public static void onRenderFPSInfo(RenderGuiEvent.Post event) {
        if (!WorldContext.isDebugMode) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug) return;
        // 修复：移除错误的屏幕宽度计算，因为我们现在使用左对齐
        // int screenWidth = event.getGuiGraphics().draw().getWidth(); // 移除这行错误代码

        if (mc.options.hideGui || mc.screen != null) return;

        Font font = mc.font;
        GuiGraphics guiGraphics = event.getGuiGraphics();
        LocalPlayer player = mc.player;
        if (player == null) return;

        // 准备数据
        Component text = Component.literal(String.format("FPS: %s", Minecraft.getInstance().getFps()));
        Component text1 = Component.literal(String.format("Rain: %f", GlobalContext.getLoaderConfig().rain()));

        // 渲染位置定义
        int xPos = 5; // 左对齐位置
        int yPos = 4; // 起始 Y 位置

        // 渲染文本 (FPS)
        guiGraphics.drawString(
                font,
                text,
                xPos, // 左对齐
                yPos,
                0xFFFFFF,
                true // 启用阴影
        );

        // 渲染文本 (Rain)
        guiGraphics.drawString(
                font,
                text1,
                xPos, // 左对齐
                yPos + 10,
                0xFFFFFF,
                true // 启用阴影
        );
    }
    @SubscribeEvent
    public static void onRenderDebugInfo(RenderGuiEvent.Post event) {
        if (!WorldContext.isDebugMode) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug) return;
        if (mc.options.hideGui || mc.screen != null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = mc.font;

        // 调试信息列表通常位于屏幕左上角
        // 避开现有的 FPS/Rain 信息 (yPos=4, yPos+10)
        int xPos = 5;
        int yPosStart = 24; // 从第 24 像素行开始绘制，与上方信息分隔
        int lineHeight = 10; // 每行文字的像素高度
        int currentY = yPosStart;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 400);
        // 遍历 GlobalContext 中存储的调试值
        for (Map.Entry<String, String> entry : GlobalContext.DEBUG_VALUES.entrySet()) {
            String debugText = entry.getKey() + ": " + entry.getValue();
            // 渲染文本
            guiGraphics.drawString(
                    font,
                    debugText,
                    xPos, // 固定左对齐
                    currentY,
                    0xFFFFFFFF, // 白色
                    true // 启用阴影 (true)
            );

            currentY += lineHeight;
        }
        guiGraphics.pose().popPose();
    }
}
