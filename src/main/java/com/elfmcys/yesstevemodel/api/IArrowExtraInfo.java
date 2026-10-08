package com.elfmcys.yesstevemodel.api;

import com.elfmcys.yesstevemodel.YesSteveModel;
import net.minecraft.util.ResourceLocation;

public interface IArrowExtraInfo {
    String DEFAULT = new ResourceLocation(YesSteveModel.MOD_ID, "default").toString();

    String EMPTY = new ResourceLocation(YesSteveModel.MOD_ID, "ysm_empty").toString();

    String TEXTURE_NAME = "arrow.png";

    /**
     * @return 箭所拥有的模型 ID
     */
    String getYsmModelId();
}
