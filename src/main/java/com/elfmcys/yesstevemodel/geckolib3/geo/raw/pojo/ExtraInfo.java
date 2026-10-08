package com.elfmcys.yesstevemodel.geckolib3.geo.raw.pojo;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.Serializable;

/// {@link ModelProperties#extraInfo}
@SuppressWarnings({"unused", "JavadocReference"})
public class ExtraInfo implements Serializable {
    @SerializedName("name")
    private String name;
    @SerializedName("tips")
    private String tips = StringUtils.EMPTY;
    @SerializedName("extra_animation_names")
    private String[] extraAnimationNames = null;
    @SerializedName("authors")
    private String[] authors = null;
    @SerializedName("license")
    private String license = "All Rights Reserved";
    @SerializedName("free")
    private boolean free = false;

    @SerializedName("preview_animation")
    private String previewAnimation = "idle";
    @SerializedName("disable_preview_rotation")
    private boolean disablePreviewRotation = false;
    @SerializedName("gui_foreground")
    private String guiForeground = StringUtils.EMPTY;
    @SerializedName("gui_background")
    private String guiBackground = StringUtils.EMPTY;

    @Nullable
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    public String getTips() {
        return this.tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    @Nullable
    public String[] getExtraAnimationNames() {
        return this.extraAnimationNames;
    }

    public void setExtraAnimationNames(String[] extraAnimationNames) {
        this.extraAnimationNames = extraAnimationNames;
    }

    @Nullable
    public String[] getAuthors() {
        return this.authors;
    }

    public void setAuthors(String[] authors) {
        this.authors = authors;
    }

    @Nullable
    public String getLicense() {
        return this.license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public boolean getFree() {
        return this.free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    @Nullable
    public String getPreviewAnimation() {
        return this.previewAnimation;
    }

    public void setPreviewAnimation(String previewAnimation) {
        this.previewAnimation = previewAnimation;
    }

    public boolean getDisablePreviewRotation() {
        return this.disablePreviewRotation;
    }

    public void setDisablePreviewRotation(boolean disablePreviewRotation) {
        this.disablePreviewRotation = disablePreviewRotation;
    }

    @Nullable
    public String getGuiForeground() {
        return this.guiForeground;
    }

    public void setGuiForeground(String guiForeground) {
        this.guiForeground = guiForeground;
    }

    @Nullable
    public String getGuiBackground() {
        return this.guiBackground;
    }

    public void setGuiBackground(String guiBackground) {
        this.guiBackground = guiBackground;
    }
}
