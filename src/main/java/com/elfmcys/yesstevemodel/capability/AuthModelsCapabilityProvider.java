package com.elfmcys.yesstevemodel.capability;

import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AuthModelsCapabilityProvider implements ICapabilitySerializable<NBTTagList> {
    @CapabilityInject(AuthModelsCapability.class)
    public static Capability<AuthModelsCapability> AUTH_MODELS_CAP = null;
    private AuthModelsCapability instance = AUTH_MODELS_CAP.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing side) {
        return cap == AUTH_MODELS_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (this.hasCapability(cap, side)) {
            return AUTH_MODELS_CAP.cast(this.createCapability());
        }
        return null;
    }

    @Nonnull
    private AuthModelsCapability createCapability() {
        if (this.instance == null) {
            this.instance = new AuthModelsCapability();
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
