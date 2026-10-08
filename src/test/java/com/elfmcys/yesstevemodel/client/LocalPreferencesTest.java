package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.capability.ModelInfoCapability;
import com.elfmcys.yesstevemodel.capability.StarModelsCapability;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class LocalPreferencesTest {
    @TempDir Path directory;
    @Test void diskRoundTripRestoresAppearanceFavoritesAndPerModelSettingsWithoutPlayingAnOldAnimation() throws Exception {
        ModelInfoCapability source = new ModelInfoCapability();
        ResourceLocation first = new ResourceLocation("yes_steve_model:first"), texture = new ResourceLocation("yes_steve_model:first/blue.png");
        source.setModelAndTexture(first, texture); source.setModelSetting("variable.hat", 1);
        source.setModelAndTexture(new ResourceLocation("yes_steve_model:second"), texture); source.setModelSetting("variable.size", 2);
        source.setModelAndTexture(first, texture); source.playAnimation("dance");
        StarModelsCapability stars = new StarModelsCapability(); stars.addModel(first);
        Path file = directory.resolve("nested/client-appearance.dat");
        LocalPreferences.write(file, LocalPreferences.capture(source, stars));
        ModelInfoCapability restored = new ModelInfoCapability(); StarModelsCapability restoredStars = new StarModelsCapability();
        LocalPreferences.restore(LocalPreferences.read(file), restored, restoredStars);
        assertEquals(first, restored.getModelId()); assertEquals(texture, restored.getSelectTexture());
        assertEquals(1d, restored.getModelSettings().get("variable.hat")); assertTrue(restoredStars.containModel(first));
        assertFalse(restored.isPlayAnimation()); assertTrue(source.isPlayAnimation());
        restored.setModelAndTexture(new ResourceLocation("yes_steve_model:second"), texture);
        assertEquals(2d, restored.getModelSettings().get("variable.size"));
    }
    @Test void missingPreferencesKeepDefaultsAndStopAnimation() throws Exception {
        ModelInfoCapability cap = new ModelInfoCapability(); ResourceLocation defaultModel = cap.getModelId(); cap.playAnimation("dance");
        LocalPreferences.restore(LocalPreferences.read(directory.resolve("missing.dat")), cap, new StarModelsCapability());
        assertEquals(defaultModel, cap.getModelId()); assertFalse(cap.isPlayAnimation());
    }
    @Test void corruptPreferencesAreReportedAndAnExistingFileCanBeReplacedAtomically() throws Exception {
        Path file = directory.resolve("appearance.dat"); Files.write(file, new byte[]{1, 2, 3});
        assertThrows(IOException.class, () -> LocalPreferences.read(file));
        NBTTagCompound saved = new NBTTagCompound(); saved.setString("marker", "first"); LocalPreferences.write(file, saved);
        saved.setString("marker", "second"); LocalPreferences.write(file, saved);
        assertEquals("second", LocalPreferences.read(file).getString("marker"));
        assertFalse(Files.exists(directory.resolve("appearance.dat.tmp")));
    }
}
