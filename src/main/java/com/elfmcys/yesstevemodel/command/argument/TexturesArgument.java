package com.elfmcys.yesstevemodel.command.argument;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class TexturesArgument {
    public static Collection<String> listSuggestions(String modelName) {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            if (ServerModelManager.CACHE_NAME_INFO.containsKey(modelName)) {
                return ServerModelManager.CACHE_NAME_INFO.get(modelName).getTextures();
            }
        } else {
            ResourceLocation modelId = new ResourceLocation(YesSteveModel.MOD_ID, modelName);
            if (ClientModelManager.MODELS.containsKey(modelId)) {
                List<ResourceLocation> textures = ClientModelManager.MODELS.get(modelId);
                return textures.stream().map(ModelIdUtil::getSubNameFromId).filter(StringUtils::isNoneBlank).collect(Collectors.toList());
            }
        }
        return Collections.emptySet();
    }
}
