package indi.yunherry.weather.hook;

import indi.yunherry.weather.WeatherType;
import indi.yunherry.weather.WorldContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;

public class WeatherHooks {
    /**
     * @author
     * @reason
     */
    private static int getDuration(CommandSourceStack p_265382_, int p_265171_, IntProvider p_265122_) {
        return p_265171_ == -1 ? p_265122_.sample(p_265382_.getLevel().getRandom()) : p_265171_;
    }


    /**
     * @author
     * @reason
     */
    public static int setClear(CommandSourceStack p_139173_, int p_139174_) {
        p_139173_.getLevel().setWeatherParameters(getDuration(p_139173_, p_139174_, ServerLevel.RAIN_DELAY), 0, false, false);
        p_139173_.sendSuccess(() -> {
            return Component.translatable("commands.weather.set.clear");
        }, true);
        return p_139174_;
    }

    /**
     * @author
     * @reason
     */
    public static int setRain(CommandSourceStack p_139178_, int p_139179_) {
        p_139178_.getLevel().setWeatherParameters(0, getDuration(p_139178_, p_139179_, ServerLevel.RAIN_DURATION), true, false);
        p_139178_.sendSuccess(() -> {
            WorldContext.nowWeather = WeatherType.RAIN;
            return Component.translatable("commands.weather.set.rain");
        }, true);
        return p_139179_;
    }
    /**
     * @author
     * @reason
     */
    public static int setSnow(CommandSourceStack p_139178_, int p_139179_) {
        p_139178_.getLevel().setWeatherParameters(0, getDuration(p_139178_, p_139179_, ServerLevel.RAIN_DURATION), true, false);
        p_139178_.sendSuccess(() -> {
            WorldContext.nowWeather = WeatherType.SNOW;
            return Component.translatable("weather.weather.set.snow");
        }, true);
        return p_139179_;
    }
    /**
     * @author
     * @reason
     */
    public static int setThunder(CommandSourceStack p_139183_, int p_139184_) {
        p_139183_.getLevel().setWeatherParameters(0, getDuration(p_139183_, p_139184_, ServerLevel.THUNDER_DURATION), true, true);
        p_139183_.sendSuccess(() -> {
            return Component.translatable("commands.weather.set.thunder");
        }, true);
        return p_139184_;
    }
}
