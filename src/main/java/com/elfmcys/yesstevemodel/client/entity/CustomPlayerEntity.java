package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.client.animation.AnimationManager;
import com.elfmcys.yesstevemodel.client.animation.modern.ModernPlayerController;
import com.elfmcys.yesstevemodel.client.model.CustomPlayerModel;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.PlayState;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationBuilder;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationData;
import com.elfmcys.yesstevemodel.geckolib3.core.manager.AnimationFactory;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.geckolib3.util.GeckoLibUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;

import static com.elfmcys.yesstevemodel.util.ControllerUtils.*;

public class CustomPlayerEntity implements IAnimatable {
    private final AnimationFactory factory = GeckoLibUtil.createFactory(this, true);
    private final ItemStack[] handItemsForAnimation = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY};
    private ResourceLocation mainModel = CustomPlayerModel.DEFAULT_MAIN_MODEL;
    private ResourceLocation texture = CustomPlayerModel.DEFAULT_TEXTURE;
    private String previewAnimation = "";
    private EntityPlayer player = null;

    @Nonnull
    private static <P extends IAnimatable> PlayState playLoopAnimation(AnimationEvent<P> event, String animationName) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation(animationName, ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        // 越往后优先级越高
        AnimationManager manager = AnimationManager.getInstance();
        ModernPlayerController.EntityState state = new ModernPlayerController.EntityState();
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("pre_parallel_%d_controller", i);
            String animationName = String.format("pre_parallel%d", i);
            data.addAnimationController(new ModernPlayerController(this, controllerName, "player.pre_parallel_" + i, 0, e -> manager.predicateParallel(e, animationName), state));
        }
        addOptional(data, "pre_main", state);
        data.addAnimationController(new ModernPlayerController(this, MAIN_CONTROLLER, "player.main", 2, manager::predicateMain, state));
        addOptional(data, "post_main", state);
        addOptional(data, "pre_hold", state);
        data.addAnimationController(new ModernPlayerController(this, HOLD_OFFHAND_CONTROLLER, "player.hold_offhand", 0, manager::predicateOffhandHold, state));
        data.addAnimationController(new ModernPlayerController(this, HOLD_MAINHAND_CONTROLLER, "player.hold_mainhand", 0, manager::predicateMainhandHold, state));
        addOptional(data, "post_hold", state);
        addOptional(data, "pre_swing", state);
        data.addAnimationController(new ModernPlayerController(this, SWING_CONTROLLER, "player.swing", 2, manager::predicateSwing, state));
        addOptional(data, "post_swing", state);
        addOptional(data, "pre_use", state);
        data.addAnimationController(new ModernPlayerController(this, USE_CONTROLLER, "player.use", 2, manager::predicateUse, state));
        addOptional(data, "post_use", state);
        for (int i = 0; i < 8; i++) {
            String controllerName = String.format("parallel_%d_controller", i);
            String animationName = String.format("parallel%d", i);
            data.addAnimationController(new ModernPlayerController(this, controllerName, "player.parallel_" + i, 0, e -> manager.predicateParallel(e, animationName), state));
        }
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                String controllerName = String.format("%s_controller", slot.getName());
                data.addAnimationController(new ModernPlayerController(this, controllerName, "player.armor_" + slot.getName(), 0, e -> manager.predicateArmor(e, slot), state));
            }
        }
        data.addAnimationController(new ModernPlayerController(this, CAP_CONTROLLER, "legacy.cap", 2, manager::predicateCap, state));
    }

    private void addOptional(AnimationData data, String slot, ModernPlayerController.EntityState state) {
        data.addAnimationController(new ModernPlayerController(this, slot + "_controller", "player." + slot, 0, e -> PlayState.STOP, state));
    }

    public ResourceLocation getMainModel() {
        if (GeckoLibCache.getInstance().getGeoModels().containsKey(this.mainModel)) {
            return this.mainModel;
        }
        return CustomPlayerModel.DEFAULT_MAIN_MODEL;
    }

    public void setMainModel(ResourceLocation mainModel) {
        this.mainModel = mainModel;
    }

    public ResourceLocation getAnimation() {
        if (GeckoLibCache.getInstance().getAnimations().containsKey(this.mainModel)) {
            return this.mainModel;
        }
        return CustomPlayerModel.DEFAULT_MAIN_ANIMATION;
    }

    public float getHeightScale() {
        if (ClientModelManager.SCALE_INFO.containsKey(this.mainModel)) {
            return ClientModelManager.SCALE_INFO.get(this.mainModel).getLeft().floatValue();
        }
        return 0.7f;
    }

    public float getWidthScale() {
        if (ClientModelManager.SCALE_INFO.containsKey(this.mainModel)) {
            return ClientModelManager.SCALE_INFO.get(this.mainModel).getRight().floatValue();
        }
        return 0.7f;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public void setPlayer(EntityPlayer player) {
        this.player = player;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    public ResourceLocation getTexture() {
        return this.texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public String getPreviewAnimation() {
        return this.previewAnimation;
    }

    public void setPreviewAnimation(String previewAnimation) {
        this.previewAnimation = previewAnimation;
    }

    public void clearPreviewAnimation() {
        this.previewAnimation = "";
    }

    public boolean hasPreviewAnimation() {
        return StringUtils.isNoneBlank(this.previewAnimation);
    }

    public boolean hasPreviewAnimation(String previewAnimation) {
        return this.hasPreviewAnimation() && previewAnimation.equals(this.previewAnimation);
    }

    public ItemStack[] getHandItemsForAnimation() {
        return this.handItemsForAnimation;
    }
}
