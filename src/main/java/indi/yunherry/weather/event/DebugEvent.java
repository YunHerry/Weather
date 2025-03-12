package indi.yunherry.weather.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DebugEvent {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();

        // 检查是否手持木棍
        if (!player.getItemInHand(event.getHand()).is(Items.STICK)) {
            return;
        }
        // 取消后续交互（防止放置方块等操作）
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);

        // 获取点击的方块位置
        BlockPos pos = event.getPos();

        // 创建坐标文本
        Component message = Component.literal("方块位置：")
                .append(Component.literal("[X: " + pos.getX() + ", Y: " + pos.getY() + ", Z: " + pos.getZ() + "]")
                        .withStyle(ChatFormatting.GREEN));

        // 发送给玩家
        player.sendSystemMessage(message);
    }
}
