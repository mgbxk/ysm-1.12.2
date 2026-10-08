package com.elfmcys.yesstevemodel.command;

import com.elfmcys.yesstevemodel.command.sub.*;
import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nonnull;

/*
这里规定一下，失败操作用sender.sendMessage（后台看不到），成功操作用notifyCommandListener（后台可见）
 */
public class RootCommand extends CommandTreeBase {
    private static final String ROOT_NAME = "ysm";

    public RootCommand() {
        this.addSubcommand(new ModelCommand());
        this.addSubcommand(new AuthCommand());
        this.addSubcommand(new ExportCommand());
        this.addSubcommand(new PlayAnimationCommand());
        this.addSubcommand(new ManageCommand());
    }

    @Nonnull
    @Override
    public String getName() {
        return ROOT_NAME;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.yes_steve_model.ysm.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
