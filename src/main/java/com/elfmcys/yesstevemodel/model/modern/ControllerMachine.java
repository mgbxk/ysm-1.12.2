package com.elfmcys.yesstevemodel.model.modern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

/** One instance per entity/controller, with one ordered transition per update. */
public final class ControllerMachine {
    public interface Evaluation {
        double value(String expression);
        void variable(String name, double value);
        void scripts(JsonElement scripts);
        void enterSounds(JsonElement sounds);
        void exitSounds();
    }

    private final ControllerDefinition definition;
    private String state;
    private double entered;
    private double lastTick = Double.NaN;
    private BlendTransition blendTransition = BlendTransition.NONE;
    private double blendSeconds;
    private boolean changed;

    public ControllerMachine(ControllerDefinition definition) {
        this.definition = definition;
    }

    public void update(double tick, Evaluation evaluation) {
        changed = false;
        if (state == null) enter(definition.initial, tick, evaluation);
        if (tick == lastTick) return;
        lastTick = tick;
        evaluation.variable("query.state_time", Math.max(0, tick - entered) / 20);
        JsonObject current = current();
        if (current.has("variables")) {
            for (Map.Entry<String, JsonElement> entry : current.getAsJsonObject("variables").entrySet()) {
                JsonObject variable = entry.getValue().getAsJsonObject();
                double value = evaluation.value(variable.get("input").getAsString());
                if (variable.has("remap_curve")) value = remap(variable.getAsJsonObject("remap_curve"), value);
                evaluation.variable(entry.getKey().startsWith("variable.") ? entry.getKey() : "variable." + entry.getKey(), value);
            }
        }
        if (current.has("transitions")) {
            for (JsonElement transition : current.getAsJsonArray("transitions")) {
                Map.Entry<String, JsonElement> target = transition.getAsJsonObject().entrySet().iterator().next();
                if (evaluation.value(target.getValue().getAsString()) != 0) {
                    evaluation.scripts(current.get("on_exit"));
                    evaluation.exitSounds();
                    blendTransition = definition.blendTransition(state);
                    blendSeconds = blendTransition.duration(evaluation);
                    enter(target.getKey(), tick, evaluation);
                    changed = true;
                    break;
                }
            }
        }
    }

    private void enter(String target, double tick, Evaluation evaluation) {
        state = target;
        entered = tick;
        evaluation.variable("query.state_time", 0);
        evaluation.scripts(current().get("on_entry"));
        evaluation.enterSounds(current().get("sound_effects"));
    }

    public Map<String, Double> weights(Evaluation evaluation) {
        Map<String, Double> weights = new LinkedHashMap<>();
        JsonElement animations = current().get("animations");
        if (animations == null) return weights;
        for (JsonElement animation : animations.getAsJsonArray()) {
            if (animation.isJsonPrimitive()) weights.merge(animation.getAsString(), 1d, Double::sum);
            else for (Map.Entry<String, JsonElement> entry : animation.getAsJsonObject().entrySet()) {
                double weight = evaluation.value(entry.getValue().getAsString());
                if (Double.isFinite(weight)) weights.merge(entry.getKey(), Math.max(0, weight), Double::sum);
            }
        }
        return weights;
    }

    public JsonObject current() { return definition.states.get(state == null ? definition.initial : state); }
    public String state() { return state; }
    public double elapsed(double tick) { return Math.max(0, tick - entered); }
    public boolean changed() { return changed; }
    public double blend(double tick) { return blendTransition.incomingWeight(elapsed(tick) / 20, blendSeconds); }

    public static double remap(JsonObject curve, double value) {
        java.util.TreeMap<Double, Double> points = new java.util.TreeMap<>();
        for (Map.Entry<String, JsonElement> point : curve.entrySet()) points.put(Double.parseDouble(point.getKey()), point.getValue().getAsDouble());
        if (points.isEmpty()) return value;
        Map.Entry<Double, Double> low = points.floorEntry(value), high = points.ceilingEntry(value);
        if (low == null) return points.firstEntry().getValue();
        if (high == null) return points.lastEntry().getValue();
        if (low.getKey().equals(high.getKey())) return low.getValue();
        return low.getValue() + (high.getValue() - low.getValue()) * (value - low.getKey()) / (high.getKey() - low.getKey());
    }
}
