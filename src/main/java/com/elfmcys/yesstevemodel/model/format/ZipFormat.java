package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.Converter;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ExtraInfo;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.RawGeoModel;
import com.elfmcys.yesstevemodel.util.InputStreamUtils;
import com.elfmcys.yesstevemodel.util.ObjectStreamUtil;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.elfmcys.yesstevemodel.model.ServerModelManager.*;
import static com.elfmcys.yesstevemodel.model.format.FormatManager.*;

public final class ZipFormat {
    public static void cacheAllModels(Path rootPath) {
        Collection<File> zipFiles = FileUtils.listFiles(rootPath.toFile(), new String[]{"zip"}, false);
        for (File file : zipFiles) {
            String modelId = FilenameUtils.removeExtension(file.getName());
            if (!ResourceUtil.isValidResourceLocation(modelId)) {
                continue;
            }
            loadLegacyModel(rootPath, file, modelId);
        }
    }

    private static void loadLegacyModel(Path rootPath, File file, String modelId) {
        try (ZipFile zipFile = new ZipFile(file)) {
            if (ModernFormat.hasManifest(zipFile)) return;
            if (zipFile.getEntry(MAIN_MODEL_FILE_NAME) == null || isBlankEntry(zipFile, MAIN_MODEL_FILE_NAME)) {
                return;
            }
            if (zipFile.getEntry(ARM_MODEL_FILE_NAME) == null || isBlankEntry(zipFile, ARM_MODEL_FILE_NAME)) {
                return;
            }
            if (zipFile.stream().noneMatch(entry -> entry.getName().endsWith(".png"))) {
                return;
            }

            try {
                boolean isAuth = rootPath.equals(AUTH);
                final ModelData modelData = getModelData(zipFile, modelId, isAuth);
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
    private static ModelData getModelData(ZipFile zipFile, String modelId, boolean isAuth) throws IOException {
        final Map<String, byte[]> model = Maps.newHashMap();
        byte[] infoByte = getBytes(zipFile, INFO_FILE_NAME);
        if (infoByte.length != 0) {
            final String infoJson = new String(infoByte, StandardCharsets.UTF_8);
            final ExtraInfo info = YesSteveModel.GSON.fromJson(infoJson, ExtraInfo.class);
            model.put(INFO_NAME, ObjectStreamUtil.toByteArray(info));
        }

        for (String modelName : MODEL_NAMES) {
            byte[] modelByte = getBytes(zipFile, getModelFileName(modelName));
            if (isModelNameNecessary(modelName) || modelByte.length != 0) {
                final String modelJson = new String(modelByte, StandardCharsets.UTF_8);
                final RawGeoModel rawModel = Converter.fromJsonString(modelJson);
                model.put(modelName, ObjectStreamUtil.toByteArray(rawModel));
            }
        }

        final Map<String, byte[]> texture = Maps.newHashMap();
        zipFile.stream().forEach(zipEntry -> {
            if (zipEntry.getName().endsWith(".png")) {
                try {
                    texture.put(zipEntry.getName(), getBytes(zipFile, zipEntry.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        final Map<String, byte[]> animation = Maps.newHashMap();
        for (String animName : ANIMATION_NAMES) {
            byte[] animByte = getBytes(zipFile, getAnimFileName(animName));
            if (animByte.length == 0) {
                animByte = FileUtils.readFileToByteArray(getDefaultAnimFile(animName));
            }
            animation.put(animName, animByte);
        }

        return new ModelData(modelId, isAuth, Type.ZIP, model, texture, animation);
    }

    @Nonnull
    private static byte[] getBytes(ZipFile zipFile, String fileName) throws IOException {
        ZipEntry fileEntry = zipFile.getEntry(fileName);
        if (fileEntry == null) return new byte[0];
        try (InputStream stream = zipFile.getInputStream(fileEntry)) {
            return InputStreamUtils.toBytes(stream);
        }
    }

    private static boolean isBlankEntry(ZipFile zipFile, String fileName) {
        ZipEntry entry = zipFile.getEntry(fileName);
        try (InputStream stream = zipFile.getInputStream(entry)) {
            String fileText = IOUtils.toString(stream, StandardCharsets.UTF_8);
            return StringUtils.isBlank(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
