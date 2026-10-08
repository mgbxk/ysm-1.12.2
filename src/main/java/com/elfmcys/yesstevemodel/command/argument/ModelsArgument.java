package com.elfmcys.yesstevemodel.command.argument;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Collection;
import java.util.stream.Collectors;

public final class ModelsArgument {
    public static Collection<String> listSuggestions() {
        if (FMLCommonHandler.instance().getSide().isServer()) {
            return ServerModelManager.CACHE_NAME_INFO.keySet();
        } else {
            return ClientModelManager.MODELS.keySet().stream().map(ResourceLocation::getPath).collect(Collectors.toSet());
        }
    }
}
