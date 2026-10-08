package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.modern.ModernAssets;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.model.modern.ControllerDefinition;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ClientAnimationMergeTest {
    private static byte[] animations(String text) { return text.replace('\'', '"').getBytes(StandardCharsets.UTF_8); }

    @Test void bodyClipsWinAgainstFirstPersonPlaceholdersRegardlessOfFileOrder() throws Exception {
        ResourceLocation id = new ResourceLocation("yes_steve_model", "merge_test/main");
        ModernAssets.Bundle bundle = new ModernAssets.Bundle();
        bundle.controllers.put("player.main", ControllerDefinition.parse(YesSteveModel.GSON.fromJson("{\"states\":{\"default\":{}}}", JsonObject.class)));
        ModernAssets.MODELS.put(id, bundle);
        byte[] main = animations("{'animations':{'parallel2':{'loop':true,'bones':{'BlackHat':{'scale':0}}}}}");
        byte[] firstPerson = animations("{'animations':{'parallel2':{'loop':true},'arm_only':{'loop':true}}}");
        try {
            for (boolean reversed : new boolean[]{false, true}) {
                Map<String, byte[]> files = new LinkedHashMap<>();
                if (reversed) files.put("fp_arm", firstPerson);
                files.put("main", main);
                if (!reversed) files.put("fp_arm", firstPerson);
                files.put("extra", animations("{'animations':{'extra5':{'loop':true}}}"));
                files.put("arrow", animations("{'animations':{'arrow_only':{'loop':true}}}"));
                ClientModelManager.registerAnimations(id, files);
                AnimationFile merged = GeckoLibCache.getInstance().getAnimations().get(id);
                assertEquals("BlackHat", merged.getAnimation("parallel2").boneAnimations.get(0).boneName);
                assertEquals(0, merged.getAnimation("parallel2").boneAnimations.get(0).scaleKeyFrames.xKeyFrames.get(0).getEndValue().get());
                assertNotNull(merged.getAnimation("extra5"));
                assertNotNull(merged.getAnimation("arm_only"));
                assertNull(merged.getAnimation("arrow_only"));
                assertTrue(files.containsKey("arrow"));
                assertNotNull(GeckoLibCache.getInstance().getAnimations().get(new ResourceLocation("yes_steve_model", "merge_test/arrow")).getAnimation("arrow_only"));
            }
        } finally {
            ModernAssets.MODELS.remove(id);
            GeckoLibCache.getInstance().getAnimations().remove(id);
            GeckoLibCache.getInstance().getAnimations().remove(new ResourceLocation("yes_steve_model", "merge_test/arrow"));
        }
    }
}
