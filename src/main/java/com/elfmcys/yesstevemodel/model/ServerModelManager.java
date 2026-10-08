package com.elfmcys.yesstevemodel.model;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.model.format.FolderFormat;
import com.elfmcys.yesstevemodel.model.format.ModernFormat;
import com.elfmcys.yesstevemodel.model.format.ServerModelInfo;
import com.elfmcys.yesstevemodel.model.format.YsmFormat;
import com.elfmcys.yesstevemodel.model.format.ZipFormat;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.RequestSyncModel;
import com.elfmcys.yesstevemodel.util.GetJarResources;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public final class ServerModelManager {
    /**
     * 配置相关文件夹
     */
    public static final Path FOLDER = Paths.get("config", YesSteveModel.MOD_ID);

    /**
     * 内置模型输出的文件夹
     */
    public static final Path BUILTIN = FOLDER.resolve("builtin");
    /**
     * 自定义模型所放置的文件夹
     */
    public static final Path CUSTOM = FOLDER.resolve("custom");
    public static final Path AUTH = FOLDER.resolve("auth");
    public static final Path EXPORT = FOLDER.resolve("export");

    /**
     * 生成缓存文件的文件夹
     */
    public static final Path CACHE = FOLDER.resolve("cache");
    public static final Path CACHE_SERVER = CACHE.resolve("server");
    /**
     * 存储密码的文件
     */
    public static final Path PASSWORD_FILE = CACHE_SERVER.resolve("PASSWORD");
    public static final Path CACHE_CLIENT = CACHE.resolve("client");
    /**
     * 模型名称 -> 模型额外信息缓存
     * 可以方便的通过此缓存，来判断客户端发来的 MD5 在不在服务端
     * 从而将服务器文件发送给玩家
     * 还可以获取其他服务端模型信息
     */
    public static final Map<String, ServerModelInfo> CACHE_NAME_INFO = Maps.newHashMap();

    /**
     * 放置授权模型名称
     */
    public static final Set<String> AUTH_MODELS = Sets.newHashSet();

    public static void sendRequestSyncModelMessage(PlayerList playerList) {
        for (EntityPlayerMP player : playerList.getPlayers()) {
            NetworkHandler.sendToClientPlayer(new RequestSyncModel(), player);
        }
    }

    public static void sendRequestSyncModelMessage() {
        ClientModelManager.sendSyncModelMessage();
    }

    public static void sendRequestSyncModelMessage(EntityPlayer player) {
        NetworkHandler.sendToClientPlayer(new RequestSyncModel(), player);
    }

    public static void reloadPacks() {
        CACHE_NAME_INFO.clear();
        com.elfmcys.yesstevemodel.model.modern.ModernModelOptions.SERVER_MODELS.clear();
        AUTH_MODELS.clear();

        createFolder(FOLDER);
        createFolder(CUSTOM);
        createFolder(AUTH);
        createFolder(EXPORT);

        createFolder(CACHE);
        createFolder(CACHE_SERVER);
        createFolder(CACHE_CLIENT);

        deleteFolder(BUILTIN);
        GetJarResources.copyFolder(getCustomFiles("builtin"), BUILTIN.getParent(), "builtin");
        initPassword();
        cacheAllModels(BUILTIN);
        cacheAllModels(CUSTOM);
        cacheAllModels(AUTH);
    }

    private static void cacheAllModels(Path rootPath) {
        YsmFormat.cacheAllModels(rootPath);
//        SevenZFormat.cacheAllModels(rootPath);
        ZipFormat.cacheAllModels(rootPath);
        FolderFormat.cacheAllModels(rootPath);
        ModernFormat.cacheAllModels(rootPath);
    }

    private static void initPassword() {
        try {
            EncryptTools.createRandomPassword();
            File passwordFile = PASSWORD_FILE.toFile();
            if (passwordFile.isFile()) {
                EncryptTools.readPassword(FileUtils.readFileToByteArray(passwordFile));
            } else {
                FileUtils.writeByteArrayToFile(passwordFile, EncryptTools.writePassword());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getCustomFiles(String path) {
        return String.format("/assets/%s/%s", YesSteveModel.MOD_ID, path);
    }

    private static void createFolder(Path path) {
        File folder = path.toFile();
        if (!folder.isDirectory()) {
            try {
                Files.createDirectories(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void deleteFolder(Path path) {
        try {
            FileUtils.deleteDirectory(path.toFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
