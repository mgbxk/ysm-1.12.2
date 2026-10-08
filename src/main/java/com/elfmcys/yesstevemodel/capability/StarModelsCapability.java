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

public class StarModelsCapability {
    private Set<ResourceLocation> starModels = Sets.newHashSet();

    public void addModel(ResourceLocation modelId) {
        this.starModels.add(modelId);
    }

    public void copyFrom(StarModelsCapability source) {
        this.starModels = source.starModels;
    }

    public void removeModel(ResourceLocation modelId) {
        this.starModels.remove(modelId);
    }

    public boolean containModel(ResourceLocation modelId) {
        return this.starModels.contains(modelId);
    }

    public Set<ResourceLocation> getStarModels() {
        return this.starModels;
    }

    public void setStarModels(Set<ResourceLocation> starModels) {
        this.starModels = starModels;
    }

    public void clear() {
        this.starModels.clear();
    }

    public NBTTagList serializeNBT() {
        NBTTagList listTag = new NBTTagList();
        for (ResourceLocation modelId : this.starModels) {
            listTag.appendTag(new NBTTagString(modelId.toString()));
        }
        return listTag;
    }

    public void deserializeNBT(NBTTagList nbt) {
        this.starModels.clear();
        for (NBTBase tag : nbt) {
            if (tag instanceof NBTTagString string) {
                this.starModels.add(new ResourceLocation(string.getString()));
            }
        }
    }

    public static class Storage implements Capability.IStorage<StarModelsCapability> {
        @Nullable
        @Override
        public NBTBase writeNBT(Capability<StarModelsCapability> capability, StarModelsCapability instance, EnumFacing side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(Capability<StarModelsCapability> capability, StarModelsCapability instance, EnumFacing side, NBTBase nbt) {
            instance.deserializeNBT((NBTTagList) nbt);
        }
    }
}
