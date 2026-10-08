package com.elfmcys.yesstevemodel.geckolib3.geo.render;

import com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo.ModelProperties;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.tree.RawBoneGroup;
import com.elfmcys.yesstevemodel.geckolib3.geo.raw.tree.RawGeometryTree;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;

public interface IGeoBuilder {
    GeoModel constructGeoModel(RawGeometryTree geometryTree);

    GeoBone constructBone(RawBoneGroup bone, ModelProperties properties, GeoBone parent);
}
