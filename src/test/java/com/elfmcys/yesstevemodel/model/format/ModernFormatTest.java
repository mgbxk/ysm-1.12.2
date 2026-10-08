package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.FormatVersion;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.tree.RawGeometryTree;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.GeoBuilder;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ModernFormatTest {
    @TempDir Path temp;
    private static final Path BUILTIN = Paths.get("src/main/resources/assets/yes_steve_model/builtin");

    @BeforeAll
    static void defaultAnimations() throws IOException {
        Path target = ServerModelManager.BUILTIN.resolve("default");
        Files.createDirectories(target);
        for (String name : new String[]{"main", "arm", "extra", "tac", "carryon", "arrow"}) {
            Files.copy(BUILTIN.resolve("default/" + name + ".animation.json"), target.resolve(name + ".animation.json"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private JsonObject fixture() throws IOException {
        Files.createDirectories(temp.resolve("模型"));
        Files.createDirectories(temp.resolve("Textures"));
        Files.copy(BUILTIN.resolve("default/main.json"), temp.resolve("模型/主体.json"));
        Files.copy(BUILTIN.resolve("default/arm.json"), temp.resolve("模型/手臂.json"));
        Files.copy(BUILTIN.resolve("default/default.png"), temp.resolve("Textures/默认.png"));
        Files.copy(BUILTIN.resolve("default/blue.png"), temp.resolve("Textures/蓝色.png"));
        JsonObject root = new JsonObject();
        root.addProperty("spec", 2);
        JsonObject metadata = new JsonObject();
        metadata.addProperty("name", "测试角色");
        JsonArray authors = new JsonArray();
        JsonObject author = new JsonObject();
        author.addProperty("name", "测试作者");
        authors.add(author);
        metadata.add("authors", authors);
        JsonObject license = new JsonObject();
        license.addProperty("type", "CC0");
        metadata.add("license", license);
        root.add("metadata", metadata);
        JsonObject properties = new JsonObject();
        properties.addProperty("height_scale", 1.2);
        properties.addProperty("width_scale", 0.8);
        properties.addProperty("default_texture", "蓝色");
        properties.addProperty("preview_animation", "walk");
        root.add("properties", properties);
        JsonObject models = new JsonObject();
        models.addProperty("main", "模型/主体.json");
        models.addProperty("arm", "模型/手臂.json");
        JsonArray textures = new JsonArray();
        textures.add("Textures/默认.png");
        JsonObject blue = new JsonObject();
        blue.addProperty("uv", "Textures/蓝色.png");
        textures.add(blue);
        JsonObject player = new JsonObject();
        player.add("model", models);
        player.add("texture", textures);
        JsonObject files = new JsonObject();
        files.add("player", player);
        root.add("files", files);
        return root;
    }

    private void save(JsonObject root) throws IOException {
        Files.write(temp.resolve("ysm.json"), YesSteveModel.GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
    }

    private ModelData read(JsonObject root) throws IOException {
        save(root);
        return ModernFormat.readFolder(temp, "test", "test", false);
    }

    private RawGeoModel main(ModelData data) {
        return (RawGeoModel) ObjectStreamUtil.toObject(data.getModel().get("main"));
    }

    private ExtraInfo info(ModelData data) {
        return (ExtraInfo) ObjectStreamUtil.toObject(data.getModel().get("info"));
    }

    @Test void loadsManifestWithUnicodeAndNestedPaths() throws Exception {
        ModelData data = read(fixture());
        assertEquals("测试角色", info(data).getName());
        assertArrayEquals(new String[]{"测试作者"}, info(data).getAuthors());
        assertEquals("CC0", info(data).getLicense());
        assertEquals(1.2, main(data).getMinecraftGeometry()[0].getProperties().getHeightScale(), 0.0001);
        assertEquals(0.8, main(data).getMinecraftGeometry()[0].getProperties().getWidthScale(), 0.0001);
        assertEquals("walk", info(data).getPreviewAnimation());
        assertEquals(5, data.getAnimation().size());
    }

    @Test void discoversDefaultFunctionDirectoryAndTransfersScriptsInEncryptedCache() throws Exception {
        JsonObject root = fixture(); Files.createDirectories(temp.resolve("functions/nested"));
        Files.write(temp.resolve("functions/nested/hair.molang"), "return args[0]*2;".getBytes(StandardCharsets.UTF_8));
        ModelData data = read(root);
        JsonObject functions = YesSteveModel.GSON.fromJson(new String(data.getModel().get("modern_functions"), StandardCharsets.UTF_8), JsonObject.class);
        assertEquals("return args[0]*2;", functions.get("nested.hair").getAsString());
        EncryptTools.createRandomPassword(); byte[] uuid = new byte[16];
        ModelData received = EncryptTools.decryptModel(uuid, EncryptTools.encryptPassword(uuid, EncryptTools.writePassword()), EncryptTools.assembleEncryptModels(data));
        assertArrayEquals(data.getModel().get("modern_functions"), received.getModel().get("modern_functions"));
        assertArrayEquals(data.getModel().get("modern_options"), received.getModel().get("modern_options"));
    }

    @Test void malformedFunctionFailsModelLoadInsteadOfDroppingHairChannels() throws Exception {
        JsonObject root = fixture(); Files.createDirectories(temp.resolve("scripts"));
        root.getAsJsonObject("files").addProperty("function_path", "scripts");
        Files.write(temp.resolve("scripts/hair.molang"), "return fn.missing( ;".getBytes(StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> read(root));
    }

    @Test void publicModelLoadsFunctionsAndNineConfigFormsWithoutChangingSourceAssets() throws Exception {
        ModelData data = ModernFormat.readFolder(Paths.get("examples/naytotime"), "public", "NaytoTime", false);
        JsonObject scripts = YesSteveModel.GSON.fromJson(new String(data.getModel().get("modern_functions"), StandardCharsets.UTF_8), JsonObject.class);
        assertTrue(scripts.has("nt_hair")); assertTrue(scripts.has("nt_ui"));
        com.elfmcys.yesstevemodel.model.modern.ModernModelOptions options = com.elfmcys.yesstevemodel.model.modern.ModernModelOptions.fromJson(YesSteveModel.GSON.fromJson(new String(data.getModel().get("modern_options"), StandardCharsets.UTF_8), JsonObject.class));
        assertEquals(9, options.settings.size()); assertEquals(3, options.group("").size());
        assertTrue(options.playable.contains("AvgnDance")); assertTrue(options.guiNoLighting);
        assertArrayEquals(Files.readAllBytes(Paths.get("examples/naytotime/textures/texture.png")), data.getTexture().get("skin_000.png"));
    }

    @Test void joinsMultilineTimelineBlocksOnlyWhenModelRequestsIt() throws Exception {
        JsonObject root = fixture(); root.getAsJsonObject("properties").addProperty("merge_multiline_expr", true);
        Files.write(temp.resolve("timeline.json"), "{\"format_version\":\"1.8.0\",\"animations\":{\"a\":{\"timeline\":{\"0\":[\"1?{\",\"v.a=2;\",\"};\"]}}}}".getBytes(StandardCharsets.UTF_8));
        JsonObject paths = new JsonObject(); paths.addProperty("main", "timeline.json"); root.getAsJsonObject("files").getAsJsonObject("player").add("animation", paths);
        JsonObject animation = YesSteveModel.GSON.fromJson(new String(read(root).getAnimation().get("main"), StandardCharsets.UTF_8), JsonObject.class);
        String script = animation.getAsJsonObject("animations").getAsJsonObject("a").getAsJsonObject("timeline").get("0").getAsString();
        assertEquals("1?{\nv.a=2;\n};\n", script);
        com.elfmcys.yesstevemodel.model.modern.MolangProgram.compile(script, new com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser());
    }

    @Test void carriesControllersSoundAndPbrWithoutAddingSkinEntries() throws Exception {
        JsonObject root = fixture();
        JsonObject player = root.getAsJsonObject("files").getAsJsonObject("player");
        player.getAsJsonArray("texture").get(1).getAsJsonObject().addProperty("normal", "Textures/蓝色.png");
        Files.createDirectories(temp.resolve("controller"));
        Files.createDirectories(temp.resolve("声音"));
        byte[] ogg = {'O', 'g', 'g', 'S', 0};
        Files.write(temp.resolve("声音/测试.ogg"), ogg);
        Files.write(temp.resolve("controller/player.json"), "{\"animation_controllers\":{\"player.main\":{\"states\":{\"default\":{\"animations\":[\"idle\"],\"sound_effects\":[{\"effect\":\"测试\"}]}}}}}".getBytes(StandardCharsets.UTF_8));
        JsonArray controllers = new JsonArray(); controllers.add("controller/player.json");
        player.add("animation_controllers", controllers);
        root.getAsJsonObject("files").addProperty("sound_path", "声音");
        ModelData data = read(root);
        assertEquals(2, data.getTexture().size());
        assertArrayEquals(data.getTexture().get("skin_000.png"), data.getModel().get("modern_pbr_skin_000.png.normal"));
        assertNotNull(data.getModel().get("modern_controllers"));
        JsonObject sounds = YesSteveModel.GSON.fromJson(new String(data.getModel().get("modern_sounds"), StandardCharsets.UTF_8), JsonObject.class);
        assertArrayEquals(ogg, data.getModel().get(sounds.get("测试").getAsString()));
        EncryptTools.createRandomPassword(); byte[] uuid = new byte[16];
        ModelData received = EncryptTools.decryptModel(uuid, EncryptTools.encryptPassword(uuid, EncryptTools.writePassword()), EncryptTools.assembleEncryptModels(data));
        assertArrayEquals(data.getModel().get("modern_controllers"), received.getModel().get("modern_controllers"));
        assertArrayEquals(ogg, received.getModel().get(sounds.get("测试").getAsString()));
    }

    @Test void distributedDemoIncludesUsableVorbisAudioAndPbr() throws Exception {
        ModelData data = ModernFormat.readFolder(Paths.get("examples/controller_sound_pbr"), "demo", "demo", false);
        assertEquals(2, data.getTexture().size());
        assertNotNull(data.getModel().get("modern_pbr_skin_000.png.normal"));
        assertNotNull(data.getModel().get("modern_pbr_skin_000.png.specular"));
        JsonObject sounds = YesSteveModel.GSON.fromJson(new String(data.getModel().get("modern_sounds"), StandardCharsets.UTF_8), JsonObject.class);
        byte[] audio = data.getModel().get(sounds.get("提示").getAsString());
        assertTrue(new String(audio, StandardCharsets.ISO_8859_1).contains("vorbis"));
        assertTrue(audio.length > 1000);
        JsonObject controllers = YesSteveModel.GSON.fromJson(new String(data.getModel().get("modern_controllers"), StandardCharsets.UTF_8), JsonObject.class);
        assertTrue(controllers.has("player.main"));
        assertNotNull(data.getAnimation().get("demo"));
    }

    @Test void rejectsMissingSoundAndInvalidPbr() throws Exception {
        JsonObject root = fixture();
        JsonObject player = root.getAsJsonObject("files").getAsJsonObject("player");
        player.getAsJsonArray("texture").get(1).getAsJsonObject().addProperty("normal", "Textures/不存在.png");
        assertThrows(IOException.class, () -> read(root));
        player.getAsJsonArray("texture").get(1).getAsJsonObject().remove("normal");
        Files.write(temp.resolve("sound.animation.json"), "{\"animations\":{\"a\":{\"sound_effects\":{\"0\":{\"effect\":\"缺失\"}}}}}".getBytes(StandardCharsets.UTF_8));
        JsonObject animations = new JsonObject(); animations.addProperty("main", "sound.animation.json"); player.add("animation", animations);
        assertThrows(IOException.class, () -> read(root));
    }

    @Test void preservesDefaultTextureAndOrderThroughWireCache() throws Exception {
        ModelData data = read(fixture());
        assertArrayEquals(Files.readAllBytes(temp.resolve("Textures/蓝色.png")), data.getTexture().get("skin_000.png"));
        EncryptTools.createRandomPassword();
        byte[] uuid = new byte[16];
        ModelData received = EncryptTools.decryptModel(uuid, EncryptTools.encryptPassword(uuid, EncryptTools.writePassword()), EncryptTools.assembleEncryptModels(data));
        assertNotNull(received);
        assertEquals(Arrays.asList("skin_000.png", "skin_001.png"), new ArrayList<>(received.getTexture().keySet()));
        assertArrayEquals(data.getTexture().get("skin_000.png"), received.getTexture().get("skin_000.png"));
    }

    @Test void loadsSpecOneAndMissingOptionalMetadata() throws Exception {
        JsonObject root = fixture();
        root.addProperty("spec", 1);
        root.remove("metadata");
        root.remove("properties");
        ModelData data = read(root);
        assertEquals("test", info(data).getName());
        assertArrayEquals(Files.readAllBytes(temp.resolve("Textures/默认.png")), data.getTexture().get("skin_000.png"));
        assertEquals(0.7, main(data).getMinecraftGeometry()[0].getProperties().getHeightScale(), 0.0001);
    }

    @Test void acceptsWrappedZipAndProducesEquivalentResources() throws Exception {
        ModelData folder = read(fixture());
        Path archive = temp.resolve("wrapped.zip");
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(archive));
             java.util.stream.Stream<Path> walk = Files.walk(temp)) {
            for (Path file : walk.filter(Files::isRegularFile).filter(p -> !p.equals(archive)).collect(java.util.stream.Collectors.toList())) {
                out.putNextEntry(new ZipEntry("包装目录/" + temp.relativize(file).toString().replace('\\', '/')));
                out.write(Files.readAllBytes(file));
                out.closeEntry();
            }
        }
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            assertTrue(ModernFormat.hasManifest(zip));
            ModelData zipped = ModernFormat.readZip(zip, "包装目录/", "test", "test", false);
            assertArrayEquals(folder.getModel().get("main"), zipped.getModel().get("main"));
            assertArrayEquals(folder.getTexture().get("skin_000.png"), zipped.getTexture().get("skin_000.png"));
        }
    }

    @Test void mapsNamedWheelAnimationToLegacySlot() throws Exception {
        JsonObject root = fixture();
        JsonObject extra = new JsonObject();
        extra.addProperty("walk", "走路");
        extra.addProperty("idle", "待机");
        root.getAsJsonObject("properties").add("extra_animation", extra);
        ModelData data = read(root);
        assertArrayEquals(new String[]{"走路", "待机"}, info(data).getExtraAnimationNames());
        JsonObject aliases = YesSteveModel.GSON.fromJson(new String(data.getAnimation().get("modern_extra_aliases"), StandardCharsets.UTF_8), JsonObject.class).getAsJsonObject("animations");
        assertTrue(aliases.has("extra0"));
        assertTrue(aliases.has("extra1"));
    }

    @Test void honorsForcedFreeModelsInAuthFolder() throws Exception {
        JsonObject root = fixture();
        root.getAsJsonObject("properties").addProperty("free", true);
        save(root);
        ModelData data = ModernFormat.readFolder(temp, "test", "test", true);
        assertFalse(data.isAuth());
        assertTrue(info(data).getFree());
    }

    @Test void acceptsGeometry114() throws Exception {
        JsonObject root = fixture();
        Path file = temp.resolve("模型/主体.json");
        String geometry = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).replace("1.12.0", "1.14.0");
        Files.write(file, geometry.getBytes(StandardCharsets.UTF_8));
        RawGeoModel model = main(read(root));
        assertEquals(FormatVersion.VERSION_1_14_0, model.getFormatVersion());
        assertFalse(new GeoBuilder().constructGeoModel(RawGeometryTree.parseHierarchy(model)).topLevelBones.isEmpty());
    }

    @Test void rejectsMissingResourcesAndUnknownSpec() throws Exception {
        JsonObject root = fixture();
        root.addProperty("spec", 3);
        assertThrows(IOException.class, () -> read(root));
        root.addProperty("spec", 2);
        Files.delete(temp.resolve("模型/手臂.json"));
        assertThrows(IOException.class, () -> read(root));
    }

    @Test void rejectsPathsOutsideFolderAndZipRoot() throws Exception {
        JsonObject root = fixture();
        root.getAsJsonObject("files").getAsJsonObject("player").getAsJsonObject("model").addProperty("main", "../outside.json");
        assertThrows(IOException.class, () -> read(root));
        assertFalse(ModernFormat.isManifest("../ysm.json"));
        assertFalse(ModernFormat.isManifest("wrapper/../ysm.json"));
        assertFalse(ModernFormat.isManifest("C:/ysm.json"));
    }

    @Test void acceptsEmptyArmBonesAndRejectsCyclicParents() throws Exception {
        JsonObject root = fixture();
        Path file = temp.resolve("模型/手臂.json");
        JsonObject geometry = YesSteveModel.GSON.fromJson(new String(Files.readAllBytes(file), StandardCharsets.UTF_8), JsonObject.class);
        geometry.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().add("bones", new JsonArray());
        Files.write(file, YesSteveModel.GSON.toJson(geometry).getBytes(StandardCharsets.UTF_8));
        assertNotNull(read(root));
        JsonArray bones = new JsonArray();
        JsonObject bone = new JsonObject();
        bone.addProperty("name", "cycle");
        bone.addProperty("parent", "cycle");
        bones.add(bone);
        geometry.getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject().add("bones", bones);
        Files.write(file, YesSteveModel.GSON.toJson(geometry).getBytes(StandardCharsets.UTF_8));
        assertThrows(IOException.class, () -> read(root));
    }

    @Test void oldBundledModelsStillLoad() throws Exception {
        for (String name : new String[]{"default", "alex", "default_boy", "qingluka", "steve", "wine_fox"}) {
            ModelData data = FolderFormat.getModelData(BUILTIN, name, false);
            assertNotNull(data.getModel().get("arm"));
            assertFalse(new GeoBuilder().constructGeoModel(RawGeometryTree.parseHierarchy(main(data))).topLevelBones.isEmpty());
            assertFalse(data.getTexture().isEmpty());
            for (byte[] texture : data.getTexture().values()) assertNotNull(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(texture)));
        }
    }

    @Test void unicodeIdsAreStableAndValidResourceLocations() {
        String id = ModernFormat.modelId("分类/角色 A");
        assertTrue(ResourceUtil.isValidResourceLocation(id));
        assertEquals(id, ModernFormat.modelId("分类/角色 A"));
        assertNotEquals(id, ModernFormat.modelId("分类/角色 B"));
        assertEquals("model", ModernFormat.modelId("model"));
        String nested = ModernFormat.modelId("group/model");
        assertFalse(nested.contains("/"));
        assertEquals("skin_000.png", com.elfmcys.yesstevemodel.util.ModelIdUtil.getSubNameFromId(
                new net.minecraft.util.ResourceLocation("yes_steve_model", nested + "/skin_000.png")));
    }

    @Test void discoversNestedModelAndPreservesAuthorization() throws Exception {
        save(fixture());
        Path nested = temp.resolve("分类/角色 A");
        Files.createDirectories(nested);
        for (String name : new String[]{"ysm.json", "模型", "Textures"}) Files.move(temp.resolve(name), nested.resolve(name));
        Files.createDirectories(ServerModelManager.CACHE_SERVER);
        EncryptTools.createRandomPassword();
        ServerModelManager.CACHE_NAME_INFO.clear();
        ModernFormat.cacheAllModels(temp);
        assertEquals(1, ServerModelManager.CACHE_NAME_INFO.size());
        assertTrue(ServerModelManager.CACHE_NAME_INFO.containsKey(ModernFormat.modelId("分类/角色 A")));
    }

    @Test void rejectsMissingWheelAnimationInsteadOfPlayingWrongSlot() throws Exception {
        JsonObject root = fixture();
        JsonObject extra = new JsonObject();
        extra.addProperty("unknown_animation", "不存在");
        root.getAsJsonObject("properties").add("extra_animation", extra);
        assertThrows(IOException.class, () -> read(root));
    }
}
