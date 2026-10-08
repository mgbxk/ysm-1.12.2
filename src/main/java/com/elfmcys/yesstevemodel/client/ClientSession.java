package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.util.ThreadTools;
import com.elfmcys.yesstevemodel.util.UuidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = YesSteveModel.MOD_ID)
public final class ClientSession {
    private enum Mode { DISCONNECTED, CONNECTING, LOCAL, SYNCED }
    private static Mode mode = Mode.DISCONNECTED;
    private static NetworkManager manager;
    private static EntityPlayerSP appliedPlayer;
    private static boolean modelsReady;
    private static NBTTagCompound saved = new NBTTagCompound();
    private static final Path PREFERENCES = ServerModelManager.FOLDER.resolve("client-appearance.dat");
    private static final com.elfmcys.yesstevemodel.network.RemoteYsmServers REMOTE_SERVERS = new com.elfmcys.yesstevemodel.network.RemoteYsmServers();

    @SubscribeEvent public static void registered(FMLNetworkEvent.CustomPacketRegistrationEvent<?> event) {
        if (event.getSide().isClient()) REMOTE_SERVERS.update(event.getManager(), event.getRegistrations(), "REGISTER".equals(event.getOperation()));
    }

    public static boolean isLocal() { return mode == Mode.LOCAL; }
    public static boolean isSynced() { return mode == Mode.SYNCED; }

    @SubscribeEvent public static void connected(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            manager = event.getManager(); mode = Mode.CONNECTING;
            appliedPlayer = null; modelsReady = false;
            ClientModelManager.clearModels();
        });
    }
    @SubscribeEvent public static void disconnected(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        REMOTE_SERVERS.forget(event.getManager());
        Minecraft.getMinecraft().addScheduledTask(() -> {
            // A delayed old disconnect must not tear down a new connection.
            if (manager != event.getManager()) return;
            mode = Mode.DISCONNECTED; manager = null; appliedPlayer = null; modelsReady = false;
            ClientModelManager.clearModels(); ClientModelManager.loadDefaultModel();
        });
    }
    /** Server requests arrive before the first client tick on some connections. */
    public static void serverSyncRequested() { mode = Mode.SYNCED; modelsReady = false; appliedPlayer = null; }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END || mc.player == null || mc.getConnection() == null) return;
        if (mode == Mode.CONNECTING) {
            boolean serverHasYsm = mc.isIntegratedServerRunning() || REMOTE_SERVERS.contains(manager);
            if (serverHasYsm) { mode = Mode.SYNCED; }
            else {
                mode = Mode.LOCAL;
                try { saved = LocalPreferences.read(PREFERENCES); }
                catch (Exception invalid) { saved = new NBTTagCompound(); YesSteveModel.LOGGER.warn("Cannot read local appearance", invalid); }
                reloadLocalModels();
                mc.player.sendMessage(new net.minecraft.util.text.TextComponentString("YSM：已启用纯客户端模式，模型与动作仅自己可见。"));
                YesSteveModel.LOGGER.info("YSM connection mode: LOCAL (server has no YSM)");
            }
        }
        if (isLocal() && modelsReady && appliedPlayer != mc.player) applyAppearance();
    }

    public static void reloadLocalModels() {
        if (!isLocal()) return;
        modelsReady = false; appliedPlayer = null;
        ClientModelManager.clearModels(); ClientModelManager.loadDefaultModel();
        // Only the player's own installed packs are parsed, never a previous server's cache.
        ServerModelManager.reloadPacks();
        final long generation = ClientModelManager.generation;
        final List<ServerModelInfo> packs = new ArrayList<>(ServerModelManager.CACHE_NAME_INFO.values());
        final byte[] uuid = UuidUtils.asBytes(Minecraft.getMinecraft().player.getUniqueID());
        final byte[] password;
        try { password = EncryptTools.encryptPassword(uuid, EncryptTools.writePassword()); }
        catch (Exception failure) { YesSteveModel.LOGGER.error("Cannot load local models", failure); return; }
        ThreadTools.THREAD_POOL.submit(() -> {
            List<ModelData> models = new ArrayList<>();
            for (ServerModelInfo pack : packs) {
                if (pack.isNeedAuth()) continue;
                if (generation != ClientModelManager.generation) return;
                try {
                    ModelData data = EncryptTools.decryptModel(uuid, password, Files.readAllBytes(ServerModelManager.CACHE_SERVER.resolve(pack.getMd5())));
                    if (data != null) models.add(data);
                } catch (Exception invalid) { YesSteveModel.LOGGER.warn("Cannot load local model {}", pack.getMd5(), invalid); }
            }
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (!isLocal() || generation != ClientModelManager.generation) return;
                for (ModelData data : models) {
                    try { ClientModelManager.registerAll(data); }
                    catch (Exception invalid) { YesSteveModel.LOGGER.warn("Cannot register local model {}", data.getModelId(), invalid); }
                }
                modelsReady = true; appliedPlayer = null;
                if (Minecraft.getMinecraft().player != null) applyAppearance();
                if (Minecraft.getMinecraft().currentScreen instanceof com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen)
                    Minecraft.getMinecraft().displayGuiScreen(new com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen());
                YesSteveModel.LOGGER.info("Local model library ready: {} models", ClientModelManager.MODELS.size());
            });
        });
    }

    private static void applyAppearance() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        CapabilityEvent.getModelInfoCap(player).ifPresent(model -> CapabilityEvent.getStarModelsCap(player).ifPresent(stars -> {
            try { LocalPreferences.restore(saved, model, stars); }
            catch (Exception invalid) { YesSteveModel.LOGGER.warn("Invalid saved local appearance", invalid); }
            List<ResourceLocation> textures = ClientModelManager.MODELS.get(model.getModelId());
            if (textures == null || textures.isEmpty()) {
                ResourceLocation fallback = new ResourceLocation(YesSteveModel.MOD_ID, "default");
                textures = ClientModelManager.MODELS.get(fallback);
                if (textures != null && !textures.isEmpty()) model.setModelAndTexture(fallback, textures.get(0));
            } else if (!textures.contains(model.getSelectTexture())) model.setSelectTexture(textures.get(0));
        }));
        appliedPlayer = player;
    }
    public static void saveAppearance() {
        if (!isLocal()) return;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        CapabilityEvent.getModelInfoCap(player).ifPresent(model -> CapabilityEvent.getStarModelsCap(player).ifPresent(stars -> {
            saved = LocalPreferences.capture(model, stars);
            try { LocalPreferences.write(PREFERENCES, saved); }
            catch (Exception failure) { YesSteveModel.LOGGER.warn("Cannot save local appearance", failure); }
        }));
    }
    public static boolean shouldRender(net.minecraft.entity.player.EntityPlayer player) {
        if (!(isSynced() || isLocal() && player == Minecraft.getMinecraft().player)) return false;
        return CapabilityEvent.getModelInfoCap(player).map(cap -> {
            List<ResourceLocation> textures = ClientModelManager.MODELS.get(cap.getModelId());
            return textures != null && textures.contains(cap.getSelectTexture())
                    && com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache.getInstance().getGeoModels()
                    .containsKey(com.elfmcys.yesstevemodel.util.ModelIdUtil.getMainId(cap.getModelId()));
        }).orElse(false);
    }
}
