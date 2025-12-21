package indi.yunherry.weather.duck;

import indi.yunherry.weather.CustomBlockEntityThreadPool;
import indi.yunherry.weather.TickBlockInfo;
import net.minecraft.client.multiplayer.ClientLevel;

public interface ICustomTick {
    public void weather$tick(ClientLevel level, TickBlockInfo info);
}
