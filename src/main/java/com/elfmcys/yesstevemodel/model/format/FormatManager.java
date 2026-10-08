package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.data.EncryptTools;
import com.elfmcys.yesstevemodel.data.ModelData;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.util.Md5Utils;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class FormatManager {
    // name 不带后缀名，file name 带后缀名
    // HashSet 无序
    public static final Set<String> MODEL_NAMES = new HashSet<>();
    public static final Set<String> ANIMATION_NAMES = new HashSet<>();

    static {
        MODEL_NAMES.add("main");
        MODEL_NAMES.add("arm");
        MODEL_NAMES.add("arrow");

        ANIMATION_NAMES.add("main");
        ANIMATION_NAMES.add("arm");
        ANIMATION_NAMES.add("extra");
        ANIMATION_NAMES.add("tac");
        ANIMATION_NAMES.add("carryon");
        ANIMATION_NAMES.add("arrow");
    }

    public static final String INFO_NAME = "info";
    public static final String INFO_FILE_NAME = "info.json";
    public static final String MAIN_MODEL_FILE_NAME = "main.json";
    public static final String ARM_MODEL_FILE_NAME = "arm.json";

    public static final String ROOT_FILE_NAME = "ysm.json";

    public static boolean isModelNameNecessary(String modelName) {
        return modelName.equals("main") || modelName.equals("arm");
    }

    public static String getModelFileName(String modelName) {
        return modelName + ".json";
    }

    public static String getAnimFileName(String animName) {
        return animName + ".animation.json";
    }

    public static String getDefaultAnimFileName(String animName) {
        return "default/" + getAnimFileName(animName);
    }

    public static File getDefaultAnimFile(String animName) {
        return ServerModelManager.BUILTIN.resolve(FormatManager.getDefaultAnimFileName(animName)).toFile();
    }

    /**
     * 使用 {@link EncryptTools#assembleEncryptModels(ModelData)} 加密，并以 MD5 值命名存入服务端缓存目录。
     *
     * @param modelData 封装好的序列化模型文件二进制流
     */
    @Nonnull
    public static ServerModelInfo cacheModel(ModelData modelData) throws IOException {
        byte[] dataBytes = EncryptTools.assembleEncryptModels(modelData);
        modelData.setMd5(Md5Utils.md5Hex(dataBytes).toUpperCase(Locale.US));
        FileUtils.writeByteArrayToFile(ServerModelManager.CACHE_SERVER.resolve(modelData.getInfo().getMd5()).toFile(), dataBytes);
        return modelData.getInfo();
    }
}
