package com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MinecraftGeometry implements Serializable {
    @SerializedName("bones")
    private Bone[] bones;
    @SerializedName("cape")
    private String cape;
    @SerializedName("description")
    private ModelProperties modelProperties;

    public Bone[] getBones() {
        return this.bones;
    }

    public void setBones(Bone[] value) {
        this.bones = value;
    }

    public String getCape() {
        return this.cape;
    }

    public void setCape(String value) {
        this.cape = value;
    }

    public ModelProperties getProperties() {
        return this.modelProperties;
    }

    public void setProperties(ModelProperties value) {
        this.modelProperties = value;
    }
}
