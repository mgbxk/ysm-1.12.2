package com.elfmcys.yesstevemodel;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = YesSteveModel.MOD_ID,
        name = Tags.MOD_NAME,
        version = Tags.VERSION,
        dependencies = "required-after:mixinbooter@[8.0,)",
        guiFactory = "com.elfmcys.yesstevemodel.client.config.ConfigGuiFactory"
)
public class YesSteveModel {
    public static final String MOD_ID = Tags.MOD_ID;
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    @SidedProxy(clientSide = "com.elfmcys.yesstevemodel.client.ClientProxy", serverSide = "com.elfmcys.yesstevemodel.CommonProxy")
    public static CommonProxy proxy;

    @NetworkCheckHandler
    public boolean checkRemoteMods(Map<String, String> mods, Side remoteSide) {
        return com.elfmcys.yesstevemodel.network.ConnectionPolicy.accepts(mods.get(MOD_ID), remoteSide.isServer(), Tags.VERSION);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
