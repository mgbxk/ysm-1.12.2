package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.client.compat.CrossbowCompat;
import com.elfmcys.yesstevemodel.client.compat.SpyglassCompat;
import com.elfmcys.yesstevemodel.client.compat.TridentCompat;
import com.elfmcys.yesstevemodel.util.ResourceUtil;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public abstract class AbstractConditionItem {
    /// 代表继续下一个测试，如果作为返回结果，外部不会播放该动画
    protected static final String EMPTY = "";
    protected final int preSize;
    protected final String idPre;
    protected final String orePre;
    //protected final String tagPre;
    protected final String extraPre;
    /// 物品 ID 测试
    protected final List<ResourceLocation> idTest = Lists.newArrayList();
    /// 矿物词典测试
    protected final List<String> oreTest = Lists.newArrayList();
    /// 标签测试
    //protected final List<ResourceLocation> tagTest = Lists.newArrayList();
    /// 额外测试，{@link IExtraMatcher} 可为 null
    protected final List<Pair<String, IExtraMatcher>> extraTest = Lists.newArrayList();

    public AbstractConditionItem(String generalPre) {
        this.preSize = generalPre.length() + 1;
        this.idPre = generalPre + "$";
        this.orePre = generalPre + "~";
        //this.tagPre = generalPre + "#";
        this.extraPre = generalPre + ":";
    }

    public void addTest(String name) {
        if (name.length() <= this.preSize) {
            return;
        }
        String substring = name.substring(this.preSize);
        if (name.startsWith(this.idPre)) {
            if (ResourceUtil.isValidResourceLocation(substring)) {
                this.idTest.add(new ResourceLocation(substring));
            }
            return;
        }
        if (name.startsWith(this.orePre)) {
            if (OreDictionary.doesOreNameExist(substring)) {
                this.oreTest.add(substring);
            }
            return;
        }
//        if (name.startsWith(this.tagPre)) {
//            if (ResourceUtil.isValidResourceLocation(substring)) {
//                ResourceLocation res = new ResourceLocation(substring);
//                ITag<Item> tag = ItemTags.getAllTags().getTag(res);
//                if (tag == null) {
//                    return;
//                }
//                this.tagTest.add(res);
//            }
//            return;
//        }
        if (name.startsWith(this.extraPre)) {
            substring = substring.toLowerCase(Locale.US);
            if (substring.equals("none")) {
                return;
            }
            this.extraTest.add(Pair.of(substring, EXTRA_MATCHERS.get(substring)));
        }
    }

    public String doTest(EntityPlayer player, EnumHand hand) {
        if (player.getHeldItem(hand).isEmpty()) {
            return EMPTY;
        }
        String result;
        result = this.doIdTest(player, hand);
        if (!result.isEmpty()) return result;
        result = this.doOreTest(player, hand);
        if (!result.isEmpty()) return result;
//        result = this.doTagTest(player, hand);
//        if (!result.isEmpty()) return result;
        result = this.doExtraTest(player, hand);
        if (!result.isEmpty()) return result;
        return EMPTY;
    }

    private String doIdTest(EntityPlayer player, EnumHand hand) {
        if (this.idTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getHeldItem(hand);
        ResourceLocation registryName = itemInHand.getItem().getRegistryName();
        if (registryName == null) {
            return EMPTY;
        }
        if (this.idTest.contains(registryName)) {
            return this.idPre + registryName;
        }
        return EMPTY;
    }

    private String doOreTest(EntityPlayer player, EnumHand hand) {
        if (this.oreTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack itemInHand = player.getHeldItem(hand);
        for (int id : OreDictionary.getOreIDs(itemInHand)) {
            String name = OreDictionary.getOreName(id);
            if ("Unknown".equals(name)) continue;
            if (this.oreTest.contains(name)) {
                return this.orePre + name;
            }
        }
        return EMPTY;
    }

    // TODO: Tag 转矿词系统，道阻且长
//    private String doTagTest(EntityPlayer player, EnumHand hand) {
//        if (this.tagTest.isEmpty()) {
//            return EMPTY;
//        }
//        Item itemInHand = player.getHeldItem(hand).getItem();
//        return this.tagTest.stream().filter(itemTagKey -> {
//            ITag<Item> tag = ItemTags.getAllTags().getTag(itemTagKey);
//            if (tag != null) {
//                return tag.contains(itemInHand);
//            }
//            return false;
//        }).findFirst().map(itemTagKey -> this.tagPre + itemTagKey).orElse(EMPTY);
//    }

    private String doExtraTest(EntityPlayer player, EnumHand hand) {
        if (this.extraTest.isEmpty()) {
            return EMPTY;
        }
        ItemStack stack = player.getHeldItem(hand);
        for (Pair<String, IExtraMatcher> pair : this.extraTest) {
            String key = pair.getLeft();
            // 同名或同类均可
            if (isSameActionName(stack, key) || (pair.getRight() != null && pair.getRight().matches(player, stack))) {
                return this.extraPre + key;
            }
        }
        return EMPTY;
    }

    protected interface IExtraMatcher {
        boolean matches(EntityPlayer player, ItemStack stack);
    }

    /// Inner Name, Inner Matcher
    private static final LinkedHashMap<String, IExtraMatcher> EXTRA_MATCHERS = new LinkedHashMap<>();

    static {
        EXTRA_MATCHERS.put("sword", (player, stack) ->
                stack.getItem() instanceof ItemSword);
        EXTRA_MATCHERS.put("axe", (player, stack) ->
                stack.getItem() instanceof ItemAxe);
        EXTRA_MATCHERS.put("pickaxe", (player, stack) ->
                stack.getItem() instanceof ItemPickaxe);
        EXTRA_MATCHERS.put("shovel", (player, stack) ->
                stack.getItem() instanceof ItemSpade);
        EXTRA_MATCHERS.put("hoe", (player, stack) ->
                stack.getItem() instanceof ItemHoe);
        EXTRA_MATCHERS.put("shield", (player, stack) ->
                stack.getItem() instanceof ItemShield);
        EXTRA_MATCHERS.put("throwable_potion", (player, stack) ->
                stack.getItem() instanceof ItemSplashPotion || stack.getItem() instanceof ItemLingeringPotion);
        EXTRA_MATCHERS.put("fishing_rod", (player, stack) ->
                stack.getItem() instanceof ItemFishingRod);
        EXTRA_MATCHERS.put("bow", (player, stack) ->
                stack.getItem() instanceof ItemBow);
        EXTRA_MATCHERS.put(TridentCompat.SPEAR_ACTION, (player, stack) ->
                TridentCompat.isSpearAction(stack));
        EXTRA_MATCHERS.put(CrossbowCompat.CROSSBOW_ACTION, (player, stack) ->
                CrossbowCompat.isCrossbowAction(stack));
        EXTRA_MATCHERS.put(SpyglassCompat.SPYGLASS_ACTION, (player, stack) ->
                SpyglassCompat.isSpyglassAction(stack));
    }

    private static boolean isSameActionName(ItemStack stack, String action) {
        return stack.getItemUseAction().name().equalsIgnoreCase(action);
    }
}
