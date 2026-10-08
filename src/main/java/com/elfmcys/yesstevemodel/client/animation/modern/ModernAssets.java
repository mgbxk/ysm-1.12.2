package com.elfmcys.yesstevemodel.client.animation.modern;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.model.modern.ControllerDefinition;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.ResourceLocation;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ModernAssets {
    public static final Map<ResourceLocation, Bundle> MODELS = new ConcurrentHashMap<>();

    public static final class Bundle {
        public final Map<String, ControllerDefinition> controllers = new ConcurrentHashMap<>();
        public final Map<String, ResourceLocation> sounds = new ConcurrentHashMap<>();
        public final Map<String, com.elfmcys.yesstevemodel.model.modern.MolangProgram> functions = new ConcurrentHashMap<>();
        public com.elfmcys.yesstevemodel.model.modern.ModernModelOptions options = com.elfmcys.yesstevemodel.model.modern.ModernModelOptions.empty();
    }

    private ModernAssets() {}

    public static void register(ResourceLocation model, Map<String, byte[]> resources) {
        Bundle bundle = new Bundle();
        byte[] functionData = resources.get("modern_functions");
        if (functionData != null) {
            JsonObject json = YesSteveModel.GSON.fromJson(new String(functionData, StandardCharsets.UTF_8), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                bundle.functions.put(entry.getKey(), com.elfmcys.yesstevemodel.model.modern.MolangProgram.compile(entry.getValue().getAsString(), com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache.getInstance().parser));
            }
        }
        byte[] optionData = resources.get("modern_options");
        if (optionData != null) bundle.options = com.elfmcys.yesstevemodel.model.modern.ModernModelOptions.fromJson(YesSteveModel.GSON.fromJson(new String(optionData, StandardCharsets.UTF_8), JsonObject.class));
        byte[] definitions = resources.get("modern_controllers");
        if (definitions != null) {
            JsonObject json = YesSteveModel.GSON.fromJson(new String(definitions, StandardCharsets.UTF_8), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                try {
                    bundle.controllers.put(entry.getKey(), ControllerDefinition.parse(entry.getValue().getAsJsonObject()));
                } catch (Exception e) {
                    YesSteveModel.LOGGER.error("Invalid controller {} for {}", entry.getKey(), model, e);
                }
            }
        }
        byte[] sounds = resources.get("modern_sounds");
        if (sounds != null) {
            JsonObject json = YesSteveModel.GSON.fromJson(new String(sounds, StandardCharsets.UTF_8), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                String key = entry.getValue().getAsString();
                byte[] bytes = resources.get(key);
                if (bytes == null) continue;
                ResourceLocation id = new ResourceLocation(ModelSoundPack.DOMAIN, model.getPath() + "/" + key);
                ModelSoundPack.INSTANCE.put(id, bytes);
                bundle.sounds.put(entry.getKey(), id);
            }
        }
        MODELS.put(new ResourceLocation(model.getNamespace(), model.getPath() + "/main"), bundle);
    }

    public static void clear() {
        MODELS.clear();
        ModelSoundPack.INSTANCE.clear();
        ModelSounds.stopAll();
    }
}
