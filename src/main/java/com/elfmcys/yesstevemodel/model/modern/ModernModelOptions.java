package com.elfmcys.yesstevemodel.model.modern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Common-side validated wheel/configuration schema, also used to authorize client packets. */
public final class ModernModelOptions {
    public static final Map<String, ModernModelOptions> SERVER_MODELS = new java.util.concurrent.ConcurrentHashMap<>();
    public final Map<String, List<Entry>> groups = new LinkedHashMap<>();
    public final Map<String, ConfigButton> buttons = new LinkedHashMap<>();
    public final Map<String, Form> settings = new LinkedHashMap<>();
    public final Set<String> animationNames = new LinkedHashSet<>();
    public final Set<String> playable = new LinkedHashSet<>();
    public boolean allCutout, renderLayersFirst, guiNoLighting;
    public static final class Entry {
        public final String key, label;
        Entry(String key, String label) { this.key = key; this.label = label; }
        public String target() { return label.startsWith("#") ? label.substring(1) : key.startsWith("#") ? key.substring(1) : key; }
        public boolean navigation() { return key.startsWith("#") || label.startsWith("#"); }
    }
    public static final class ConfigButton {
        public final String id, name; public final List<Form> forms = new ArrayList<>();
        ConfigButton(String id, String name) { this.id = id; this.name = name; }
    }
    public static final class Form {
        public final String type, title, description, variable;
        public final double min, max, step;
        public final Map<String, String> labels = new LinkedHashMap<>();
        private final List<MolangProgram> scripts = new ArrayList<>();
        Form(JsonObject json) {
            type = text(json, "type", ""); title = text(json, "title", ""); description = text(json, "description", "");
            variable = MolangProgram.canonical(text(json, "value", ""));
            min = number(json, "min", 0); max = number(json, "max", 1); step = number(json, "step", 0.1);
            if (!variable.matches("variable\\.[\\p{L}_][\\p{L}\\p{N}_.]*")) throw new IllegalArgumentException("Configuration value must be a variable: " + variable);
            if (!Double.isFinite(min) || !Double.isFinite(max) || min > max || !Double.isFinite(step) || step <= 0) throw new IllegalArgumentException("Invalid range: " + title);
            JsonObject entries = object(json, "labels");
            for (Map.Entry<String, JsonElement> entry : entries.entrySet()) labels.put(entry.getKey(), entry.getValue().getAsString());
            if (!type.equals("range") && !type.equals("checkbox") && !type.equals("radio")) throw new IllegalArgumentException("Unsupported config form: " + type);
            if (type.equals("radio")) {
                if (labels.isEmpty() || labels.size() > 128) throw new IllegalArgumentException("Radio requires 1..128 labels");
                com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser parser = new com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser();
                for (String script : labels.values()) scripts.add(MolangProgram.compile(script, parser));
            }
        }
        public Map<String, Double> apply(double value, Map<String, Double> current) {
            double normalized = normalize(value);
            Map<String, Double> changes = new LinkedHashMap<>();
            if (!type.equals("radio")) { changes.put(variable, normalized); return changes; }
            scripts.get((int) normalized).evaluate(new MolangProgram.Environment() {
                @Override public double read(String name) { return changes.getOrDefault(name, current.getOrDefault(name, 0d)); }
                @Override public void write(String name, double number) {
                    if (!name.startsWith("variable.") || changes.size() >= 128) throw new IllegalArgumentException("Invalid radio assignment");
                    changes.put(name, number);
                }
                @Override public double function(String name, double[] args) { throw new IllegalArgumentException("Radio script function is unavailable: " + name); }
                @Override public double secondOrder(String name, double input, double frequency, double damping, double response) { return input; }
            });
            return changes;
        }
        public double normalize(double value) {
            if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite configuration value");
            if (type.equals("checkbox")) return value != 0 ? 1 : 0;
            if (type.equals("radio")) return Math.max(0, Math.min(Math.max(0, labels.size() - 1), Math.rint(value)));
            value = Math.max(min, Math.min(max, value));
            return Math.max(min, Math.min(max, min + Math.round((value - min) / step) * step));
        }
    }
    public static ModernModelOptions empty() { return new ModernModelOptions(); }
    public static ModernModelOptions fromJson(JsonObject data) {
        ModernModelOptions options = new ModernModelOptions();
        if (data.has("animations")) for (JsonElement name : data.getAsJsonArray("animations")) options.animationNames.add(name.getAsString());
        JsonObject properties = object(data, "properties");
        options.allCutout = flag(properties, "all_cutout"); options.renderLayersFirst = flag(properties, "render_layers_first"); options.guiNoLighting = flag(properties, "gui_no_lighting");
        options.groups.put("", entries(object(properties, "extra_animation")));
        if (properties.has("extra_animation_classify")) for (JsonElement value : properties.getAsJsonArray("extra_animation_classify")) {
            JsonObject group = value.getAsJsonObject(); String id = text(group, "id", "");
            if (id.isEmpty() || options.groups.put(id, entries(object(group, "extra_animation"))) != null) throw new IllegalArgumentException("Duplicate/empty wheel group: " + id);
        }
        if (properties.has("extra_animation_buttons")) for (JsonElement value : properties.getAsJsonArray("extra_animation_buttons")) {
            JsonObject button = value.getAsJsonObject(); String id = text(button, "id", "");
            ConfigButton definition = new ConfigButton(id, text(button, "name", id));
            if (id.isEmpty() || options.buttons.put(id, definition) != null) throw new IllegalArgumentException("Duplicate/empty configuration button: " + id);
            if (button.has("config_forms")) for (JsonElement raw : button.getAsJsonArray("config_forms")) {
                Form form = new Form(raw.getAsJsonObject()); definition.forms.add(form); options.settings.put(form.variable, form);
            }
        }
        if (options.groups.size() > 128 || options.settings.size() > 128) throw new IllegalArgumentException("Too many model options");
        for (List<Entry> entries : options.groups.values()) {
            if (entries.size() > 128) throw new IllegalArgumentException("Too many wheel entries");
            for (Entry entry : entries) if (!entry.navigation() && options.animationNames.contains(entry.key)) options.playable.add(entry.key);
        }
        return options;
    }
    public List<Entry> group(String id) { return groups.getOrDefault(id, Collections.emptyList()); }
    private static List<Entry> entries(JsonObject json) {
        List<Entry> result = new ArrayList<>(); for (Map.Entry<String, JsonElement> entry : json.entrySet()) result.add(new Entry(entry.getKey(), entry.getValue().getAsString())); return result;
    }
    private static String text(JsonObject json, String name, String fallback) { return json.has(name) ? json.get(name).getAsString() : fallback; }
    private static double number(JsonObject json, String name, double fallback) { return json.has(name) ? json.get(name).getAsDouble() : fallback; }
    private static boolean flag(JsonObject json, String name) { return json.has(name) && json.get(name).getAsBoolean(); }
    private static JsonObject object(JsonObject json, String name) { return json.has(name) && json.get(name).isJsonObject() ? json.getAsJsonObject(name) : new JsonObject(); }
}
