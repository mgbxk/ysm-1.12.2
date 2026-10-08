package com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class TextureMesh implements Serializable {
    @SerializedName("local_pivot")
    private double[] localPivot;
    @SerializedName("position")
    private double[] position;
    @SerializedName("rotation")
    private double[] rotation;
    @SerializedName("scale")
    private double[] scale;
    @SerializedName("texture")
    private String texture;

    public double[] getLocalPivot() {
        return this.localPivot;
    }

    public void setLocalPivot(double[] value) {
        this.localPivot = value;
    }

    public double[] getPosition() {
        return this.position;
    }

    public void setPosition(double[] value) {
        this.position = value;
    }

    public double[] getRotation() {
        return this.rotation;
    }

    public void setRotation(double[] value) {
        this.rotation = value;
    }

    public double[] getScale() {
        return this.scale;
    }

    public void setScale(double[] value) {
        this.scale = value;
    }

    public String getTexture() {
        return this.texture;
    }

    public void setTexture(String value) {
        this.texture = value;
    }
}
