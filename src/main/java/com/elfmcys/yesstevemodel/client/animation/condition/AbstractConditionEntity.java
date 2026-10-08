package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public abstract class AbstractConditionEntity {
    private static final String EMPTY = "";
    private final List<ResourceLocation> idTest = Lists.newArrayList();
    //private final List<ResourceLocation> tagTest = Lists.newArrayList();
    private final String idPre;
    //private final String tagPre;

    public AbstractConditionEntity(String generalPre) {
        this.idPre = generalPre + "$";
        //this.tagPre = generalPre + "#";
    }

    public void addTest(String name) {
        int preSize = this.idPre.length();
        if (name.length() <= preSize) {
            return;
        }
        String substring = name.substring(preSize);
        if (name.startsWith(this.idPre) && ResourceUtil.isValidResourceLocation(substring)) {
            this.idTest.add(new ResourceLocation(substring));
            //return;
        }
//        if (name.startsWith(this.tagPre) && ResourceUtil.isValidResourceLocation(substring)) {
//            ITagManager<EntityType<?>> tags = ForgeRegistries.ENTITY_TYPES.tags();
//            if (tags == null) {
//                return;
//            }
//            TagKey<EntityType<?>> tagKey = tags.createTagKey(new ResourceLocation(substring));
//            this.tagTest.add(tagKey);
//            //return;
//        }
    }

    public abstract String doTest(EntityPlayer player);

    protected String doTest(Entity entity) {
        if (entity == null || !entity.isEntityAlive()) {
            return EMPTY;
        }
        String result;
        result = this.doIdTest(entity);
        if (!result.isEmpty()) return result;
//        result = this.doTagTest(entity);
//        if (!result.isEmpty()) return result;
        return EMPTY;
    }

    private String doIdTest(Entity entity) {
        if (this.idTest.isEmpty()) {
            return EMPTY;
        }
        ResourceLocation registryName = EntityList.getKey(entity);
        if (registryName == null) {
            return EMPTY;
        }
        if (this.idTest.contains(registryName)) {
            return this.idPre + registryName;
        }
        return EMPTY;
    }

//    private String doTagTest(Entity entity) {
//        if (this.tagTest.isEmpty()) {
//            return EMPTY;
//        }
//        ITagManager<EntityType<?>> tags = ForgeRegistries.ENTITY_TYPES.tags();
//        if (tags == null) {
//            return EMPTY;
//        }
//        return this.tagTest.stream().filter(tag -> entity.getType().is(tag)).findFirst().map(itemTagKey -> this.tagPre + itemTagKey.location()).orElse(EMPTY);
//    }
}
