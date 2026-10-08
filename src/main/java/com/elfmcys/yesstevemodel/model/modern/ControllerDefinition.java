package com.elfmcys.yesstevemodel.model.modern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/** Public Bedrock controller data; independent of client-only Minecraft classes. */
public final class ControllerDefinition {
    public final String initial;
    public final Map<String, JsonObject> states;
    private final Map<String, BlendTransition> blends;

    private ControllerDefinition(String initial, Map<String, JsonObject> states, Map<String, BlendTransition> blends) {
        this.initial = initial;
        this.states = states;
        this.blends = blends;
    }

    public static ControllerDefinition parse(JsonObject json) throws IOException {
        if (!json.has("states") || !json.get("states").isJsonObject()) throw new IOException("Controller requires states");
        Map<String, JsonObject> states = new LinkedHashMap<>();
        Map<String, BlendTransition> blends = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("states").entrySet()) {
            if (!entry.getValue().isJsonObject()) throw new IOException("Invalid controller state: " + entry.getKey());
            JsonObject state = entry.getValue().getAsJsonObject();
            states.put(entry.getKey(), state);
            try { blends.put(entry.getKey(), BlendTransition.parse(state.get("blend_transition"))); }
            catch (IOException e) { throw new IOException("Invalid blend_transition in state " + entry.getKey() + ": " + e.getMessage(), e); }
        }
        String initial = json.has("initial_state") ? json.get("initial_state").getAsString() : "default";
        if (!states.containsKey(initial)) throw new IOException("Missing initial controller state: " + initial);
        for (JsonObject state : states.values()) {
            if (!state.has("transitions")) continue;
            for (JsonElement transition : state.getAsJsonArray("transitions")) {
                if (!transition.isJsonObject() || transition.getAsJsonObject().size() != 1) throw new IOException("Each transition must have exactly one target");
                String target = transition.getAsJsonObject().entrySet().iterator().next().getKey();
                if (!states.containsKey(target)) throw new IOException("Unknown controller transition target: " + target);
            }
        }
        return new ControllerDefinition(initial, states, blends);
    }

    BlendTransition blendTransition(String state) { return blends.get(state); }

    public static boolean isPlayerSlot(String name) {
        if (name.matches("player\\.(pre_parallel_|parallel_)[0-7]")) return true;
        return Arrays.asList("player.main", "player.pre_main", "player.post_main", "player.hold_mainhand", "player.hold_offhand",
                "player.pre_hold", "player.post_hold", "player.swing", "player.pre_swing", "player.post_swing",
                "player.use", "player.pre_use", "player.post_use", "player.armor_feet", "player.armor_legs",
                "player.armor_chest", "player.armor_head").contains(name);
    }
}
