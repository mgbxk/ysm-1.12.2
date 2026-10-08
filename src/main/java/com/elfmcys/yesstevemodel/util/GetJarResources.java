package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.YesSteveModel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.stream.Stream;

public final class GetJarResources {
//    /**
//     * 复制本模组的文件到指定目录
//     *
//     * @param filePath jar 里面的文件地址
//     * @param destPath 想要复制到的目录
//     * @param fileName 复制后的文件名
//     */
//    public static void copyFile(String filePath, Path destPath, String fileName) {
//        URL url = YesSteveModel.class.getResource(filePath);
//        if (url == null) {
//            return;
//        }
//        try {
//            FileUtils.copyURLToFile(url, destPath.resolve(fileName).toFile());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 复制本模组的文件夹到指定目录
     *
     * @param folderPath jar 里面的文件地址
     * @param destPath   想要复制到的目录
     * @param folderName 复制后的文件夹名
     */
    public static void copyFolder(String folderPath, Path destPath, String folderName) {
        URL url = YesSteveModel.class.getResource(folderPath);
        if (url == null) {
            return;
        }
        try {
            URI uri = url.toURI();
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                Path jarFolderPath = fs.getPath(folderPath);
                Path targetRoot = destPath.resolve(folderName);
                try (Stream<Path> walk = Files.walk(jarFolderPath)) {
                    walk.forEach(source -> {
                        try {
                            Path destFolder = targetRoot.resolve(jarFolderPath.relativize(source).toString());
                            if (Files.isDirectory(source)) {
                                Files.createDirectories(destFolder);
                            } else {
                                Files.createDirectories(destFolder.getParent());
                                Files.copy(source, destFolder, StandardCopyOption.REPLACE_EXISTING);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}
