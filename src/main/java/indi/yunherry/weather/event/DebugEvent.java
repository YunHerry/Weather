package indi.yunherry.weather.event;

import indi.yunherry.weather.AnimationController;
import indi.yunherry.weather.WindDirectionType;
import indi.yunherry.weather.WorldContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;


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
        if (mc.options.hideGui || mc.screen != null) return;
        Font font = mc.font;
        GuiGraphics guiGraphics = event.getGuiGraphics();
        LocalPlayer player = mc.player;
        if (player == null) return;
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        Component text = Component.literal(String.format("FPS: %s",  Minecraft.getInstance().getFps()));
        Component text1 = Component.literal(String.format("雨PartialTick: %f", AnimationController.getAnimationPartialTick(event.getPartialTick())));
        // 计算屏幕位置（物品栏上方居中）
        int screenWidth = 50;
        int yPos = 4;

        // 渲染文本
        guiGraphics.drawString(
                font,
                text,
                (screenWidth - font.width(text)) / 2,
                yPos,
                0xFFFFFF,
                false
        );
        guiGraphics.drawString(
                font,
                text1,
                (screenWidth + 84 - font.width(text1)) / 2,
                yPos + 10,
                0xFFFFFF,
                false
        );

    }
}
