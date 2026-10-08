package com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UvFaces implements Serializable {
    @SerializedName("down")
    private FaceUv down;
    @SerializedName("east")
    private FaceUv east;
    @SerializedName("north")
    private FaceUv north;
    @SerializedName("south")
    private FaceUv south;
    @SerializedName("up")
    private FaceUv up;
    @SerializedName("west")
    private FaceUv west;

    public FaceUv getDown() {
        return this.down;
    }

    public void setDown(FaceUv value) {
        this.down = value;
    }

    public FaceUv getEast() {
        return this.east;
    }

    public void setEast(FaceUv value) {
        this.east = value;
    }

    public FaceUv getNorth() {
        return this.north;
    }

    public void setNorth(FaceUv value) {
        this.north = value;
    }

    public FaceUv getSouth() {
        return this.south;
    }

    public void setSouth(FaceUv value) {
        this.south = value;
    }

    public FaceUv getUp() {
        return this.up;
    }

    public void setUp(FaceUv value) {
        this.up = value;
    }

    public FaceUv getWest() {
        return this.west;
    }

    public void setWest(FaceUv value) {
        this.west = value;
    }
}
