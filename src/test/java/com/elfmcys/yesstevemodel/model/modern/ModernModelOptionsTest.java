package com.elfmcys.yesstevemodel.model.modern;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapability;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.Map;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ModernModelOptionsTest {
    static ModernModelOptions options(String form) {
        return ModernModelOptions.fromJson(YesSteveModel.GSON.fromJson("{\"animations\":[\"dance\"],\"properties\":{\"extra_animation\":{\"#group\":\"Actions\",\"empty\":\"#settings\"},\"extra_animation_classify\":[{\"id\":\"group\",\"extra_animation\":{\"dance\":\"Dance\",\"#return\":\"Back\"}}],\"extra_animation_buttons\":[{\"id\":\"settings\",\"config_forms\":[" + form + "]}]}}", JsonObject.class));
    }
    @Test void wheelNavigationAuthorizesOnlyExistingAnimations() {
        ModernModelOptions options = options("{\"type\":\"checkbox\",\"value\":\"v.hat\"}");
        assertEquals("group", options.group("").get(0).target());
        assertEquals("settings", options.group("").get(1).target());
        assertEquals(Collections.singleton("dance"), options.playable);
        assertTrue(options.settings.containsKey("variable.hat"));
    }
    @Test void rangesClampQuantizeAndRejectNonFiniteAndQueryAssignments() {
        ModernModelOptions.Form form = options("{\"type\":\"range\",\"value\":\"v.size\",\"min\":-0.75,\"max\":2,\"step\":0.05}").settings.get("variable.size");
        assertEquals(2, form.normalize(999)); assertEquals(-.75, form.normalize(-999)); assertEquals(.1, form.normalize(.12), 1e-8);
        assertThrows(IllegalArgumentException.class, () -> form.normalize(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> options("{\"type\":\"range\",\"value\":\"q.health\"}"));
    }
    @Test void radioRunsModelScriptsRatherThanAssigningIndex() {
        ModernModelOptions.Form form = options("{\"type\":\"radio\",\"value\":\"v.type\",\"labels\":{\"A\":\"v.type=0;v.hat=0;\",\"B\":\"v.type=1;v.hat=math.clamp(v.old,0,3);\"}}").settings.get("variable.type");
        Map<String, Double> values = form.apply(1, Collections.singletonMap("variable.old", 8d));
        assertEquals(1d, values.get("variable.type")); assertEquals(3d, values.get("variable.hat"));
        assertFalse(values.containsKey("variable.old"));
    }
    @Test void settingsPersistPerModelAndCapabilityCopiesRemainIndependent() {
        ModelInfoCapability source = new ModelInfoCapability();
        ResourceLocation first = new ResourceLocation("yes_steve_model", "first"), second = new ResourceLocation("yes_steve_model", "second");
        source.setModelAndTexture(first, first); source.setModelSetting("variable.hat", 1);
        source.setModelAndTexture(second, second); assertTrue(source.getModelSettings().isEmpty()); source.setModelSetting("variable.hat", 0);
        ModelInfoCapability restored = new ModelInfoCapability(); restored.deserializeNBT(source.serializeNBT());
        restored.setModelAndTexture(first, first); assertEquals(1d, restored.getModelSettings().get("variable.hat"));
        ModelInfoCapability other = new ModelInfoCapability(); other.copyFrom(restored); other.setModelSetting("variable.hat", 2);
        assertEquals(1d, restored.getModelSettings().get("variable.hat")); assertEquals(2d, other.getModelSettings().get("variable.hat"));
    }
}
