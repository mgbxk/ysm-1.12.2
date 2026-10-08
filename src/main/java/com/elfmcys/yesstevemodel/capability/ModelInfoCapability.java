package com.elfmcys.yesstevemodel.capability;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.config.ServerConfig;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class ModelInfoCapability {
    private ResourceLocation modelId = new ResourceLocation(YesSteveModel.MOD_ID, ServerConfig.DEFAULT_MODEL_ID);
    private ResourceLocation selectTexture = new ResourceLocation(YesSteveModel.MOD_ID, ServerConfig.DEFAULT_MODEL_ID + "/" + ServerConfig.DEFAULT_MODEL_TEXTURE);
    private String animation = "idle";
    private boolean playAnimation = false;
    private long animationRequest;
    private boolean dirty;
    private volatile java.util.Map<String, java.util.Map<String, Double>> modelSettings = java.util.Collections.emptyMap();

    public java.util.Map<String, Double> getModelSettings() { return modelSettings.getOrDefault(modelId.toString(), java.util.Collections.emptyMap()); }

    public void setModelSetting(String name, double value) {
        if (!Double.isFinite(value)) return;
        java.util.Map<String, java.util.Map<String, Double>> next = new java.util.HashMap<>(modelSettings);
        java.util.Map<String, Double> values = new java.util.HashMap<>(getModelSettings()); values.put(name, value);
        next.put(modelId.toString(), java.util.Collections.unmodifiableMap(values));
        modelSettings = java.util.Collections.unmodifiableMap(next); markDirty();
    }

    public void setModelAndTexture(ResourceLocation modelId, ResourceLocation selectTexture) {
        this.modelId = modelId;
        this.selectTexture = selectTexture;
        this.markDirty();
    }

    public void copyFrom(ModelInfoCapability source) {
        this.modelId = source.modelId;
        this.selectTexture = source.selectTexture;
        this.animation = source.animation;
        this.playAnimation = source.playAnimation;
        this.animationRequest = source.animationRequest;
        this.modelSettings = source.modelSettings;
        this.markDirty();
    }

    public ResourceLocation getModelId() {
        return this.modelId;
    }

    public ResourceLocation getSelectTexture() {
        return this.selectTexture;
    }

    public void setSelectTexture(ResourceLocation selectTexture) {
        this.selectTexture = selectTexture;
        this.markDirty();
    }

    public void playAnimation(String animation) {
        this.animation = animation;
        this.playAnimation = true;
        this.animationRequest++;
        this.markDirty();
    }

    public void stopAnimation() {
        this.playAnimation = false;
        this.markDirty();
    }

    public String getAnimation() {
        return this.animation;
    }

    public boolean isPlayAnimation() {
        return this.playAnimation;
    }

    public long getAnimationRequest() { return this.animationRequest; }

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("model_id", this.modelId.toString());
        tag.setString("select_texture", this.selectTexture.toString());
        tag.setString("animation", this.animation);
        tag.setBoolean("play_animation", this.playAnimation);
        tag.setLong("animation_request", this.animationRequest);
        NBTTagCompound settings = new NBTTagCompound();
        modelSettings.forEach((model, values) -> {
            NBTTagCompound entries = new NBTTagCompound(); values.forEach(entries::setDouble); settings.setTag(model, entries);
        });
        tag.setTag("model_settings", settings);
        return tag;
    }

    public void deserializeNBT(NBTTagCompound nbt) {
        this.modelId = new ResourceLocation(nbt.getString("model_id"));
        this.selectTexture = new ResourceLocation(nbt.getString("select_texture"));
        this.animation = nbt.getString("animation");
        this.playAnimation = nbt.getBoolean("play_animation");
        this.animationRequest = nbt.getLong("animation_request");
        java.util.Map<String, java.util.Map<String, Double>> settings = new java.util.HashMap<>();
        NBTTagCompound saved = nbt.getCompoundTag("model_settings");
        for (String model : saved.getKeySet()) {
            java.util.Map<String, Double> values = new java.util.HashMap<>(); NBTTagCompound entries = saved.getCompoundTag(model);
            for (String name : entries.getKeySet()) if (name.startsWith("variable.") && Double.isFinite(entries.getDouble(name))) values.put(name, entries.getDouble(name));
            settings.put(model, java.util.Collections.unmodifiableMap(values));
        }
        this.modelSettings = java.util.Collections.unmodifiableMap(settings);
    }

    public static class Storage implements Capability.IStorage<ModelInfoCapability> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<ModelInfoCapability> capability, ModelInfoCapability instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<ModelInfoCapability> capability, ModelInfoCapability instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagCompound) nbt);
        }
    }
}
