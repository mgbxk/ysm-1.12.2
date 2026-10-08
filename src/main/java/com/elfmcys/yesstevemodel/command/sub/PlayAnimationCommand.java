package com.elfmcys.yesstevemodel.command.sub;

import com.elfmcys.yesstevemodel.command.argument.AnimationArgument;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class PlayAnimationCommand extends CommandBase {
    private static final String PLAY_NAME = "play";
    private static final String STOP = "stop";

    @Nonnull
    @Override
    public String getName() {
        return PLAY_NAME;
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "commands.yes_steve_model.play.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length != 2) throw new WrongUsageException(this.getUsage(sender));
        List<EntityPlayerMP> targets = getPlayers(server, sender, args[0]);
        this.playAnimation(targets, args[1]);
    }

    private void playAnimation(List<EntityPlayerMP> targets, String animation) throws CommandException {
        targets.forEach(player -> CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
            if (STOP.equals(animation)) {
                cap.stopAnimation();
            } else {
                cap.playAnimation(animation);
            }
        }));
    }

    @Nonnull
    @Override
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
        return switch (args.length) {
            case 1 -> getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            case 2 -> getListOfStringsMatchingLastWord(args, AnimationArgument.listSuggestions());
            default -> Collections.emptyList();
        };
    }
}
