package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionArmor {
    private static final Pattern ID_PRE_REG = Pattern.compile("^(.+?)\\$(.*?)$");
    private static final Pattern ORE_PRE_REG = Pattern.compile("^(.+?)~(.*?)$");
    //private static final Pattern TAG_PRE_REG = Pattern.compile("^(.+?)#(.*?)$");
    private static final String EMPTY = "";

    private final Map<EntityEquipmentSlot, List<ResourceLocation>> idTest = Maps.newHashMap();
    private final Map<EntityEquipmentSlot, List<String>> oreTest = Maps.newHashMap();
    //private final Map<EntityEquipmentSlot, List<ResourceLocation>> tagTest = Maps.newHashMap();

    public void addTest(String name) {
        Matcher matcherId = ID_PRE_REG.matcher(name);
        if (matcherId.find()) {
            EntityEquipmentSlot type = getType(matcherId.group(1));
            if (type == null) {
                return;
            }
            String id = matcherId.group(2);
            if (!ResourceUtil.isValidResourceLocation(id)) {
                return;
            }
            ResourceLocation res = new ResourceLocation(id);
            if (this.idTest.containsKey(type)) {
                this.idTest.get(type).add(res);
            } else {
                this.idTest.put(type, Lists.newArrayList(res));
            }
            return;
        }

        Matcher matcherOre = ORE_PRE_REG.matcher(name);
        if (matcherOre.find()) {
            EntityEquipmentSlot type = getType(matcherOre.group(1));
            if (type == null) {
                return;
            }
            String id = matcherOre.group(2);
            if (!OreDictionary.doesOreNameExist(id)) {
                return;
            }
            if (this.oreTest.containsKey(type)) {
                this.oreTest.get(type).add(id);
            } else {
                this.oreTest.put(type, Lists.newArrayList(id));
            }
            //return;
        }

//        Matcher matcherTag = TAG_PRE_REG.matcher(name);
//        if (matcherTag.find()) {
//            EntityEquipmentSlot type = getType(matcherTag.group(1));
//            if (type == null) {
//                return;
//            }
//            String id = matcherTag.group(2);
//            if (!ResourceUtil.isValidResourceLocation(id)) {
//                return;
//            }
//            ResourceLocation res = new ResourceLocation(id);
//            ITag<Item> tag = ItemTags.getAllTags().getTag(res);
//            if (tag == null) {
//                return;
//            }
//            if (this.tagTest.containsKey(type)) {
//                this.tagTest.get(type).add(res);
//            } else {
//                this.tagTest.put(type, Lists.newArrayList(res));
//            }
//            //return;
//        }
    }

    public String doTest(EntityPlayer player, EntityEquipmentSlot slot) {
        ItemStack item = player.getItemStackFromSlot(slot);
        if (item.isEmpty()) {
            return EMPTY;
        }
        String result;
        result = this.doIdTest(player, slot);
        if (!result.isEmpty()) return result;
        result = this.doOreTest(player, slot);
        if (!result.isEmpty()) return result;
//        result = this.doTagTest(player, slot);
//        if (!result.isEmpty()) return result;
        return EMPTY;
    }

    private String doIdTest(EntityPlayer player, EntityEquipmentSlot slot) {
        if (this.idTest.isEmpty()) {
            return EMPTY;
        }
        if (!this.idTest.containsKey(slot) || this.idTest.get(slot).isEmpty()) {
            return EMPTY;
        }
        List<ResourceLocation> idListTest = this.idTest.get(slot);
        ItemStack item = player.getItemStackFromSlot(slot);
        ResourceLocation registryName = item.getItem().getRegistryName();
        if (registryName == null) {
            return EMPTY;
        }
        if (idListTest.contains(registryName)) {
            return slot.getName() + "$" + registryName;
        }
        return EMPTY;
    }

    private String doOreTest(EntityPlayer player, EntityEquipmentSlot slot) {
        if (this.oreTest.isEmpty()) {
            return EMPTY;
        }
        if (!this.oreTest.containsKey(slot) || this.oreTest.get(slot).isEmpty()) {
            return EMPTY;
        }
        List<String> tagListTest = this.oreTest.get(slot);
        ItemStack item = player.getItemStackFromSlot(slot);
        for (int id : OreDictionary.getOreIDs(item)) {
            String name = OreDictionary.getOreName(id);
            if ("Unknown".equals(name)) continue;
            if (tagListTest.contains(name)) {
                return slot.getName() + "~" + name;
            }
        }
        return EMPTY;
    }

//    private String doTagTest(EntityPlayer player, EntityEquipmentSlot slot) {
//        if (this.tagTest.isEmpty()) {
//            return EMPTY;
//        }
//        if (!this.tagTest.containsKey(slot) || this.tagTest.get(slot).isEmpty()) {
//            return EMPTY;
//        }
//        List<ResourceLocation> tagListTest = this.tagTest.get(slot);
//        Item item = player.getItemStackFromSlot(slot).getItem();
//        return tagListTest.stream().filter(itemTagKey -> {
//            ITag<Item> tag = ItemTags.getAllTags().getTag(itemTagKey);
//            if (tag != null) {
//                return tag.contains(item);
//            }
//            return false;
//        }).findFirst().map(itemTagKey -> slot.getName() + "#" + itemTagKey).orElse(EMPTY);
//    }


    @Nullable
    public static EntityEquipmentSlot getType(String type) {
        for (EntityEquipmentSlot equipmentslot : EntityEquipmentSlot.values()) {
            if (equipmentslot.getName().equals(type)) {
                return equipmentslot;
            }
        }
        return null;
    }
}
