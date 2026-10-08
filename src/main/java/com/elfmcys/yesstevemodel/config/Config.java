package com.elfmcys.yesstevemodel.config;

import com.elfmcys.yesstevemodel.client.config.ExtraPlayerScreenConfig;
import com.elfmcys.yesstevemodel.client.config.GeneralConfig;
import com.elfmcys.yesstevemodel.config.util.ConfigBuilder;
import com.elfmcys.yesstevemodel.config.util.IFormatter;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public final class Config {
    private static Configuration config;

    public static void init(File configFile, Side side) {
        if (config != null) throw new IllegalStateException("Init have been performed!");
        config = new Configuration(configFile);
        readFromFile(side);
    }

    public static void readFromFile(Side side) {
        build(ConfigBuilder.startReadingFromFile(config), side);
    }

    public static void readFromProp() {
        build(ConfigBuilder.startReadingFromProp(config), Side.CLIENT);
    }

    public static void save() {
        build(ConfigBuilder.startSaving(config), Side.CLIENT);
    }

    private static void build(@Nonnull ConfigBuilder builder, Side side) {
        builder.setLangKeyPrefix("config.yes_steve_model");
        builder.setLangKeyFormatter(IFormatter.CAMEL_TO_SNAKE);

        if (side.isClient()) {
            builder.pushCategory("client");
            GeneralConfig.build(builder);
            ExtraPlayerScreenConfig.build(builder);
            builder.popCategory();
        }

        builder.pushCategory("server");
        ServerConfig.build(builder);
        builder.popCategory();

        builder.finishBuilding();
    }

    public static Configuration getConfig() {
        return config;
    }

    @Nonnull
    public static List<IConfigElement> getRootConfigElements() {
        return config.getCategoryNames().stream()
                .map(config::getCategory)
                .filter(category -> !category.isChild())
                .map(ConfigElement::new)
                .collect(Collectors.toList());
    }
}
