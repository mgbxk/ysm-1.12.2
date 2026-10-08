package com.elfmcys.yesstevemodel.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelInfoCapabilityProvider implements ICapabilitySerializable<NBTTagCompound> {
    @CapabilityInject(ModelInfoCapability.class)
    public static Capability<ModelInfoCapability> MODEL_INFO_CAP = null;
    private ModelInfoCapability instance = MODEL_INFO_CAP.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing side) {
        return cap == MODEL_INFO_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (this.hasCapability(cap, side)) {
            return MODEL_INFO_CAP.cast(this.createCapability());
        }
        return null;
    }

    @Nonnull
    private ModelInfoCapability createCapability() {
        if (this.instance == null) {
            this.instance = new ModelInfoCapability();
        }
        return this.instance;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.createCapability().deserializeNBT(nbt);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return this.createCapability().serializeNBT();
    }
}
