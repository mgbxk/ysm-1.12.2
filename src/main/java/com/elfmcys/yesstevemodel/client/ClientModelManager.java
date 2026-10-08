package com.elfmcys.yesstevemodel.client;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.api.IArrowExtraInfo;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.client.animation.modern.ModernAssets;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.FormatVersion;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.tree.RawGeometryTree;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.GeoBuilder;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.json.JsonAnimationUtils;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.model.format.FolderFormat;
import com.elfmcys.yesstevemodel.network.message.SyncModelFiles;
import com.elfmcys.yesstevemodel.util.ModelIdUtil;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.JsonException;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ClientModelManager {
    public static volatile long generation;
    private static final java.util.Set<ResourceLocation> LOADED_TEXTURES = new java.util.HashSet<>();
    public static Map<ResourceLocation, List<ResourceLocation>> MODELS = Maps.newHashMap();
    public static Map<ResourceLocation, Pair<Double, Double>> SCALE_INFO = Maps.newHashMap();
    public static Map<ResourceLocation, List<String>> METADATA = Maps.newHashMap();
    public static Map<ResourceLocation, ExtraInfo> EXTRA_INFO = Maps.newHashMap();
    public static AnimationFile DEFAULT_ANIMATION_FILE = new AnimationFile();
    public static List<String> CACHE_MD5 = Lists.newArrayList();
    public static List<String> AUTH_MODELS = Lists.newArrayList();
    public static byte[] PASSWORD;

    public static void registerAll(ModelData data) {
        ResourceLocation modelId = new ResourceLocation(YesSteveModel.MOD_ID, data.getModelId());
        if (data.isAuth()) {
            AUTH_MODELS.add(data.getModelId());
        }
        ModernAssets.register(modelId, data.getModel());
        ClientModelManager.registerGeo(modelId, data.getModel());
        ClientModelManager.registerAnimations(ModelIdUtil.getMainId(modelId), data.getAnimation());
        ClientModelManager.registerTexture(modelId, data.getTexture(), data.getModel());
    }

    public static void registerGeo(ResourceLocation modelId, Map<String, byte[]> modelData) {
        for (Map.Entry<String, byte[]> entry : modelData.entrySet()) {
            String partName = entry.getKey();
            if ("info".equals(partName) || partName.startsWith("modern_")) continue;
            registerGeo(modelId, partName, entry.getValue());
        }
        byte[] infoData = modelData.get("info");
        if (infoData != null && ObjectStreamUtil.toObject(infoData) instanceof ExtraInfo extraInfo) {
            addExtraInfo(modelId, extraInfo);
        }
        if (!EXTRA_INFO.containsKey(ModelIdUtil.getInfoId(modelId))) addExtraInfo(modelId, new ExtraInfo());
    }

    private static void registerGeo(ResourceLocation modelId, String partName, byte[] partData) {
        Map<ResourceLocation, GeoModel> geoModels = GeckoLibCache.getInstance().getGeoModels();
        try {
            Object obj = ObjectStreamUtil.toObject(partData);
            if (obj instanceof RawGeoModel rawModel) {
                if (rawModel.getFormatVersion() == FormatVersion.VERSION_1_12_0 || rawModel.getFormatVersion() == FormatVersion.VERSION_1_14_0) {
                    RawGeometryTree rawGeometryTree = RawGeometryTree.parseHierarchy(rawModel);
                    ResourceLocation partId = ModelIdUtil.getSubModelId(modelId, partName);
                    GeoModel geoModel = GeoBuilder.getGeoBuilder(partId.getNamespace()).constructGeoModel(rawGeometryTree);
                    if ("main".equals(partName)) {
                        SCALE_INFO.put(partId, Pair.of(rawGeometryTree.properties.getHeightScale(), rawGeometryTree.properties.getWidthScale()));
                        addExtraInfo(modelId, rawGeometryTree.properties.getExtraInfo());
                    }
                    geoModels.put(partId, geoModel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void registerTexture(ResourceLocation modelId, Map<String, byte[]> mapData) {
        registerTexture(modelId, mapData, java.util.Collections.emptyMap());
    }

    private static void registerTexture(ResourceLocation modelId, Map<String, byte[]> mapData, Map<String, byte[]> resources) {
        List<ResourceLocation> textures = Lists.newArrayList();
        for (String name : mapData.keySet()) {
            if (isModelTexture(ModelIdUtil.getInfoId(modelId), name)) {
                ResourceLocation textureId = ModelIdUtil.getSubModelId(modelId, name);
                textures.add(textureId);
            }
        }
        MODELS.put(modelId, textures);
        for (Map.Entry<String, byte[]> entry : mapData.entrySet()) {
            ResourceLocation textureId = ModelIdUtil.getSubModelId(modelId, entry.getKey());
            registerTexture(textureId, entry.getValue(), resources.get("modern_pbr_" + entry.getKey() + ".normal"), resources.get("modern_pbr_" + entry.getKey() + ".specular"));
        }
    }

    private static boolean isModelTexture(ResourceLocation infoId, String name) {
        if (name.equals(IArrowExtraInfo.TEXTURE_NAME)) return false;
        final ExtraInfo extraInfo = EXTRA_INFO.get(infoId);
        return extraInfo == null || (!name.equals(extraInfo.getGuiBackground()) && !name.equals(extraInfo.getGuiForeground()));
    }

    private static void registerTexture(ResourceLocation textureId, byte[] data, byte[] normal, byte[] specular) {
        // 确保主线程上传
        final Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            mc.getTextureManager().loadTexture(textureId, new OuterFileTexture(data, normal, specular));
            LOADED_TEXTURES.add(textureId);
        });
    }

    static void registerAnimations(ResourceLocation mainId, Map<String, byte[]> mapData) {
        Map<ResourceLocation, AnimationFile> animations = GeckoLibCache.getInstance().getAnimations();

        if (mapData.containsKey("arrow")) {
            byte[] arrowBytes = mapData.get("arrow");
            AnimationFile arrowsAnimationFile = getAnimationFile(new String(arrowBytes, StandardCharsets.UTF_8));
            animations.put(ModelIdUtil.getArrowId(ModelIdUtil.getModelIdFromMainId(mainId)), arrowsAnimationFile);
        }

        ModernAssets.Bundle bundle = ModernAssets.MODELS.get(mainId);
        boolean modernController = bundle != null && !bundle.controllers.isEmpty();
        AnimationFile main = new AnimationFile();
        mapData.forEach((name, bytes) -> {
            if (name.equals("arrow") || modernController && name.equals("main")) return;
            AnimationFile other = getAnimationFile(new String(bytes, StandardCharsets.UTF_8));
            mergeAnimationFile(main, other);
        });
        // Part-specific files can contain empty same-name placeholders (e.g. fp_arm's
        // parallel0..7). The player's primary clips must win for the body renderer.
        if (modernController && mapData.containsKey("main")) {
            mergeAnimationFile(main, getAnimationFile(new String(mapData.get("main"), StandardCharsets.UTF_8)));
        }
        // 从默认补全缺失的动画
        DEFAULT_ANIMATION_FILE.animations().forEach((name, action) -> {
            if (!main.animations().containsKey(name)) {
                main.putAnimation(name, action);
            }
        });
        main.animations().forEach((name, animation) -> ConditionManager.addTest(mainId, name));
        animations.put(mainId, main);
    }

    private static AnimationFile getAnimationFile(String file) {
        AnimationFile animationFile = new AnimationFile();
        MolangParser parser = GeckoLibCache.getInstance().parser;
        JsonObject jsonObject = JsonUtils.fromJson(YesSteveModel.GSON, file, JsonObject.class, false);
        if (jsonObject != null) {
            for (Map.Entry<String, JsonElement> entry : JsonAnimationUtils.getAnimations(jsonObject)) {
                String animationName = entry.getKey();
                Animation animation;
                try {
                    animation = JsonAnimationUtils.deserializeJsonToAnimation(JsonAnimationUtils.getAnimation(jsonObject, animationName), parser);
                    animationFile.putAnimation(animationName, animation);
                } catch (JsonException e) {
                    e.printStackTrace();
                }
            }
        }
        return animationFile;
    }

    /**
     * @return main animation
     */
    private static AnimationFile mergeAnimationFile(AnimationFile main, AnimationFile other) {
        other.animations().forEach(main::putAnimation);
        return main;
    }

    public static void loadDefaultModel() {
        try {
            ModelData data = FolderFormat.getModelData(ServerModelManager.BUILTIN, "default", false);
            data.getAnimation().forEach((name, bytes) -> {
                AnimationFile animationFile = getAnimationFile(new String(bytes, StandardCharsets.UTF_8));
                if (!"arrow".equals(name)) mergeAnimationFile(DEFAULT_ANIMATION_FILE, animationFile);
            });
            ClientModelManager.registerAll(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void clearModels() {
        generation++;
        PASSWORD = null;
        LOADED_TEXTURES.forEach(id -> Minecraft.getMinecraft().getTextureManager().deleteTexture(id));
        LOADED_TEXTURES.clear();
        MODELS.clear();
        CACHE_MD5.clear();
        AUTH_MODELS.clear();
        SCALE_INFO.clear();
        METADATA.clear();
        EXTRA_INFO.clear();
        ModernAssets.clear();
        ConditionManager.clear();
        GeckoLibCache.getInstance().getGeoModels().keySet().removeIf(id -> id.getNamespace().equals(YesSteveModel.MOD_ID));
        GeckoLibCache.getInstance().getAnimations().keySet().removeIf(id -> id.getNamespace().equals(YesSteveModel.MOD_ID));
        com.elfmcys.yesstevemodel.util.AnimatableCacheUtil.ANIMATABLE_CACHE.invalidateAll();
        com.elfmcys.yesstevemodel.util.AnimatableCacheUtil.TEXTURE_GUI_CACHE.invalidateAll();
        com.elfmcys.yesstevemodel.util.AnimatableCacheUtil.ENTITIES_CACHE.invalidateAll();
    }

    public static void sendSyncModelMessage() {
        if (!ClientSession.isSynced() || Minecraft.getMinecraft().getConnection() == null) return;
        clearModels();
        loadDefaultModel();
        String[] md5Info = getMd5Info();
        SyncModelFiles syncModelFiles = new SyncModelFiles(md5Info);
        ClientActions.sendToServer(syncModelFiles);
    }

    private static String[] getMd5Info() {
        Collection<File> files = FileUtils.listFiles(ServerModelManager.CACHE_CLIENT.toFile(), FileFileFilter.FILE, null);
        String[] output = new String[files.size()];
        int i = 0;
        for (File file : files) {
            output[i] = file.getName();
            i++;
        }
        return output;
    }

    private static byte[] getBytes(Path root, String fileName) throws IOException {
        return FileUtils.readFileToByteArray(root.resolve(fileName).toFile());
    }

    private static void addExtraInfo(ResourceLocation modelId, @Nullable ExtraInfo extraInfo) {
        if (extraInfo == null) return;
        ResourceLocation infoId = ModelIdUtil.getInfoId(modelId);
        EXTRA_INFO.put(infoId, extraInfo);
        METADATA.put(infoId, readMetaData(extraInfo));
        if (extraInfo.getFree()) {
            AUTH_MODELS.remove(modelId.getPath());
        }
        if (modelId.getPath().equals("default") && extraInfo.getPreviewAnimation() != null) {
            DEFAULT_ANIMATION_FILE.animations().remove(extraInfo.getPreviewAnimation());
        }
    }

    @Nullable
    private static List<String> readMetaData(@Nullable ExtraInfo extraInfo) {
        if (extraInfo == null || StringUtils.isBlank(extraInfo.getName())) {
            return null;
        }
        List<String> component = Lists.newArrayList();
        component.add(TextFormatting.GOLD + extraInfo.getName());
        if (extraInfo.getTips() != null && StringUtils.isNoneBlank(extraInfo.getTips())) {
            String[] split = extraInfo.getTips().split("\n");
            Arrays.stream(split).forEach(s -> component.add(TextFormatting.GRAY + I18n.format(s)));
        }
        if (extraInfo.getAuthors() != null && extraInfo.getAuthors().length != 0) {
            component.add(I18n.format("gui.yes_steve_model.model.authors", TextFormatting.RESET + StringUtils.join(extraInfo.getAuthors(), "丨")));
        }
        if (StringUtils.isNoneBlank(extraInfo.getLicense())) {
            component.add(I18n.format("gui.yes_steve_model.model.license", TextFormatting.RESET + extraInfo.getLicense()));
        }
        return component;
    }
}
