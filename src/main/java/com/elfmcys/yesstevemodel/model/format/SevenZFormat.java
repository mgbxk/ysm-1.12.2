//package com.elfmcys.yesstevemodel.model.format;
//
//import com.elfmcys.yesstevemodel.data.ModelData;
//import com.elfmcys.yesstevemodel.util.ResourceUtil;
//import com.google.common.collect.Maps;
//import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
//import org.apache.commons.compress.archivers.sevenz.SevenZFile;
//import org.apache.commons.io.FileUtils;
//import org.apache.commons.io.FilenameUtils;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.Collection;
//import java.util.Map;
//
//import static com.elfmcys.yesstevemodel.model.ServerModelManager.*;
//import static com.elfmcys.yesstevemodel.model.format.FormatManager.*;
//
//public final class SevenZFormat {
//    public static void cacheAllModels(Path rootPath) {
//        Collection<File> sevenZFiles = FileUtils.listFiles(rootPath.toFile(), new String[]{"7z"}, false);
//        for (File file : sevenZFiles) {
//            String modelId = FilenameUtils.removeExtension(file.getName());
//            if (!ResourceUtil.isValidResourceLocation(modelId)) {
//                continue;
//            }
//            loadLegacyModel(rootPath, file, modelId);
//        }
//    }
//
//    private static void loadLegacyModel(Path rootPath, File file, String modelId) {
//        Map<String, byte[]> data = Maps.newHashMap();
//        try (SevenZFile sevenZFile = new SevenZFile(file)) {
//            SevenZArchiveEntry entry;
//            while ((entry = sevenZFile.getNextEntry()) != null) {
//                if (entry.isDirectory()) continue;
//                int size = (int) entry.getSize();
//                byte[] content = new byte[size];
//                int bytesRead = 0;
//                while (bytesRead < size) {
//                    int result = sevenZFile.read(content, bytesRead, size - bytesRead);
//                    if (result == -1) break;
//                    bytesRead += result;
//                }
//                data.put(entry.getName(), content);
//            }
//            if (data.isEmpty()) {
//                return;
//            }
//            if (!data.containsKey(MAIN_MODEL_FILE_NAME)) {
//                return;
//            }
//            if (!data.containsKey(ARM_MODEL_FILE_NAME)) {
//                return;
//            }
//            if (data.keySet().stream().noneMatch(fileName -> fileName.endsWith(".png"))) {
//                return;
//            }
//
//            try {
//                boolean isAuth = rootPath.equals(AUTH);
//                final ModelData modelData = YsmFormat.getModelData(data, modelId, isAuth, Type.SEVEN_Z);
//                final ServerModelInfo info = cacheModel(modelData);
//                CACHE_NAME_INFO.put(modelId, info);
//                if (isAuth) AUTH_MODELS.add(modelId);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
