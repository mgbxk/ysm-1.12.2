package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.Converter;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.elfmcys.yesstevemodel.util.YesModelUtils;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import static com.elfmcys.yesstevemodel.model.ServerModelManager.*;
import static com.elfmcys.yesstevemodel.model.format.FormatManager.*;

public final class YsmFormat {
    public static void cacheAllModels(Path rootPath) {
        Collection<File> ysmFiles = FileUtils.listFiles(rootPath.toFile(), new String[]{"ysm"}, false);
        for (File ysmFile : ysmFiles) {
            String modelId = FilenameUtils.removeExtension(ysmFile.getName());
            if (!ResourceUtil.isValidResourceLocation(modelId)) {
                continue;
            }
            loadLegacyModel(rootPath, ysmFile, modelId);
        }
    }

    private static void loadLegacyModel(Path rootPath, File ysmFile, String modelId) {
        try {
            Map<String, byte[]> data = YesModelUtils.input(ysmFile);
            if (data.isEmpty()) {
                return;
            }
            if (!data.containsKey(MAIN_MODEL_FILE_NAME)) {
                return;
            }
            if (!data.containsKey(ARM_MODEL_FILE_NAME)) {
                return;
            }
            if (data.keySet().stream().noneMatch(fileName -> fileName.endsWith(".png"))) {
                return;
            }

            try {
                boolean isAuth = rootPath.equals(AUTH);
                final ModelData modelData = getModelData(data, modelId, isAuth);
                final ServerModelInfo info = cacheModel(modelData);
                CACHE_NAME_INFO.put(modelId, info);
                if (isAuth) AUTH_MODELS.add(modelId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    private static ModelData getModelData(Map<String, byte[]> data, String modelId, boolean isAuth) throws IOException {
        final Map<String, byte[]> model = Maps.newHashMap();
        byte[] infoByte = getBytes(data, INFO_FILE_NAME);
        if (infoByte.length != 0) {
            final String infoJson = new String(infoByte, StandardCharsets.UTF_8);
            final ExtraInfo info = YesSteveModel.GSON.fromJson(infoJson, ExtraInfo.class);
            model.put(INFO_NAME, ObjectStreamUtil.toByteArray(info));
        }

        for (String modelName : MODEL_NAMES) {
            byte[] modelByte = getBytes(data, getModelFileName(modelName));
            if (isModelNameNecessary(modelName) || modelByte.length != 0) {
                final String modelJson = new String(modelByte, StandardCharsets.UTF_8);
                final RawGeoModel rawModel = Converter.fromJsonString(modelJson);
                model.put(modelName, ObjectStreamUtil.toByteArray(rawModel));
            }
        }

        final Map<String, byte[]> texture = Maps.newHashMap();
        data.forEach((name, textureData) -> {
            if (name.endsWith(".png")) {
                texture.put(name, textureData);
            }
        });

        final Map<String, byte[]> animation = Maps.newHashMap();
        for (String animName : ANIMATION_NAMES) {
            byte[] animByte = getBytes(data, getAnimFileName(animName));
            if (animByte.length == 0) {
                animByte = FileUtils.readFileToByteArray(getDefaultAnimFile(animName));
            }
            animation.put(animName, animByte);
        }

        return new ModelData(modelId, isAuth, Type.YSM, model, texture, animation);
    }

    @Nonnull
    private static byte[] getBytes(Map<String, byte[]> data, String fileName) {
        return data.containsKey(fileName) ? data.get(fileName) : new byte[0];
    }
}
