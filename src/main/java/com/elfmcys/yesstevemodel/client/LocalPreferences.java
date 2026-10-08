package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability;
import com.elfmcys.yesstevemodel.capability.StarModelsCapability;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;

/** Local appearance is kept separate from every server's player data. */
public final class LocalPreferences {
    private LocalPreferences() {}
    public static NBTTagCompound capture(ModelInfoCapability model, StarModelsCapability stars) {
        NBTTagCompound result = new NBTTagCompound();
        NBTTagCompound appearance = model.serializeNBT();
        appearance.setBoolean("play_animation", false);
        appearance.setString("animation", "idle");
        result.setTag("appearance", appearance);
        result.setTag("favorites", stars.serializeNBT());
        return result;
    }
    public static void restore(NBTTagCompound saved, ModelInfoCapability model, StarModelsCapability stars) {
        if (saved.hasKey("appearance", 10)) model.deserializeNBT(saved.getCompoundTag("appearance"));
        model.stopAnimation();
        stars.deserializeNBT(saved.getTagList("favorites", 8));
    }
    public static NBTTagCompound read(Path file) throws IOException {
        if (!Files.isRegularFile(file)) return new NBTTagCompound();
        try (InputStream stream = Files.newInputStream(file)) { return CompressedStreamTools.readCompressed(stream); }
    }
    public static void write(Path file, NBTTagCompound saved) throws IOException {
        Files.createDirectories(file.getParent());
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try (OutputStream stream = Files.newOutputStream(temporary)) { CompressedStreamTools.writeCompressed(saved, stream); }
        try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
        catch (AtomicMoveNotSupportedException unsupported) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
    }
}
