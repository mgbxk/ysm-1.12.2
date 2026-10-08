package com.elfmcys.yesstevemodel.command.sub;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.command.argument.ModelsArgument;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.YesModelUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ExportCommand extends CommandBase {
    private static final String EXPORT_NAME = "export";

    @Nonnull
    @Override
    public String getName() {
        return EXPORT_NAME;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.yes_steve_model.export.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length != 1) throw new WrongUsageException(this.getUsage(sender));
        this.exportModel(sender, args[0]);
    }

    private void exportModel(ICommandSender sender, String modelName) {
        File builtinFolder = ServerModelManager.BUILTIN.resolve(modelName).toFile();
        if (builtinFolder.isDirectory()) {
            try {
                YesModelUtils.export(builtinFolder);
                notifyCommandListener(sender, this, "commands.yes_steve_model.export.success",
                        YesSteveModel.MOD_ID, modelName);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File customFolder = ServerModelManager.CUSTOM.resolve(modelName).toFile();
        if (customFolder.isDirectory()) {
            try {
                YesModelUtils.export(customFolder);
                notifyCommandListener(sender, this, "commands.yes_steve_model.export.success",
                        YesSteveModel.MOD_ID, modelName);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File authFolder = ServerModelManager.AUTH.resolve(modelName).toFile();
        if (authFolder.isDirectory()) {
            try {
                YesModelUtils.export(authFolder);
                notifyCommandListener(sender, this, "commands.yes_steve_model.export.success",
                        YesSteveModel.MOD_ID, modelName);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sender.sendMessage(new TextComponentTranslation("commands.yes_steve_model.export.not_exist", modelName));
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        return args.length == 1 ?
                getListOfStringsMatchingLastWord(args, ModelsArgument.listSuggestions()) :
                Collections.emptyList();
    }
}
