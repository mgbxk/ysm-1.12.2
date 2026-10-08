package com.elfmcys.yesstevemodel.client.animation;

import com.elfmcys.yesstevemodel.client.animation.condition.*;
import com.elfmcys.yesstevemodel.client.compat.CarryOnCompat;
import com.elfmcys.yesstevemodel.client.compat.CrossbowCompat;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.LinkedList;

/*
一部分对照
1.16.5 - 1.12.2
Hand - EnumHand
EquipmentSlotType - EntityEquipmentSlot
player.getItemBySlot(slot) - player.getItemStackFromSlot(slot)
player.isUsingItem() - player.isHandActive()
player.getUsedItemHand() - player.getActiveHand()
player.getTicksUsingItem() - player.getItemInUseMaxCount()
player.getItemInHand() - player.getHeldItem()

1.12.2 几个物品使用时间的解释：
getMaxItemUseDuration() - 总时长（常量）
getItemInUseCount() - 剩余时长（倒计时）
getItemInUseMaxCount() - 已用时长（计时器）
*/

public final class AnimationManager {
    //private final static String TAC_ID = "tac";
    private static AnimationManager MANAGER;
    private final Int2ObjectOpenHashMap<LinkedList<AnimationState>> data = new Int2ObjectOpenHashMap<>();

    public static AnimationManager getInstance() {
        if (MANAGER == null) {
            MANAGER = new AnimationManager();
        }
        return MANAGER;
    }

    @Nonnull
    private static <P extends IAnimatable> PlayState playLoopAnimation(AnimationEvent<P> event, String animationName) {
        return playAnimation(event, animationName, ILoopType.EDefaultLoopTypes.LOOP);
    }

    @Nonnull
    private static <P extends IAnimatable> PlayState playAnimation(AnimationEvent<P> event, String animationName, ILoopType loopType) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, loopType));
        return PlayState.CONTINUE;
    }

    @Nonnull
    private static <P extends IAnimatable> PlayState playAnimation(AnimationEvent<P> event, String animationName) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName));
        return PlayState.CONTINUE;
    }

    public void register(AnimationState state) {
        if (this.data.containsKey(state.priority())) {
            this.data.get(state.priority()).add(state);
        } else {
            LinkedList<AnimationState> states = Lists.newLinkedList();
            states.add(state);
            this.data.put(state.priority(), states);
        }
    }

    @Nonnull
    public PlayState predicateParallel(AnimationEvent<CustomPlayerEntity> event, String animationName) {
        if (Minecraft.getMinecraft().isGamePaused()) {
            return PlayState.STOP;
        }
        return playLoopAnimation(event, animationName);
    }

    @Nonnull
    public PlayState predicateCap(AnimationEvent<CustomPlayerEntity> event) {
        CustomPlayerEntity animatable = event.getAnimatable();
        EntityPlayer player = animatable.getPlayer();
        if (player == null) {
            if (animatable.hasPreviewAnimation()) {
                return playLoopAnimation(event, animatable.getPreviewAnimation());
            }
            return PlayState.STOP;
        }

        return CapabilityEvent.getModelInfoCap(player).map(cap -> {
            if (cap.isPlayAnimation()) {
                if (event.getController() instanceof com.elfmcys.yesstevemodel.client.animation.modern.ModernPlayerController controller) {
                    controller.acceptAnimationRequest(cap.getAnimationRequest());
                }
                return playAnimation(event, cap.getAnimation());
            }
            return PlayState.STOP;
        }).orElse(PlayState.STOP);
    }

    @Nonnull
    public PlayState predicateMain(AnimationEvent<CustomPlayerEntity> event) {
        EntityPlayer player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        for (int i = Priority.HIGHEST; i <= Priority.LOWEST; i++) {
            if (!this.data.containsKey(i)) {
                continue;
            }
            LinkedList<AnimationState> states = this.data.get(i);
            for (AnimationState state : states) {
                if (state.predicate().test(player, event)) {
                    String animationName = state.animationName();
                    ILoopType loopType = state.loopType();
//                    if (Loader.isModLoaded(TAC_ID) && TacGunRenderer.isGun(player.getMainHandItem())) {
//                        return TacGunRenderer.playGunMainAnimation(event, animationName, loopType);
//                    }
                    return playAnimation(event, animationName, loopType);
                }
            }
        }
        return PlayState.STOP;
    }

    @Nonnull
    public PlayState predicateOffhandHold(AnimationEvent<CustomPlayerEntity> event) {
        CustomPlayerEntity animatable = event.getAnimatable();
        EntityPlayer player = animatable.getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        if (!player.isSwingInProgress && !player.isHandActive()) {
            ItemStack offhandItem = player.getHeldItem(EnumHand.OFF_HAND);
            if (CrossbowCompat.isCharged(offhandItem)) {
                return playAnimation(event, "hold_offhand:charged_crossbow", ILoopType.EDefaultLoopTypes.LOOP);

            }
        }
        if (checkSwingAndUse(player, EnumHand.OFF_HAND)) {
            ItemStack offhandItem = player.getHeldItem(EnumHand.OFF_HAND);
            if (!isSameItem(animatable, offhandItem, EnumHand.OFF_HAND)) {
                animatable.getHandItemsForAnimation()[EnumHand.OFF_HAND.ordinal()] = offhandItem;
                playAnimation(event, "empty", ILoopType.EDefaultLoopTypes.LOOP);
            }

            ResourceLocation id = event.getAnimatable().getAnimation();
            ConditionalHold conditionalHold = ConditionManager.getHoldOffhand(id);
            if (conditionalHold != null) {
                String name = conditionalHold.doTest(player, EnumHand.OFF_HAND);
                if (StringUtils.isNoneBlank(name)) {
                    return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        }
        return PlayState.STOP;
    }

    @Nonnull
    public PlayState predicateMainhandHold(AnimationEvent<CustomPlayerEntity> event) {
        CustomPlayerEntity animatable = event.getAnimatable();
        EntityPlayer player = animatable.getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        if (CarryOnCompat.isInstalled()) {
            String carryName = CarryOnCompat.getCarryOnString(player);
            if (carryName != null) {
                return playAnimation(event, "carryon:" + carryName, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        if (!player.isSwingInProgress && !player.isHandActive()) {
            ItemStack mainHandItem = player.getHeldItem(EnumHand.MAIN_HAND);
//            PlayState gunHoldAnimation = GunClientUtil.playGunHoldAnimation(mainHandItem, event);
//            if (gunHoldAnimation != null) {
//                return gunHoldAnimation;
//            }
            if (CrossbowCompat.isCharged(mainHandItem)) {
                return playAnimation(event, "hold_mainhand:charged_crossbow", ILoopType.EDefaultLoopTypes.LOOP);
            }
            if (player.fishEntity != null) {
                return playAnimation(event, "hold_mainhand:fishing", ILoopType.EDefaultLoopTypes.LOOP);
            }
        }

        if (checkSwingAndUse(player, EnumHand.MAIN_HAND)) {
            ItemStack mainHandItem = player.getHeldItem(EnumHand.MAIN_HAND);
            if (!isSameItem(animatable, mainHandItem, EnumHand.MAIN_HAND)) {
                animatable.getHandItemsForAnimation()[EnumHand.MAIN_HAND.ordinal()] = mainHandItem;
                playAnimation(event, "empty", ILoopType.EDefaultLoopTypes.LOOP);
            }

            ResourceLocation id = event.getAnimatable().getAnimation();
            ConditionalHold conditionalHold = ConditionManager.getHoldMainhand(id);
            if (conditionalHold != null) {
                String name = conditionalHold.doTest(player, EnumHand.MAIN_HAND);
                if (StringUtils.isNoneBlank(name)) {
                    return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                }
            }
        }
        return PlayState.STOP;
    }

    @Nonnull
    public PlayState predicateSwing(AnimationEvent<CustomPlayerEntity> event) {
        EntityPlayer player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        if (player.isSwingInProgress && !player.isPlayerSleeping()) {
            if (player.swingProgressInt == 0) {
                // 空动画用于重置 PLAY_ONCE 动画
                playAnimation(event, "empty", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
            }
            ResourceLocation id = event.getAnimatable().getAnimation();
            ConditionalSwing conditionalSwing = (player.swingingHand == EnumHand.MAIN_HAND) ? ConditionManager.getSwingMainhand(id) : ConditionManager.getSwingOffhand(id);
            if (conditionalSwing != null) {
                String name = conditionalSwing.doTest(player, player.swingingHand);
                if (StringUtils.isNoneBlank(name)) {
                    return playAnimation(event, name, ILoopType.EDefaultLoopTypes.PLAY_ONCE);
                }
            }
            String defaultSwing = (player.swingingHand == EnumHand.MAIN_HAND) ? "swing_hand" : "swing_offhand";
            return playAnimation(event, defaultSwing, ILoopType.EDefaultLoopTypes.PLAY_ONCE);
        }
        return PlayState.CONTINUE;
    }

    @Nonnull
    public PlayState predicateUse(AnimationEvent<CustomPlayerEntity> event) {
        EntityPlayer player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        if (player.isHandActive() && !player.isPlayerSleeping()) {
            if (player.getItemInUseMaxCount() == 1) {
                event.getController().shouldResetTick = true;
                event.getController().adjustTick(0);
            }
//            if (Loader.isModLoaded(TAC_ID) && TacGunRenderer.isGrenade(player.getUseItem())) {
//                return TacGunRenderer.playGrenadeAnimation(event, player.getUsedItemHand());
//            }
            if (player.getActiveHand() == EnumHand.MAIN_HAND) {
                ResourceLocation id = event.getAnimatable().getAnimation();
                ConditionalUse conditionalUse = ConditionManager.getUseMainhand(id);
                if (conditionalUse != null) {
                    String name = conditionalUse.doTest(player, EnumHand.MAIN_HAND);
                    if (StringUtils.isNoneBlank(name)) {
                        return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                    }
                }
                return playAnimation(event, "use_mainhand", ILoopType.EDefaultLoopTypes.LOOP);
            } else {
                ResourceLocation id = event.getAnimatable().getAnimation();
                ConditionalUse conditionalUse = ConditionManager.getUseOffhand(id);
                if (conditionalUse != null) {
                    String name = conditionalUse.doTest(player, EnumHand.OFF_HAND);
                    if (StringUtils.isNoneBlank(name)) {
                        return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
                    }
                }
                return playAnimation(event, "use_offhand", ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        return PlayState.STOP;
    }

    @Nonnull
    public PlayState predicateArmor(AnimationEvent<CustomPlayerEntity> event, EntityEquipmentSlot slot) {
        EntityPlayer player = event.getAnimatable().getPlayer();
        if (player == null) {
            return PlayState.STOP;
        }
        ItemStack itemBySlot = player.getItemStackFromSlot(slot);
        if (itemBySlot.isEmpty()) {
            return PlayState.STOP;
        }

        ResourceLocation id = event.getAnimatable().getAnimation();
        ConditionArmor conditionArmor = ConditionManager.getArmor(id);
        if (conditionArmor != null) {
            String name = conditionArmor.doTest(player, slot);
            if (StringUtils.isNoneBlank(name)) {
                return playAnimation(event, name, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }

        ResourceLocation animation = event.getAnimatable().getAnimation();
        String defaultName = slot.getName() + ":default";
        if (GeckoLibCache.getInstance().getAnimations().get(animation).animations().containsKey(defaultName)) {
            return playAnimation(event, defaultName, ILoopType.EDefaultLoopTypes.LOOP);
        }
        return PlayState.STOP;
    }

    private static boolean checkSwingAndUse(EntityPlayer player, EnumHand hand) {
        if (player.isSwingInProgress && player.swingingHand == hand) {
            return false;
        }
        return !player.isHandActive() || player.getActiveHand() != hand;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isSameItem(CustomPlayerEntity animatable, ItemStack maidItem, EnumHand hand) {
        ItemStack preItem = animatable.getHandItemsForAnimation()[hand.ordinal()];
        if (preItem.isItemDamaged()) {
            return ItemStack.areItemsEqual(maidItem, preItem);
        }
        return ItemStack.areItemStacksEqual(maidItem, preItem);
    }
}
