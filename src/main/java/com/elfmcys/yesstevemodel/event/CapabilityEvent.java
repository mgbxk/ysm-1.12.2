package com.elfmcys.yesstevemodel.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.api.IArrowExtraInfo;
import com.elfmcys.yesstevemodel.capability.*;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SyncArrowModel;
import com.elfmcys.yesstevemodel.network.message.SyncAuthModels;
import com.elfmcys.yesstevemodel.network.message.SyncModelInfo;
import com.elfmcys.yesstevemodel.network.message.SyncStarModels;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = YesSteveModel.MOD_ID)
public final class CapabilityEvent {
    private static final ResourceLocation MODEL_INFO_CAP = new ResourceLocation(YesSteveModel.MOD_ID, "model_id");
    private static final ResourceLocation AUTH_MODELS_CAP = new ResourceLocation(YesSteveModel.MOD_ID, "own_models");
    private static final ResourceLocation STAR_MODELS_CAP = new ResourceLocation(YesSteveModel.MOD_ID, "star_models");
    private static final ResourceLocation ARROW_MODEL_CAP = new ResourceLocation(YesSteveModel.MOD_ID, "arrow_model");

    @SubscribeEvent
    public static void onAttachCapabilityEvent(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer player) {
            if (!CapabilityEvent.getModelInfoCap(player).isPresent() && !event.getCapabilities().containsKey(MODEL_INFO_CAP)) {
                event.addCapability(MODEL_INFO_CAP, new ModelInfoCapabilityProvider());
            }
            if (!CapabilityEvent.getAuthModelsCap(player).isPresent() && !event.getCapabilities().containsKey(AUTH_MODELS_CAP)) {
                event.addCapability(AUTH_MODELS_CAP, new AuthModelsCapabilityProvider());
            }
            if (!CapabilityEvent.getStarModelsCap(player).isPresent() && !event.getCapabilities().containsKey(STAR_MODELS_CAP)) {
                event.addCapability(STAR_MODELS_CAP, new StarModelsCapabilityProvider());
            }
            return;
        }
        if (event.getObject() instanceof EntityArrow arrow) {
            if (!CapabilityEvent.getArrowModelCap(arrow).isPresent() && !event.getCapabilities().containsKey(ARROW_MODEL_CAP)) {
                event.addCapability(ARROW_MODEL_CAP, new ArrowModelCapabilityProvider());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        Optional<ModelInfoCapability> oldModelInfoCap = getModelInfoCap(event.getOriginal());
        Optional<AuthModelsCapability> oldAuthModelsCap = getAuthModelsCap(event.getOriginal());
        Optional<StarModelsCapability> oldStarModelsCap = getStarModelsCap(event.getOriginal());

        Optional<ModelInfoCapability> newModelInfoCap = getModelInfoCap(event.getEntityPlayer());
        Optional<AuthModelsCapability> newAuthModelsCap = getAuthModelsCap(event.getEntityPlayer());
        Optional<StarModelsCapability> newStarModelsCap = getStarModelsCap(event.getEntityPlayer());

        newModelInfoCap.ifPresent(newModelInfo -> oldModelInfoCap.ifPresent(newModelInfo::copyFrom));
        newAuthModelsCap.ifPresent(newAuthModels -> oldAuthModelsCap.ifPresent(newAuthModels::copyFrom));
        newStarModelsCap.ifPresent(newStarModels -> oldStarModelsCap.ifPresent(newStarModels::copyFrom));
    }

    @SubscribeEvent
    public static void onTrackingPlayer(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof EntityPlayer trackPlayer) {
            EntityPlayer player = event.getEntityPlayer();
            getModelInfoCap(trackPlayer).ifPresent(cap -> {
                SyncModelInfo syncMsg = new SyncModelInfo(trackPlayer.getEntityId(), cap);
                NetworkHandler.sendToClientPlayer(syncMsg, player);
            });
            return;
        }
        if (event.getTarget() instanceof EntityArrow arrow) {
            String modelId = ((IArrowExtraInfo) arrow).getYsmModelId();
            if (!IArrowExtraInfo.EMPTY.equals(modelId)) {
                NetworkHandler.CHANNEL.sendToAllTracking(new SyncArrowModel(arrow.getEntityId(), modelId), arrow);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPlayer player) {
            getModelInfoCap(player).ifPresent(modelInfoCap -> {
                if (player instanceof EntityPlayerMP serverPlayer) {
                    getAuthModelsCap(player).ifPresent(authModelsCap -> {
                        NetworkHandler.sendToClientPlayer(new SyncAuthModels(authModelsCap.getAuthModels()), serverPlayer);
                        if (ServerModelManager.AUTH_MODELS.contains(modelInfoCap.getModelId().getPath()) && !authModelsCap.containModel(modelInfoCap.getModelId())) {
                            ResourceLocation defaultModelId = new ResourceLocation(YesSteveModel.MOD_ID, "default");
                            ResourceLocation defaultTextureId = new ResourceLocation(YesSteveModel.MOD_ID, "default/default.png");
                            modelInfoCap.setModelAndTexture(defaultModelId, defaultTextureId);
                        }
                    });
                    SyncModelInfo syncMsg = new SyncModelInfo(serverPlayer.getEntityId(), modelInfoCap);
                    NetworkHandler.sendToClientPlayer(syncMsg, serverPlayer);
                } else {
                    modelInfoCap.markDirty();
                }
            });

            getStarModelsCap(player).ifPresent(starModelCap -> {
                if (player instanceof EntityPlayerMP serverPlayer) {
                    NetworkHandler.sendToClientPlayer(new SyncStarModels(starModelCap.getStarModels()), serverPlayer);
                }
            });
        }
    }

    /**
     * 同步客户端服务端数据
     */
    @SubscribeEvent
    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
            getModelInfoCap(player).ifPresent(cap -> {
                if (cap.isDirty()) {
                    SyncModelInfo syncMsg = new SyncModelInfo(player.getEntityId(), cap);
                    NetworkHandler.CHANNEL.sendToAllTracking(syncMsg, player);
                    NetworkHandler.sendToClientPlayer(syncMsg, player);
                    cap.setDirty(false);
                }
            });
        }
    }

    public static Optional<ModelInfoCapability> getModelInfoCap(EntityPlayer player) {
        return getCapability(player, ModelInfoCapabilityProvider.MODEL_INFO_CAP);
    }

    public static Optional<AuthModelsCapability> getAuthModelsCap(EntityPlayer player) {
        return getCapability(player, AuthModelsCapabilityProvider.AUTH_MODELS_CAP);
    }

    public static Optional<StarModelsCapability> getStarModelsCap(EntityPlayer player) {
        return getCapability(player, StarModelsCapabilityProvider.STAR_MODELS_CAP);
    }

    public static Optional<ArrowModelCapability> getArrowModelCap(EntityArrow arrow) {
        return getCapability(arrow, ArrowModelCapabilityProvider.ARROW_MODEL_CAP);
    }

    public static <T> Optional<T> getCapability(@Nullable ICapabilityProvider provider, Capability<T> capability) {
        return getCapability(provider, capability, null);
    }

    public static <T> Optional<T> getCapability(@Nullable ICapabilityProvider provider, Capability<T> capability, @Nullable EnumFacing side) {
        return provider != null && provider.hasCapability(capability, side) ?
                Optional.ofNullable(provider.getCapability(capability, side)) :
                Optional.empty();
    }
}
