package com.elfmcys.yesstevemodel.capability;

import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StarModelsCapabilityProvider implements ICapabilitySerializable<NBTTagList> {
    @CapabilityInject(StarModelsCapability.class)
    public static Capability<StarModelsCapability> STAR_MODELS_CAP = null;
    private StarModelsCapability instance = STAR_MODELS_CAP.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing side) {
        return cap == STAR_MODELS_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (this.hasCapability(cap, side)) {
            return STAR_MODELS_CAP.cast(this.createCapability());
        }
        return null;
    }

    @Nonnull
    private StarModelsCapability createCapability() {
        if (this.instance == null) {
            this.instance = new StarModelsCapability();
        }
        return this.instance;
    }

    @Override
    public void deserializeNBT(NBTTagList nbt) {
        this.createCapability().deserializeNBT(nbt);
    }

    @Override
    public NBTTagList serializeNBT() {
        return this.createCapability().serializeNBT();
    }
}
