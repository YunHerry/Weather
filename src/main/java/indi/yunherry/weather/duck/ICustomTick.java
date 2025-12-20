package indi.yunherry.weather.duck;

import indi.yunherry.weather.CustomBlockEntityThreadPool;
import indi.yunherry.weather.TickBlockInfo;

public interface ICustomTick {
    public void weather$tick(TickBlockInfo info);
}
