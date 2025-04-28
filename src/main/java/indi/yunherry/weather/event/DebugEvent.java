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
//    @SubscribeEvent
//    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
//        Player player = event.getEntity();
//
//        // 检查是否手持木棍
//        if (!player.getItemInHand(event.getHand()).is(Items.STICK)) {
//            return;
//        }
//        // 取消后续交互（防止放置方块等操作）
//        event.setCancellationResult(InteractionResult.SUCCESS);
//        event.setCanceled(true);
//
//        // 获取点击的方块位置
//        BlockPos pos = event.getPos();
//
//        // 创建坐标文本
//        Component message = Component.literal("方块位置：")
//                .append(Component.literal("[X: " + pos.getCenter().x + ", Y: " + pos.getCenter().x + ", Z: " + pos.getCenter().x + "]")
//                        .withStyle(ChatFormatting.GREEN));
//
//        player.sendSystemMessage(message);
//    }
//    @SubscribeEvent
//    public static void onRenderGui(RenderGuiEvent.Post event) {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.options.hideGui || mc.screen != null) return;
//
//        Font font = mc.font;
//        GuiGraphics guiGraphics = event.getGuiGraphics();
//
//        Component text = Component.literal(String.format("Wind Direction: %s", WindDirectionType.getWindDirectionType(WorldContext.windDirection)));
//
//        // 计算屏幕位置（物品栏上方居中）
//        int screenWidth = event.getWindow().getGuiScaledWidth();
//        int yPos = event.getWindow().getGuiScaledHeight() - 42; // 物品栏上方偏移
//
//        // 渲染文本
//        guiGraphics.drawString(
//                font,
//                text,
//                (screenWidth - font.width(text)) / 2, // 水平居中
//                yPos,
//                0xFFFFFF,
//                false // 是否带阴影
//        );
//    }
}
