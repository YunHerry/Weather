package indi.yunherry.weather.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;

public class DebugCommand {
//    public static void registerCommand(RegisterCommandsEvent event) {
//        var dispatcher = event.getDispatcher();
//
//        LiteralArgumentBuilder<CommandSourceStack> setWindCommand = Commands
//                .literal("setWind")
//                .requires(source -> source.hasPermission(2)) // 需要OP权限
//                .then(Commands.argument("rotation", FloatArgumentType.floatArg(0.0f, 360.0f))
//                        .executes(ctx -> {
////                            WeatherRenderer.rotationWind = FloatArgumentType.getFloat(ctx, "rotation");
//                            return Command.SINGLE_SUCCESS;
//                        }));
//
//        dispatcher.register(setWindCommand);
//    }
}
