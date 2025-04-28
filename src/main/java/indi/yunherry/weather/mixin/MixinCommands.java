package indi.yunherry.weather.mixin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static indi.yunherry.weather.hook.WeatherHooks.*;

@Mixin(Commands.class)
public class MixinCommands {
    //@TODO v2 实现自己的天气系统
//    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/commands/WeatherCommand;register(Lcom/mojang/brigadier/CommandDispatcher;)V"))
//    private void register(CommandDispatcher<CommandSourceStack> p_139167_) {
//        p_139167_.register(Commands.literal("weather").requires((p_139171_) -> {
//            return p_139171_.hasPermission(2);
//        }).then(Commands.literal("clear").executes((p_264806_) -> {
//            return setClear(p_264806_.getSource(), -1);
//        }).then(Commands.argument("duration", TimeArgument.time(1)).executes((p_264807_) -> {
//            return setClear(p_264807_.getSource(), IntegerArgumentType.getInteger(p_264807_, "duration"));
//        }))).then(Commands.literal("rain").executes((p_264805_) -> {
//            return setRain(p_264805_.getSource(), -1);
//        }).then(Commands.argument("duration", TimeArgument.time(1)).executes((p_264809_) -> {
//            return setRain(p_264809_.getSource(), IntegerArgumentType.getInteger(p_264809_, "duration"));
//        }))).then(Commands.literal("snow").executes((p_264805_) -> {
//            return setSnow(p_264805_.getSource(), -1);
//        }).then(Commands.argument("duration", TimeArgument.time(1)).executes((p_264809_) -> {
//            return setSnow(p_264809_.getSource(), IntegerArgumentType.getInteger(p_264809_, "duration"));
//        }))).then(Commands.literal("thunder").executes((p_264808_) -> {
//            return setThunder(p_264808_.getSource(), -1);
//        }).then(Commands.argument("duration", TimeArgument.time(1)).executes((p_264804_) -> {
//            return setThunder(p_264804_.getSource(), IntegerArgumentType.getInteger(p_264804_, "duration"));
//        }))));
//    }
//

}
