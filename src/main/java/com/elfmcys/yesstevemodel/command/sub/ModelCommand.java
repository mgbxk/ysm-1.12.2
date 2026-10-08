package com.elfmcys.yesstevemodel.command.sub;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.command.argument.ModelsArgument;
import com.elfmcys.yesstevemodel.command.argument.TexturesArgument;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.model.format.FormatManager;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ModelCommand extends CommandBase {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().excludeFieldsWithoutExposeAnnotation().create();
    private static final String MODEL_NAME = "model";
    private static final String RELOAD_NAME = "reload";
    private static final String SET_NAME = "set";
    private static final String EXPORT_NAME = "export";

    @Nonnull
    @Override
    public String getName() {
        return MODEL_NAME;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.yes_steve_model.model.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length == 0) throw new WrongUsageException(this.getUsage(sender));
        switch (args[0].toLowerCase()) {
            case RELOAD_NAME -> {
                if (args.length != 1) throw new WrongUsageException(this.getUsage(sender));
                this.reloadAllPack(server, sender);
            }
            case EXPORT_NAME -> {
                if (args.length != 1) throw new WrongUsageException(this.getUsage(sender));
                this.exportAllPackInfo(sender);
            }
            case SET_NAME -> {
                switch (args.length) {
                    case 4 -> this.setModel(server, sender, args[1], args[2], args[3], false);
                    case 5 -> this.setModel(server, sender, args[1], args[2], args[3], parseBoolean(args[4]));
                    default -> throw new WrongUsageException("commands.yes_steve_model.model.set.usage");
                }
            }
            default -> throw new WrongUsageException(this.getUsage(sender));
        }
    }

    private void setModel(MinecraftServer server, ICommandSender sender, String targetsArg, String modelName, String textureName, boolean ignoreAuth) throws CommandException {
        List<EntityPlayerMP> targets = getPlayers(server, sender, targetsArg);

        if (!ServerModelManager.CACHE_NAME_INFO.containsKey(modelName)) {
            sender.sendMessage(new TextComponentTranslation("commands.yes_steve_model.export.not_exist",
                    modelName));
            return;
        }

        ServerModelInfo info = ServerModelManager.CACHE_NAME_INFO.get(modelName);
        if (!info.getTexture().isPresent()) return;

        ResourceLocation modelId = new ResourceLocation(YesSteveModel.MOD_ID, modelName);
        ResourceLocation textureId = ModelIdUtil.getSubModelId(modelId, textureName);

        if (ignoreAuth) {
            targets.forEach(player -> CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
                cap.setModelAndTexture(modelId, textureId);
                notifyCommandListener(sender, this, "message.yes_steve_model.model.set.success",
                        modelName, player.getName());
            }));
            return;
        }

        targets.forEach(player -> CapabilityEvent.getModelInfoCap(player).ifPresent(cap ->
                CapabilityEvent.getAuthModelsCap(player).ifPresent(authCap -> {
                    if (!ServerModelManager.AUTH_MODELS.contains(modelName) || authCap.containModel(modelId)) {
                        cap.setModelAndTexture(modelId, textureId);
                        notifyCommandListener(sender, this, "message.yes_steve_model.model.set.success",
                                modelName, player.getName());
                    } else {
                        notifyCommandListener(sender, this, "message.yes_steve_model.model.set.need_auth",
                                modelName, player.getName());
                    }
                })));
    }

    private void exportAllPackInfo(ICommandSender sender) {
        String infoText = GSON.toJson(ServerModelManager.CACHE_NAME_INFO);
        sender.sendMessage(new TextComponentString(infoText));
    }

    private void reloadAllPack(MinecraftServer server, ICommandSender sender) {
        StopWatch watch = StopWatch.createStarted();
        this.checkModelFiles(sender, ServerModelManager.BUILTIN);
        this.checkModelFiles(sender, ServerModelManager.CUSTOM);
        this.checkModelFiles(sender, ServerModelManager.AUTH);
        ServerModelManager.reloadPacks();
        if (FMLCommonHandler.instance().getSide().isClient()) {
            ServerModelManager.sendRequestSyncModelMessage();
        } else {
            ServerModelManager.sendRequestSyncModelMessage(server.getPlayerList());
        }
        server.getPlayerList().getPlayers().forEach(player -> CapabilityEvent.getAuthModelsCap(player).ifPresent(ownModelsCap -> {
            CapabilityEvent.getModelInfoCap(player).ifPresent(modelIdCap -> {
                if (ServerModelManager.AUTH_MODELS.contains(modelIdCap.getModelId().getPath()) && !ownModelsCap.containModel(modelIdCap.getModelId())) {
                    ResourceLocation defaultModelId = new ResourceLocation(YesSteveModel.MOD_ID, "default");
                    ResourceLocation defaultTextureId = new ResourceLocation(YesSteveModel.MOD_ID, "default/default.png");
                    modelIdCap.setModelAndTexture(defaultModelId, defaultTextureId);
                }
            });
        }));
        watch.stop();
        notifyCommandListener(sender, this, "message.yes_steve_model.model.reload.info", watch.getTime(TimeUnit.MICROSECONDS) / 1000.0);
    }

    private void checkModelFiles(ICommandSender sender, Path rootPath) {
        File folder = rootPath.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(folder.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collection<File> dirs = FileUtils.listFiles(folder, DirectoryFileFilter.INSTANCE, null);
        for (File dir : dirs) {
            if (new File(dir, FormatManager.ROOT_FILE_NAME).isFile()) continue;
            String dirName = dir.getName();
            if (!ResourceUtil.isValidResourceLocation(dirName)) {
                sender.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.reload.error.dir_name", dirName));
            }
            boolean noMainModelFile = true;
            boolean noArmModelFile = true;
            boolean noTextureFile = true;
            Collection<File> files = FileUtils.listFiles(rootPath.resolve(dirName).toFile(), FileFileFilter.FILE, null);
            for (File file : files) {
                String fileName = file.getName();
                if (FormatManager.MAIN_MODEL_FILE_NAME.equals(fileName) && isNotBlankFile(file)) {
                    noMainModelFile = false;
                }
                if (FormatManager.ARM_MODEL_FILE_NAME.equals(fileName) && isNotBlankFile(file)) {
                    noArmModelFile = false;
                }
                if (fileName.endsWith(".png")) {
                    noTextureFile = false;
                    String name = file.getName();
                    name = name.substring(0, name.length() - 4);
                    if (!ResourceUtil.isValidResourceLocation(name)) {
                        String showName = String.format("%s/%s.png", dirName, name);
                        sender.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.reload.error.texture_name", showName));
                    }
                }
            }
            if (noMainModelFile)
                sender.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.reload.error.no_main_file", dirName));
            if (noArmModelFile)
                sender.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.reload.error.no_arm_file", dirName));
            if (noTextureFile)
                sender.sendMessage(new TextComponentTranslation("message.yes_steve_model.model.reload.error.no_texture_file", dirName));
        }
    }

    private static boolean isNotBlankFile(File file) {
        try {
            String fileText = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            return StringUtils.isNoneBlank(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        return switch (args.length) {
            case 1 -> getListOfStringsMatchingLastWord(args, RELOAD_NAME, SET_NAME, EXPORT_NAME);
            case 2 -> SET_NAME.equalsIgnoreCase(args[0]) ?
                    getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) :
                    Collections.emptyList();
            case 3 -> SET_NAME.equalsIgnoreCase(args[0]) ?
                    getListOfStringsMatchingLastWord(args, ModelsArgument.listSuggestions()) :
                    Collections.emptyList();
            case 4 -> SET_NAME.equalsIgnoreCase(args[0]) ?
                    getListOfStringsMatchingLastWord(args, TexturesArgument.listSuggestions(args[2])) :
                    Collections.emptyList();
            case 5 -> SET_NAME.equalsIgnoreCase(args[0]) ?
                    getListOfStringsMatchingLastWord(args, "true", "false") :
                    Collections.emptyList();
            default -> Collections.emptyList();
        };
    }
}
