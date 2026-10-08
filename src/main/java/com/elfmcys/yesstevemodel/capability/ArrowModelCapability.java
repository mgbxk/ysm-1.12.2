package com.elfmcys.yesstevemodel.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class ArrowModelCapability {
    private String modelId = "";

    public String getModelId() {
        return this.modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void copyFrom(ArrowModelCapability source) {
        this.modelId = source.modelId;
    }

    public NBTTagString serializeNBT() {
        return new NBTTagString(this.modelId);
    }

    public void deserializeNBT(NBTTagString nbt) {
        this.modelId = nbt.getString();
    }

    public static class Storage implements Capability.IStorage<ArrowModelCapability> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<ArrowModelCapability> capability, ArrowModelCapability instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<ArrowModelCapability> capability, ArrowModelCapability instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagString) nbt);
        }
    }
}
