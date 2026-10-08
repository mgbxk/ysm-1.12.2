// To use this code, add the following Maven dependency to your project:
//
//
//     com.fasterxml.jackson.core     : jackson-databind          : 2.9.0
//     com.fasterxml.jackson.datatype : jackson-datatype-jsr310   : 2.9.0
//
// Import this package:
//
//     import software.bernie.geckolib.file.geo.Converter;
//
// Then you can deserialize a JSON string with
//
//     GeoModel data = Converter.fromJsonString(jsonString);

package com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo;

import com.elfmcys.yesstevemodel.YesSteveModel;

public class Converter {
    public static RawGeoModel fromJsonString(String json) {
        return YesSteveModel.GSON.fromJson(json, RawGeoModel.class);
    }
}
