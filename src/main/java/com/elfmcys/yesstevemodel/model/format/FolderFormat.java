package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.Converter;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

import static com.elfmcys.yesstevemodel.model.ServerModelManager.*;
import static com.elfmcys.yesstevemodel.model.format.FormatManager.*;

public final class FolderFormat {
    public static void cacheAllModels(Path rootPath) {
        File[] dirs = rootPath.toFile().listFiles();
        if (dirs == null) {
            return;
        }
        for (File dir : dirs) {
            if (!dir.isDirectory()) {
                continue;
            }
            if (new File(dir, ROOT_FILE_NAME).isFile()) continue;
            String modelId = dir.getName();
            if (!ResourceUtil.isValidResourceLocation(modelId)) {
                continue;
            }
            loadLegacyModel(rootPath, dir, modelId);
        }
    }

    private static void loadLegacyModel(Path rootPath, File dir, String modelId) {
        boolean noMainModelFile = true;
        boolean noArmModelFile = true;
        boolean noTextureFile = true;
        Collection<File> files = FileUtils.listFiles(dir, FileFileFilter.FILE, null);
        for (File file : files) {
            String fileName = file.getName();
            if (MAIN_MODEL_FILE_NAME.equals(fileName) && isNotBlankFile(file)) {
                noMainModelFile = false;
            }
            if (ARM_MODEL_FILE_NAME.equals(fileName) && isNotBlankFile(file)) {
                noArmModelFile = false;
            }
            if (fileName.endsWith(".png")) {
                noTextureFile = false;
            }
        }
        if (noMainModelFile) {
            return;
        }
        if (noArmModelFile) {
            return;
        }
        if (noTextureFile) {
            return;
        }

        try {
            boolean isAuth = rootPath.equals(AUTH);
            final ModelData modelData = getModelData(rootPath, modelId, isAuth);
            final ServerModelInfo info = cacheModel(modelData);
            CACHE_NAME_INFO.put(modelId, info);
            if (isAuth) AUTH_MODELS.add(modelId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    public static ModelData getModelData(Path rootPath, String modelId, boolean isAuth) throws IOException {
        final Path modelPath = rootPath.resolve(modelId);

        final Map<String, byte[]> model = Maps.newHashMap();
        File infoFile = modelPath.resolve(INFO_FILE_NAME).toFile();
        if (infoFile.isFile()) {
            final String infoJson = FileUtils.readFileToString(infoFile, StandardCharsets.UTF_8);
            final ExtraInfo info = YesSteveModel.GSON.fromJson(infoJson, ExtraInfo.class);
            model.put(INFO_NAME, ObjectStreamUtil.toByteArray(info));
        }

        for (String modelName : MODEL_NAMES) {
            File modelFile = modelPath.resolve(getModelFileName(modelName)).toFile();
            if (isModelNameNecessary(modelName) || modelFile.isFile()) {
                final String modelJson = FileUtils.readFileToString(modelFile, StandardCharsets.UTF_8);
                final RawGeoModel rawModel = Converter.fromJsonString(modelJson);
                model.put(modelName, ObjectStreamUtil.toByteArray(rawModel));
            }
        }

        final Map<String, byte[]> texture = Maps.newHashMap();
        FileUtils.listFiles(modelPath.toFile(), new String[]{"png"}, false).forEach(png -> {
            try {
                texture.put(png.getName(), FileUtils.readFileToByteArray(png));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        final Map<String, byte[]> animation = Maps.newHashMap();
        for (String animName : ANIMATION_NAMES) {
            File animFile = modelPath.resolve(getAnimFileName(animName)).toFile();
            if (!animFile.isFile()) {
                animFile = getDefaultAnimFile(animName);
            }
            animation.put(animName, FileUtils.readFileToByteArray(animFile));
        }

        return new ModelData(modelId, isAuth, Type.FOLDER, model, texture, animation);
    }

    private static boolean isNotBlankFile(File file) {
        try {
            String fileText = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            return StringUtils.isNoneBlank(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
