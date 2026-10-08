package com.elfmcys.yesstevemodel.capability;

import com.google.common.collect.Sets;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Set;

public class AuthModelsCapability {
    private Set<ResourceLocation> authModels = Sets.newHashSet();

    public void addModel(ResourceLocation modelId) {
        this.authModels.add(modelId);
    }

    public void copyFrom(AuthModelsCapability source) {
        this.authModels = source.authModels;
    }

    public void removeModel(ResourceLocation modelId) {
        this.authModels.remove(modelId);
    }

    public boolean containModel(ResourceLocation modelId) {
        return this.authModels.contains(modelId);
    }

    public Set<ResourceLocation> getAuthModels() {
        return this.authModels;
    }

    public void setAuthModels(Set<ResourceLocation> authModels) {
        this.authModels = authModels;
    }

    public void clear() {
        this.authModels.clear();
    }

    public NBTTagList serializeNBT() {
        NBTTagList listTag = new NBTTagList();
        for (ResourceLocation modelId : this.authModels) {
            listTag.appendTag(new NBTTagString(modelId.toString()));
        }
        return listTag;
    }

    public void deserializeNBT(NBTTagList nbt) {
        this.authModels.clear();
        for (NBTBase tag : nbt) {
            if (tag instanceof NBTTagString string) {
                this.authModels.add(new ResourceLocation(string.getString()));
            }
        }
    }

    public static class Storage implements Capability.IStorage<AuthModelsCapability> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<AuthModelsCapability> capability, AuthModelsCapability instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<AuthModelsCapability> capability, AuthModelsCapability instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagList) nbt);
        }
    }
}
