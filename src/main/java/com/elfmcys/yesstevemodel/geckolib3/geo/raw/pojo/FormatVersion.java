package com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 版本
 */
@JsonAdapter(FormatVersion.Serializer.class)
public enum FormatVersion {
    VERSION_1_12_0, VERSION_1_14_0, VERSION_1_8_0;

    public static FormatVersion forValue(String value) throws IOException {
        return switch (value) {
            case "1.12.0" -> VERSION_1_12_0;
            case "1.14.0" -> VERSION_1_14_0;
            case "1.8.0" -> VERSION_1_8_0;
            default -> throw new IOException("Cannot deserialize FormatVersion: " + value);
        };
    }

    public String toValue() {
        return switch (this) {
            case VERSION_1_12_0 -> "1.12.0";
            case VERSION_1_14_0 -> "1.14.0";
            case VERSION_1_8_0 -> "1.8.0";
        };
    }

    protected static class Serializer implements JsonSerializer<FormatVersion>, JsonDeserializer<FormatVersion> {
        @Override
        public FormatVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return FormatVersion.forValue(json.getAsString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public JsonElement serialize(FormatVersion src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toValue());
        }
    }
}
