package com.elfmcys.yesstevemodel.capability;

import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArrowModelCapabilityProvider implements ICapabilitySerializable<NBTTagString> {
    @CapabilityInject(ArrowModelCapability.class)
    public static Capability<ArrowModelCapability> ARROW_MODEL_CAP = null;
    private ArrowModelCapability instance = ARROW_MODEL_CAP.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing side) {
        return cap == ARROW_MODEL_CAP;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (this.hasCapability(cap, side)) {
            return ARROW_MODEL_CAP.cast(this.createCapability());
        }
        return null;
    }

    @Nonnull
    private ArrowModelCapability createCapability() {
        if (this.instance == null) {
            this.instance = new ArrowModelCapability();
        }
        return this.instance;
    }

    @Override
    public void deserializeNBT(NBTTagString nbt) {
        this.createCapability().deserializeNBT(nbt);
    }

    @Override
    public NBTTagString serializeNBT() {
        return this.createCapability().serializeNBT();
    }
}
