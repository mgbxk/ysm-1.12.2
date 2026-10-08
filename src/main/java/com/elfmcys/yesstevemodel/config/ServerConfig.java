package com.elfmcys.yesstevemodel.config;

import com.elfmcys.yesstevemodel.config.util.ConfigBuilder;

import javax.annotation.Nonnull;

public class ServerConfig {
    public static String DEFAULT_MODEL_ID = "default";
    public static String DEFAULT_MODEL_TEXTURE = "default.png";

    public static void build(@Nonnull ConfigBuilder builder) {
        DEFAULT_MODEL_ID = builder.get(
                "DefaultModelId",
                DEFAULT_MODEL_ID,
                "The default model ID when a player first enters the game"
        );

        DEFAULT_MODEL_TEXTURE = builder.get(
                "DefaultModelTexture",
                DEFAULT_MODEL_TEXTURE,
                "The default model texture when a player first enters the game"
        );
    }
}
