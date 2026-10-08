package com.elfmcys.yesstevemodel.command.sub;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.command.argument.ModelsArgument;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.SyncAuthModels;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class AuthCommand extends CommandBase {
    private static final String AUTH_NAME = "auth";
    private static final String ADD_NAME = "add";
    private static final String REMOVE_NAME = "remove";
    private static final String ALL_NAME = "all";
    private static final String CLEAR_NAME = "clear";

    @Nonnull
    @Override
    public String getName() {
        return AUTH_NAME;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.yes_steve_model.auth_model.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length < 2) throw new WrongUsageException(this.getUsage(sender));
        List<EntityPlayerMP> targets = getPlayers(server, sender, args[0]);
        switch (args[1].toLowerCase()) {
            case ADD_NAME -> {
                if (args.length != 3) throw new WrongUsageException(this.getUsage(sender));
                this.addAuthModel(sender, targets, args[2]);
            }
            case REMOVE_NAME -> {
                if (args.length != 3) throw new WrongUsageException(this.getUsage(sender));
                this.removeAuthModel(sender, targets, args[2]);
            }
            case ALL_NAME -> {
                if (args.length != 2) throw new WrongUsageException(this.getUsage(sender));
                this.addAllAuthModel(sender, targets);
            }
            case CLEAR_NAME -> {
                if (args.length != 2) throw new WrongUsageException(this.getUsage(sender));
                this.clearAuthModel(sender, targets);
            }
            default -> throw new WrongUsageException(this.getUsage(sender));
        }
    }

    private void addAuthModel(ICommandSender sender, List<EntityPlayerMP> targets, String modelName) {
        if (!ServerModelManager.CACHE_NAME_INFO.containsKey(modelName)) {
            sender.sendMessage(new TextComponentTranslation("commands.yes_steve_model.export.not_exist", modelName));
            return;
        }
        targets.forEach(player -> CapabilityEvent.getAuthModelsCap(player).ifPresent(cap -> {
            ResourceLocation modelId = new ResourceLocation(YesSteveModel.MOD_ID, modelName);
            cap.addModel(modelId);
            NetworkHandler.sendToClientPlayer(new SyncAuthModels(cap.getAuthModels()), player);
            notifyCommandListener(sender, this, "commands.yes_steve_model.auth_model.add.info",
                    modelId.getPath(), player.getName());
        }));
    }

    private void addAllAuthModel(ICommandSender sender, List<EntityPlayerMP> targets) {
        targets.forEach(player -> CapabilityEvent.getAuthModelsCap(player).ifPresent(cap -> {
            ServerModelManager.CACHE_NAME_INFO.keySet().forEach(name -> cap.addModel(new ResourceLocation(YesSteveModel.MOD_ID, name)));
            NetworkHandler.sendToClientPlayer(new SyncAuthModels(cap.getAuthModels()), player);
            notifyCommandListener(sender, this, "commands.yes_steve_model.auth_model.all.info",
                    player.getName());
        }));
    }

    private void removeAuthModel(ICommandSender sender, List<EntityPlayerMP> targets, String modelName) {
        ResourceLocation modelId = new ResourceLocation(YesSteveModel.MOD_ID, modelName);
        targets.forEach(player -> CapabilityEvent.getAuthModelsCap(player).ifPresent(ownModelsCap -> {
            ownModelsCap.removeModel(modelId);
            CapabilityEvent.getModelInfoCap(player).ifPresent(modelIdCap -> {
                if (ServerModelManager.AUTH_MODELS.contains(modelIdCap.getModelId().getPath()) && !ownModelsCap.containModel(modelIdCap.getModelId())) {
                    ResourceLocation defaultModelId = new ResourceLocation(YesSteveModel.MOD_ID, "default");
                    ResourceLocation defaultTextureId = new ResourceLocation(YesSteveModel.MOD_ID, "default/default.png");
                    modelIdCap.setModelAndTexture(defaultModelId, defaultTextureId);
                }
            });
            NetworkHandler.sendToClientPlayer(new SyncAuthModels(ownModelsCap.getAuthModels()), player);
            notifyCommandListener(sender, this, "commands.yes_steve_model.auth_model.remove.info",
                    modelId.getPath(), player.getName());
        }));
    }

    private void clearAuthModel(ICommandSender sender, List<EntityPlayerMP> targets) {
        targets.forEach(player -> CapabilityEvent.getAuthModelsCap(player).ifPresent(ownModelCap -> {
            ownModelCap.clear();
            CapabilityEvent.getModelInfoCap(player).ifPresent(modelIdCap -> {
                ResourceLocation defaultModelId = new ResourceLocation(YesSteveModel.MOD_ID, "default");
                ResourceLocation defaultTextureId = new ResourceLocation(YesSteveModel.MOD_ID, "default/default.png");
                modelIdCap.setModelAndTexture(defaultModelId, defaultTextureId);
            });
            NetworkHandler.sendToClientPlayer(new SyncAuthModels(ownModelCap.getAuthModels()), player);
            notifyCommandListener(sender, this, "commands.yes_steve_model.auth_model.clear.info",
                    player.getName());
        }));
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        return switch (args.length) {
            case 1 -> getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            case 2 -> getListOfStringsMatchingLastWord(args, ADD_NAME, REMOVE_NAME, ALL_NAME, CLEAR_NAME);
            case 3 -> ADD_NAME.equalsIgnoreCase(args[1]) || REMOVE_NAME.equalsIgnoreCase(args[1]) ?
                    getListOfStringsMatchingLastWord(args, ModelsArgument.listSuggestions()) :
                    Collections.emptyList();
            default -> Collections.emptyList();
        };
    }
}
