package indi.yunherry.weather.hook;

import indi.yunherry.weather.WeatherConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;

public class ConfigHandler {
    public static InteractionResult saveListener(ConfigHolder<WeatherConfig> modConfigConfigHolder, WeatherConfig modConfig) {
//        if (config.biomeTint != previousBiomeTintOption || config.ripple.useResourcepackResolution != previousUseResolutionOption || config.ripple.resolution != previousResolutionOption) {
//            Minecraft.getInstance().reloadResourcePacks();
//        }
        return InteractionResult.PASS;
    }

}
