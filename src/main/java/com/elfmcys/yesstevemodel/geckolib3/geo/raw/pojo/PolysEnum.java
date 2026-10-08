package com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo;

import java.io.IOException;

public enum PolysEnum {
    QUAD_LIST, TRI_LIST;

    public static PolysEnum forValue(String value) throws IOException {
        return switch (value) {
            case "quad_list" -> QUAD_LIST;
            case "tri_list" -> TRI_LIST;
            default -> throw new IOException("Cannot deserialize PolysEnum");
        };
    }

    public String toValue() {
        return switch (this) {
            case QUAD_LIST -> "quad_list";
            case TRI_LIST -> "tri_list";
        };
    }
}
