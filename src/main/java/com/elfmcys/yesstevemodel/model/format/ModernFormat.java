package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.model.modern.ControllerDefinition;
import com.elfmcys.yesstevemodel.model.modern.PbrImages;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.Converter;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ModelProperties;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.tree.RawGeometryTree;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.Md5Utils;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Reads the public, unencrypted ysm.json spec 1/2 layout into the legacy runtime. */
public final class ModernFormat {
    private static final String[] DEFAULT_ANIMATIONS = {"main", "arm", "extra", "tac", "carryon", "arrow"};
    private static final int MAX_DEPTH = 16;

    private ModernFormat() {}

    public static void cacheAllModels(Path root) {
        scan(root, root, 0);
    }

    private static void scan(Path root, Path directory, int depth) {
        if (!Files.isDirectory(directory) || depth > MAX_DEPTH) return;
        if (Files.isRegularFile(directory.resolve(FormatManager.ROOT_FILE_NAME))) {
            String source = root.relativize(directory).toString().replace('\\', '/');
            try {
                cache(readFolder(directory, modelId(source), directory.getFileName().toString(),
                        root.equals(ServerModelManager.AUTH)));
            } catch (Exception e) {
                YesSteveModel.LOGGER.error("Cannot load modern YSM model {}: {}", directory, e.getMessage(), e);
            }
            return;
        }
        // Legacy model assets are not independent model packages.
        if (Files.isRegularFile(directory.resolve("main.json")) && Files.isRegularFile(directory.resolve("arm.json"))) return;
        try (Stream<Path> children = Files.list(directory)) {
            for (Path child : children.sorted().collect(Collectors.toList())) {
                if (Files.isSymbolicLink(child)) continue;
                if (Files.isDirectory(child)) {
                    scan(root, child, depth + 1);
                } else if (child.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip")) {
                    try (ZipFile zip = new ZipFile(child.toFile())) {
                        List<String> manifests = zip.stream().filter(e -> !e.isDirectory())
                                .map(ZipEntry::getName).filter(ModernFormat::isManifest)
                                .sorted().collect(Collectors.toList());
                        for (String manifest : manifests) {
                            String prefix = manifest.substring(0, manifest.length() - "ysm.json".length());
                            String relative = root.relativize(child).toString().replace('\\', '/');
                            relative = relative.substring(0, relative.length() - 4);
                            String identity = relative + (manifests.size() == 1 ? "" : "/" + prefix);
                            String display = prefix.isEmpty() ? child.getFileName().toString() : prefix.substring(0, prefix.length() - 1);
                            try {
                                cache(readZip(zip, prefix, modelId(identity), display, root.equals(ServerModelManager.AUTH)));
                            } catch (Exception e) {
                                YesSteveModel.LOGGER.error("Cannot load modern YSM model {}!{}: {}", child, manifest, e.getMessage(), e);
                            }
                        }
                    } catch (IOException e) {
                        YesSteveModel.LOGGER.error("Cannot read model ZIP {}: {}", child, e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            YesSteveModel.LOGGER.error("Cannot scan model directory {}: {}", directory, e.getMessage());
        }
    }

    public static boolean isManifest(String entry) {
        if (!(entry.equals("ysm.json") || entry.endsWith("/ysm.json"))) return false;
        try {
            safePath(entry);
            return entry.split("/").length <= MAX_DEPTH + 1;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean hasManifest(ZipFile zip) {
        return zip.stream().anyMatch(e -> !e.isDirectory() && isManifest(e.getName()));
    }

    private static void cache(ModelData data) throws IOException {
        if (ServerModelManager.CACHE_NAME_INFO.containsKey(data.getModelId())) {
            throw new IOException("Duplicate model id: " + data.getModelId());
        }
        ServerModelManager.CACHE_NAME_INFO.put(data.getModelId(), FormatManager.cacheModel(data));
        byte[] options = data.getModel().get("modern_options");
        if (options != null) com.elfmcys.yesstevemodel.model.modern.ModernModelOptions.SERVER_MODELS.put(data.getModelId(), com.elfmcys.yesstevemodel.model.modern.ModernModelOptions.fromJson(json(options, "modern options")));
        if (data.isAuth()) ServerModelManager.AUTH_MODELS.add(data.getModelId());
    }

    public static String modelId(String relativeName) {
        if (!relativeName.isEmpty() && ResourceUtil.isValidResourceLocation(relativeName) && relativeName.indexOf(':') < 0 && relativeName.indexOf('/') < 0) {
            return relativeName;
        }
        return "modern_" + Md5Utils.md5Hex(relativeName.getBytes(StandardCharsets.UTF_8)).toLowerCase(Locale.ROOT);
    }

    public static ModelData readFolder(Path directory, String id, String displayName, boolean auth) throws IOException {
        final Path root = directory.toRealPath();
        return read(new Resources() { @Override public byte[] read(String path) throws IOException {
            Path file = root.resolve(safePath(path)).normalize().toRealPath();
            if (!file.startsWith(root) || !Files.isRegularFile(file)) throw new IOException("Resource outside model folder: " + path);
            return Files.readAllBytes(file);
        }
            @Override public List<String> list(String path) throws IOException {
                Path folder = root.resolve(safePath(path));
                if (!Files.exists(folder)) return java.util.Collections.emptyList();
                if (!folder.toRealPath().startsWith(root)) throw new IOException("Function path outside model folder");
                try (Stream<Path> entries = Files.walk(folder, MAX_DEPTH)) {
                    return entries.filter(Files::isRegularFile).map(file -> root.relativize(file).toString().replace('\\', '/')).sorted().collect(Collectors.toList());
                }
            }
        }, id, displayName, auth, Type.FOLDER);
    }

    public static ModelData readZip(ZipFile zip, String prefix, String id, String displayName, boolean auth) throws IOException {
        if (!prefix.isEmpty()) safePath(prefix.substring(0, prefix.length() - 1));
        return read(new Resources() { @Override public byte[] read(String path) throws IOException {
            String name = prefix + safePath(path);
            ZipEntry entry = zip.getEntry(name);
            if (entry == null || entry.isDirectory()) throw new IOException("Missing ZIP resource: " + name);
            try (InputStream input = zip.getInputStream(entry)) {
                java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                return output.toByteArray();
            }
        }
            @Override public List<String> list(String path) throws IOException {
                String folder = prefix + safePath(path) + "/";
                return zip.stream().filter(entry -> !entry.isDirectory() && entry.getName().startsWith(folder)).map(entry -> entry.getName().substring(prefix.length())).sorted().collect(Collectors.toList());
            }
        }, id, displayName, auth, Type.ZIP);
    }

    private static String safePath(String path) throws IOException {
        if (path == null || path.isEmpty() || path.startsWith("/") || path.indexOf('\\') >= 0 || path.indexOf(':') >= 0) {
            throw new IOException("Expected a relative model resource path: " + path);
        }
        for (String part : path.split("/", -1)) {
            if (part.isEmpty() || part.equals(".") || part.equals("..")) throw new IOException("Invalid model resource path: " + path);
        }
        return path;
    }

    @FunctionalInterface
    private interface Resources {
        byte[] read(String path) throws IOException;
        default List<String> list(String path) throws IOException { return java.util.Collections.emptyList(); }
    }

    private static ModelData read(Resources resources, String id, String displayName, boolean auth, Type type) throws IOException {
        JsonObject manifest = json(resources.read("ysm.json"), "ysm.json");
        int spec = manifest.has("spec") ? manifest.get("spec").getAsInt() : 1;
        if (spec != 1 && spec != 2) throw new IOException("Unsupported ysm.json spec: " + spec);
        JsonObject files = requiredObject(manifest, "files");
        JsonObject player = requiredObject(files, "player");
        JsonObject modelFiles = requiredObject(player, "model");
        JsonObject properties = object(manifest, "properties");
        ExtraInfo info = metadata(manifest, displayName);
        Map<String, byte[]> models = new LinkedHashMap<>();
        Map<String, byte[]> textures = new LinkedHashMap<>();
        Map<String, byte[]> animations = new LinkedHashMap<>();

        for (String part : new String[]{"main", "arm"}) {
            String path = requiredString(modelFiles, part);
            RawGeoModel geometry = geometry(resources.read(path), path);
            if (part.equals("main")) {
                ModelProperties description = geometry.getMinecraftGeometry()[0].getProperties();
                if (properties.has("height_scale")) description.setHeightScale(properties.get("height_scale").getAsDouble());
                if (properties.has("width_scale")) description.setWidthScale(properties.get("width_scale").getAsDouble());
                description.setExtraInfo(info);
            }
            models.put(part, ObjectStreamUtil.toByteArray(geometry));
        }

        JsonElement listed = player.get("texture");
        if (listed == null || !listed.isJsonArray() || listed.getAsJsonArray().size() == 0) throw new IOException("files.player.texture must be a nonempty array");
        List<JsonElement> paths = new ArrayList<>();
        for (JsonElement texture : listed.getAsJsonArray()) {
            texturePath(texture);
            paths.add(texture);
        }
        String preferred = string(properties, "default_texture", "");
        // Stable move: retain the manifest order for all other textures.
        for (int i = 0; i < paths.size(); i++) {
            if (!preferred.isEmpty() && (texturePath(paths.get(i)).equals(preferred) || baseName(texturePath(paths.get(i))).equals(preferred))) {
                paths.add(0, paths.remove(i));
                break;
            }
        }
        int textureIndex = 0;
        for (JsonElement texture : paths) {
            String key = String.format(Locale.ROOT, "skin_%03d.png", textureIndex++);
            byte[] uv = resources.read(texturePath(texture));
            textures.put(key, uv);
            if (texture.isJsonObject()) {
                JsonObject maps = texture.getAsJsonObject();
                byte[] normal = maps.has("normal") ? resources.read(maps.get("normal").getAsString()) : null;
                byte[] specular = maps.has("specular") ? resources.read(maps.get("specular").getAsString()) : null;
                if (normal != null || specular != null) {
                    PbrImages.validate(uv, normal, specular);
                    if (normal != null) models.put("modern_pbr_" + key + ".normal", normal);
                    if (specular != null) models.put("modern_pbr_" + key + ".specular", specular);
                }
            }
        }

        JsonObject animationFiles = object(player, "animation");
        for (String name : DEFAULT_ANIMATIONS) {
            if (name.equals("arrow")) continue;
            String path = string(animationFiles, name, "");
            animations.put(name, path.isEmpty() ? defaultAnimation(name) : resources.read(path));
        }
        // Controllers can reference animations from additional player animation files.
        for (Map.Entry<String, JsonElement> entry : animationFiles.entrySet()) {
            if (!animations.containsKey(entry.getKey())) animations.put(entry.getKey(), resources.read(entry.getValue().getAsString()));
        }
        JsonObject controllers = new JsonObject();
        if (player.has("animation_controllers")) {
            for (JsonElement file : player.getAsJsonArray("animation_controllers")) {
                JsonObject definitions = requiredObject(json(resources.read(file.getAsString()), file.getAsString()), "animation_controllers");
                for (Map.Entry<String, JsonElement> entry : definitions.entrySet()) {
                    ControllerDefinition.parse(entry.getValue().getAsJsonObject());
                    if (!ControllerDefinition.isPlayerSlot(entry.getKey())) warn(id, "unsupported controller slot " + entry.getKey());
                    controllers.add(entry.getKey(), entry.getValue());
                }
            }
            models.put("modern_controllers", controllers.toString().getBytes(StandardCharsets.UTF_8));
        }
        Set<String> soundNames = new LinkedHashSet<>();
        collectSounds(controllers, soundNames);
        for (byte[] bytes : animations.values()) collectSounds(json(bytes, "animation"), soundNames);
        JsonObject sounds = new JsonObject();
        int soundBytes = 0;
        String soundPath = string(files, "sound_path", "sounds");
        for (String name : soundNames) {
            if (name.indexOf(':') >= 0) continue;
            String resource = soundPath + "/" + name + ".ogg";
            byte[] bytes = resources.read(resource);
            if (bytes.length > 4 * 1024 * 1024) throw new IOException("Sound exceeds 4 MiB: " + resource);
            if (bytes.length < 4 || bytes[0] != 'O' || bytes[1] != 'g' || bytes[2] != 'g' || bytes[3] != 'S') throw new IOException("Expected Ogg sound: " + resource);
            soundBytes += bytes.length;
            if (soundBytes > 66 * 1024 * 1024) throw new IOException("Model sounds exceed 66 MiB");
            String key = "modern_sound_" + Md5Utils.md5Hex(name.getBytes(StandardCharsets.UTF_8)).toLowerCase(Locale.ROOT);
            models.put(key, bytes);
            sounds.addProperty(name, key);
        }
        if (sounds.size() > 0) models.put("modern_sounds", sounds.toString().getBytes(StandardCharsets.UTF_8));
        JsonObject functions = new JsonObject();
        String functionPath = safePath(string(files, "function_path", "functions"));
        int functionBytes = 0;
        for (String path : resources.list(functionPath)) {
            if (!path.endsWith(".molang")) continue;
            String name = path.substring(functionPath.length() + 1, path.length() - 7).replace('/', '.').toLowerCase(Locale.ROOT);
            if (name.indexOf('@') >= 0) { warn(id, "script controller " + path); continue; }
            if (!name.matches("[\\p{L}_][\\p{L}\\p{N}_.]*") || functions.has(name)) throw new IOException("Invalid/duplicate function name: " + name);
            byte[] bytes = resources.read(path); functionBytes += bytes.length;
            if (functionBytes > 1024 * 1024 || functions.size() >= 128) throw new IOException("Model functions exceed limits");
            String source = new String(bytes, StandardCharsets.UTF_8).replace("\uFEFF", "");
            try { com.elfmcys.yesstevemodel.model.modern.MolangProgram.compile(source, new com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser()); }
            catch (RuntimeException e) { throw new IOException("Invalid function " + path + ": " + e.getMessage(), e); }
            functions.addProperty(name, source);
        }
        if (functions.size() > 0) models.put("modern_functions", functions.toString().getBytes(StandardCharsets.UTF_8));
        for (String name : new String[]{"language_path", "vehicles"}) {
            if (files.has(name)) warn(id, name);
        }
        if (properties.has("merge_multiline_expr") && properties.get("merge_multiline_expr").getAsBoolean()) {
            for (Map.Entry<String, byte[]> file : animations.entrySet()) {
                JsonObject root = json(file.getValue(), "animation");
                for (Map.Entry<String, JsonElement> clip : object(root, "animations").entrySet()) {
                    JsonObject timeline = object(clip.getValue().getAsJsonObject(), "timeline");
                    for (Map.Entry<String, JsonElement> frame : timeline.entrySet()) if (frame.getValue().isJsonArray()) {
                        StringBuilder joined = new StringBuilder();
                        for (JsonElement line : frame.getValue().getAsJsonArray()) joined.append(line.getAsString()).append('\n');
                        frame.setValue(new com.google.gson.JsonPrimitive(joined.toString()));
                    }
                }
                file.setValue(root.toString().getBytes(StandardCharsets.UTF_8));
            }
        }

        JsonObject arrow = object(files, "arrow");
        if (arrow.size() == 0 && files.has("projectiles")) {
            for (JsonElement entry : files.getAsJsonArray("projectiles")) {
                JsonObject projectile = entry.getAsJsonObject();
                JsonElement match = projectile.get("match");
                boolean matchesArrow = match != null && (match.isJsonPrimitive() && "minecraft:arrow".equals(match.getAsString()));
                if (match != null && match.isJsonArray()) {
                    for (JsonElement target : match.getAsJsonArray()) {
                        if ("minecraft:arrow".equals(target.getAsString()) || "#minecraft:arrows".equals(target.getAsString())) matchesArrow = true;
                    }
                }
                if (matchesArrow && arrow.size() == 0) arrow = projectile;
                else warn(id, "additional projectile replacement");
            }
        }
        if (arrow.size() > 0) {
            String path = requiredString(arrow, "model");
            models.put("arrow", ObjectStreamUtil.toByteArray(geometry(resources.read(path), path)));
            textures.put("arrow.png", resources.read(texturePath(arrow.get("texture"))));
            path = string(arrow, "animation", "");
            animations.put("arrow", path.isEmpty() ? defaultAnimation("arrow") : resources.read(path));
            if (arrow.has("controller")) warn(id, "arrow controller");
        }
        String foreground = string(properties, "gui_foreground", "");
        if (!foreground.isEmpty()) {
            textures.put("gui_foreground.png", resources.read(foreground));
            info.setGuiForeground("gui_foreground.png");
        }
        String background = string(properties, "gui_background", "");
        if (!background.isEmpty()) {
            textures.put("gui_background.png", resources.read(background));
            info.setGuiBackground("gui_background.png");
        }
        addExtraAliases(properties, animations, info, id);
        JsonObject options = new JsonObject(); options.add("properties", properties);
        JsonArray animationNames = new JsonArray(); Set<String> available = new LinkedHashSet<>();
        for (byte[] bytes : animations.values()) for (Map.Entry<String, JsonElement> entry : object(json(bytes, "animation"), "animations").entrySet()) available.add(entry.getKey());
        for (String name : available) animationNames.add(name);
        options.add("animations", animationNames);
        try { com.elfmcys.yesstevemodel.model.modern.ModernModelOptions.fromJson(options); }
        catch (RuntimeException e) { throw new IOException("Invalid model configuration: " + e.getMessage(), e); }
        models.put("modern_options", options.toString().getBytes(StandardCharsets.UTF_8));
        models.put("info", ObjectStreamUtil.toByteArray(info));
        return new ModelData(id, auth && !info.getFree(), type, models, textures, animations);
    }

    private static void collectSounds(JsonElement value, Set<String> names) {
        if (value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if (object.has("effect") && object.get("effect").isJsonPrimitive()) names.add(object.get("effect").getAsString());
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                // Particle effect names are unrelated to the audio registry.
                if (!entry.getKey().equals("particle_effects")) collectSounds(entry.getValue(), names);
            }
        } else if (value.isJsonArray()) {
            for (JsonElement entry : value.getAsJsonArray()) collectSounds(entry, names);
        }
    }

    private static void addExtraAliases(JsonObject properties, Map<String, byte[]> animations, ExtraInfo info, String id) throws IOException {
        JsonObject extra = object(properties, "extra_animation");
        if (extra.size() == 0) return;
        JsonObject available = new JsonObject();
        for (byte[] bytes : animations.values()) {
            for (Map.Entry<String, JsonElement> entry : object(json(bytes, "animation"), "animations").entrySet()) available.add(entry.getKey(), entry.getValue());
        }
        JsonObject aliases = new JsonObject();
        List<String> labels = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : extra.entrySet()) {
            String label = entry.getValue().getAsString();
            if (entry.getKey().startsWith("#") || label.startsWith("#")) continue;
            if (labels.size() == 8) {
                warn(id, "more than eight wheel animations (only the first eight are mapped)");
                break;
            }
            JsonElement animation = available.get(entry.getKey());
            if (animation != null) aliases.add("extra" + labels.size(), animation);
            else throw new IOException("Wheel animation not found: " + entry.getKey());
            labels.add(label);
        }
        info.setExtraAnimationNames(labels.toArray(new String[0]));
        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.8.0");
        root.add("animations", aliases);
        animations.put("modern_extra_aliases", YesSteveModel.GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
    }

    private static ExtraInfo metadata(JsonObject manifest, String displayName) throws IOException {
        JsonObject metadata = object(manifest, "metadata");
        JsonObject properties = object(manifest, "properties");
        ExtraInfo info = new ExtraInfo();
        info.setName(string(metadata, "name", displayName));
        info.setTips(string(metadata, "tips", ""));
        JsonElement license = metadata.get("license");
        if (license != null && !license.isJsonNull()) {
            info.setLicense(license.isJsonObject() ? string(license.getAsJsonObject(), "type", "All Rights Reserved") +
                    (license.getAsJsonObject().has("desc") ? "\n" + license.getAsJsonObject().get("desc").getAsString() : "") : license.getAsString());
        }
        if (metadata.has("authors")) {
            List<String> authors = new ArrayList<>();
            for (JsonElement author : metadata.getAsJsonArray("authors")) {
                authors.add(author.isJsonObject() ? requiredString(author.getAsJsonObject(), "name") : author.getAsString());
            }
            info.setAuthors(authors.toArray(new String[0]));
        }
        if (properties.has("free")) info.setFree(properties.get("free").getAsBoolean());
        info.setPreviewAnimation(string(properties, "preview_animation", "idle"));
        if (properties.has("disable_preview_rotation")) info.setDisablePreviewRotation(properties.get("disable_preview_rotation").getAsBoolean());
        return info;
    }

    private static RawGeoModel geometry(byte[] bytes, String path) throws IOException {
        JsonObject source = json(bytes, path);
        String version = string(source, "format_version", "");
        if (!version.equals("1.12.0") && !version.equals("1.14.0")) throw new IOException("Unsupported geometry format " + version + ": " + path);
        JsonArray geometries = source.getAsJsonArray("minecraft:geometry");
        if (geometries == null || geometries.size() != 1) throw new IOException("Expected exactly one minecraft:geometry: " + path);
        JsonObject geometry = geometries.get(0).getAsJsonObject();
        if (!geometry.has("description") || !geometry.has("bones")) throw new IOException("Missing geometry description or bones: " + path);
        for (JsonElement entry : geometry.getAsJsonArray("bones")) {
            JsonObject bone = entry.getAsJsonObject();
            for (String unsupported : new String[]{"poly_mesh", "texture_mesh", "texture_meshes", "binding"}) {
                if (bone.has(unsupported)) throw new IOException("Unsupported geometry feature " + unsupported + ": " + path);
            }
        }
        RawGeoModel result = Converter.fromJsonString(YesSteveModel.GSON.toJson(source));
        try {
            RawGeometryTree.parseHierarchy(result);
        } catch (RuntimeException e) {
            throw new IOException("Invalid bone hierarchy: " + path, e);
        }
        return result;
    }

    private static byte[] defaultAnimation(String name) throws IOException {
        return Files.readAllBytes(FormatManager.getDefaultAnimFile(name).toPath());
    }

    private static String texturePath(JsonElement texture) throws IOException {
        if (texture == null || texture.isJsonNull()) throw new IOException("Missing texture path");
        return texture.isJsonObject() ? requiredString(texture.getAsJsonObject(), "uv") : texture.getAsString();
    }

    private static String baseName(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.toLowerCase(Locale.ROOT).endsWith(".png") ? name.substring(0, name.length() - 4) : name;
    }

    private static JsonObject json(byte[] bytes, String name) throws IOException {
        try {
            String text = new String(bytes, StandardCharsets.UTF_8);
            if (text.startsWith("\uFEFF")) text = text.substring(1);
            JsonObject result = YesSteveModel.GSON.fromJson(text, JsonObject.class);
            if (result == null) throw new IOException("Empty JSON: " + name);
            return result;
        } catch (RuntimeException e) {
            throw new IOException("Invalid JSON: " + name, e);
        }
    }

    private static JsonObject object(JsonObject parent, String key) {
        JsonElement value = parent.get(key);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : new JsonObject();
    }

    private static JsonObject requiredObject(JsonObject parent, String key) throws IOException {
        if (!parent.has(key) || !parent.get(key).isJsonObject()) throw new IOException("Missing object: " + key);
        return parent.getAsJsonObject(key);
    }

    private static String requiredString(JsonObject parent, String key) throws IOException {
        String value = string(parent, key, "");
        if (value.isEmpty()) throw new IOException("Missing string: " + key);
        return value;
    }

    private static String string(JsonObject parent, String key, String fallback) {
        JsonElement value = parent.get(key);
        return value == null || value.isJsonNull() ? fallback : value.getAsString();
    }

    private static void warn(String id, String feature) {
        YesSteveModel.LOGGER.warn("Modern YSM model {}: unsupported feature {}", id, feature);
    }
}
