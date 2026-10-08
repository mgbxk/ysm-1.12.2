package com.elfmcys.yesstevemodel.geckolib3.resource;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.MolangParser;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public class GeckoLibCache {
    private static GeckoLibCache INSTANCE;
    public final MolangParser parser = new MolangParser();
    private final Map<ResourceLocation, AnimationFile> animations = Maps.newHashMap();
    private final Map<ResourceLocation, GeoModel> geoModels = Maps.newHashMap();

    public static GeckoLibCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GeckoLibCache();
            return INSTANCE;
        }
        return INSTANCE;
    }

    public Map<ResourceLocation, AnimationFile> getAnimations() {
        return this.animations;
    }

    public Map<ResourceLocation, GeoModel> getGeoModels() {
        return this.geoModels;
    }
}
