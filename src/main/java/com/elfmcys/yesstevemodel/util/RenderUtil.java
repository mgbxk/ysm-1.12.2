package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.client.ClientProxy;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.client.renderer.CustomPlayerRenderer;
import com.elfmcys.yesstevemodel.geckolib3.core.IAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.geo.GeoReplacedEntityRenderer;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/*
1.16.5 - 1.12.2
player.yBodyRot - player.renderYawOffset	身体转向角度
player.yRot - player.rotationYaw	视口偏航角
player.xRot	- player.rotationPitch	视口俯仰角
player.yHeadRot	- player.rotationYawHead	头部偏航角
player.yHeadRotO - player.prevRotationYawHead	上一刻头部偏航角
 */
public final class RenderUtil {
    private static boolean playerPreviewRender;

    public static boolean isPlayerPreviewRender() { return playerPreviewRender; }

    public static void renderTextureScreenEntity(float pPosX, float pPosY, float pScale, float pitch, float yaw, EntityPlayer player, ResourceLocation modelId, ResourceLocation textureId, boolean showGround, Consumer<CustomPlayerEntity> consumer) {
        if (player == null) {
            return;
        }
        try {
            CustomPlayerRenderer renderer = ClientProxy.getInstance();
            IAnimatable animatable = AnimatableCacheUtil.TEXTURE_GUI_CACHE.get(modelId, CustomPlayerEntity::new);
            if (animatable instanceof CustomPlayerEntity entity) {
                consumer.accept(entity);

                entity.setMainModel(ModelIdUtil.getMainId(modelId));
                entity.setTexture(textureId);

                GlStateManager.pushMatrix();
                GlStateManager.translate(pPosX, pPosY, 1050.0F);
                GlStateManager.scale(1.0F, 1.0F, -1.0F);

                /*
                动画位移矩阵开始
                 */
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0D, 0.0D, 1000.0D);
                GlStateManager.scale(pScale, pScale, pScale);
                GlStateManager.translate(0, 0.8, 0);
                GlStateManager.rotate(180.0F, 0, 0, 1);
                rotateAndEnableLighting();
                applyGuiLighting(ModelIdUtil.getMainId(modelId));
                float xp = -10 + pitch;
                GlStateManager.rotate(xp, 1, 0, 0);

                float yBodyRot = player.renderYawOffset;
                float yRot = player.rotationYaw;
                float xRot = player.rotationPitch;
                float yHeadRotO = player.prevRotationYawHead;
                float yHeadRot = player.rotationYawHead;

                player.renderYawOffset = -yaw;
                player.rotationYaw = 180;
                player.rotationPitch = 0;
                player.rotationYawHead = player.rotationYaw;
                player.prevRotationYawHead = player.rotationYaw;
                boolean sleeping = player.sleeping;
                BlockPos bedLocation = player.bedLocation;
                float renderOffsetX = player.renderOffsetX;
                float renderOffsetY = player.renderOffsetY;
                float renderOffsetZ = player.renderOffsetZ;

                RenderManager dispatcher = Minecraft.getMinecraft().getRenderManager();
                xp = 180.0F - xp;
                dispatcher.setPlayerViewY(xp);
                dispatcher.setRenderShadow(false);
                if (entity.hasPreviewAnimation("sleep")) {
                    GlStateManager.rotate(yaw - 90, 0, 1, 0);
                    GlStateManager.translate(0.5, 0.5625, 0);
                    player.sleeping = true;
                    player.bedLocation = null;
                    player.renderOffsetX = 0;
                    player.renderOffsetY = 0;
                    player.renderOffsetZ = 0;
                }
                if (entity.hasPreviewAnimation("swim") || entity.hasPreviewAnimation("swim_stand")) {
                    //player.setPose(Pose.SWIMMING);
                }
                if (entity.hasPreviewAnimation("sneak") || entity.hasPreviewAnimation("sneaking")) {
                    //player.setPose(Pose.CROUCHING);
                }
                if (entity.hasPreviewAnimation("sit")) {
                    GlStateManager.translate(0, -0.5, 0);
                }
                if (entity.hasPreviewAnimation("ride")) {
                    GlStateManager.translate(0, 0.85, 0);
                }
                if (entity.hasPreviewAnimation("ride_pig")) {
                    GlStateManager.translate(0, 0.3125, 0);
                }
                if (entity.hasPreviewAnimation("boat")) {
                    GlStateManager.translate(0, -0.45, 0);
                }
                GlStateManager.pushMatrix();
                if (player.getRidingEntity() instanceof EntityLivingBase vehicle) {
                    float vehicleYRot = vehicle.rotationYaw;
                    GlStateManager.rotate(vehicleYRot + yaw, 0, 1, 0);
                    player.rotationYawHead = vehicleYRot;
                    player.prevRotationYawHead = vehicleYRot;
                }
                renderer.render(player, entity, 0, 0, 0, 0.0F, 1.0F);
                GlStateManager.popMatrix();
                // 清理实体渲染
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableColorMaterial();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
                try {
                    renderExtraEntity(yaw, player, entity, dispatcher);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
                GlStateManager.popMatrix();
                /*
                动画位移矩阵结束
                 */
                if (showGround) {
                    if (entity.hasPreviewAnimation("sleep")) {
                        renderBed(pScale, pitch, yaw);
                    }
                    renderGround(pScale, pitch, yaw);
                }
                dispatcher.setRenderShadow(true);

                player.renderYawOffset = yBodyRot;
                player.rotationYaw = yRot;
                player.rotationPitch = xRot;
                player.prevRotationYawHead = yHeadRotO;
                player.rotationYawHead = yHeadRot;
                player.sleeping = sleeping;
                player.bedLocation = bedLocation;
                player.renderOffsetX = renderOffsetX;
                player.renderOffsetY = renderOffsetY;
                player.renderOffsetZ = renderOffsetZ;

                GlStateManager.popMatrix();

                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
                GlStateManager.disableTexture2D();
                GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void renderBed(float scale, float pitch, float yaw) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.0D, 1000.0D);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0, 0.8, 0);
        GlStateManager.rotate(180.0F, 0, 0, 1);
        GlStateManager.rotate(-10 + pitch, 1, 0, 0);

        GlStateManager.rotate(yaw + 180, 0, 1, 0);
        GlStateManager.translate(-0.5, 0, 0.5);
        ItemStack stack = new ItemStack(Items.BED, 1, EnumDyeColor.RED.getMetadata());
        stack.getItem().getTileEntityItemStackRenderer().renderByItem(stack);
        GlStateManager.popMatrix();
    }

    private static void renderGround(float scale, float pitch, float yaw) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.0D, 1000.0D);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(0, 0.8, 0);
        GlStateManager.rotate(180.0F, 0, 0, 1);
        GlStateManager.rotate(-10 + pitch, 1, 0, 0);

        GlStateManager.rotate(yaw, 0, 1, 0);
        GlStateManager.translate(-1.5, -1, -1.5);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                GlStateManager.translate(0, 0, 1);
                renderSingleBlock(Blocks.GRASS.getDefaultState());
            }
            GlStateManager.translate(1, 0, -3);
        }
        GlStateManager.translate(-1, 1, 1);
        renderSingleBlock(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS));
        GlStateManager.translate(0, 0, 1);
        renderSingleBlock(Blocks.RED_FLOWER.getDefaultState().withProperty(Blocks.RED_FLOWER.getTypeProperty(), BlockFlower.EnumFlowerType.RED_TULIP));
        GlStateManager.popMatrix();
    }

    private static void renderSingleBlock(IBlockState state) {
        GlStateManager.pushMatrix(); // 一定要 push，原版方块渲染不干净
        final Minecraft mc = Minecraft.getMinecraft();
        mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        mc.getBlockRendererDispatcher().renderBlockBrightness(state, 1.0F);
        GlStateManager.popMatrix();
    }

    @SuppressWarnings({"DataFlowIssue", "UnnecessaryReturnStatement"})
    private static void renderExtraEntity(float yaw, EntityPlayer player, CustomPlayerEntity playerEntity, RenderManager dispatcher) throws ExecutionException {
        if (playerEntity.hasPreviewAnimation("ride")) {
            Entity entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityList.getKey(EntityHorse.class), () -> new EntityHorse(player.world));
            renderExtraEntity(yaw, player, dispatcher, entity);
            return;
        }
        if (playerEntity.hasPreviewAnimation("ride_pig")) {
            Entity entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityList.getKey(EntityPig.class), () -> new EntityPig(player.world));
            renderExtraEntity(yaw, player, dispatcher, entity);
            return;
        }
        if (playerEntity.hasPreviewAnimation("boat")) {
            Entity entity = AnimatableCacheUtil.ENTITIES_CACHE.get(EntityList.getKey(EntityBoat.class), () -> new EntityBoat(player.world));
            renderExtraEntity(yaw, player, dispatcher, entity);
            return;
        }
    }

    private static void renderExtraEntity(float yaw, EntityPlayer player, RenderManager dispatcher, Entity entity) {
        GlStateManager.rotate(yaw, 0, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        dispatcher.renderEntity(entity, 0, -entity.getMountedYOffset() - player.getYOffset(), 0, 0.0F, 1.0F, false);
        // 清理实体渲染
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void renderEntityInInventory(int pPosX, int pPosY, int pScale, EntityPlayer player, ResourceLocation modelId, ResourceLocation textureId, Consumer<CustomPlayerEntity> consumer, boolean disableRot) {
        if (player == null) {
            return;
        }
        try {
            CustomPlayerRenderer renderer = ClientProxy.getInstance();
            IAnimatable animatable = AnimatableCacheUtil.ANIMATABLE_CACHE.get(modelId, CustomPlayerEntity::new);
            if (animatable instanceof CustomPlayerEntity entity) {
                consumer.accept(entity);
                renderModel(pPosX, pPosY, (float) pScale, player, modelId, textureId, renderer, entity, disableRot);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * {@link com.elfmcys.yesstevemodel.client.gui.button.TextureButton#renderWidget(Minecraft, int, int, float)}
     */
    @SuppressWarnings("JavadocReference")
    public static void renderTextureButtonEntity(int pPosX, int pPosY, int pScale, EntityPlayer player, ResourceLocation modelId, ResourceLocation textureId) {
        renderEntityInInventory(pPosX, pPosY, pScale, player, modelId, textureId, entity -> {
            if (entity.hasPreviewAnimation()) {
                entity.clearPreviewAnimation();
            }
        }, false);
    }

    private static void renderModel(
            double pPosX, double pPosY, float pScale, EntityPlayer player,
            ResourceLocation modelId, ResourceLocation textureId,
            GeoReplacedEntityRenderer<EntityPlayer, CustomPlayerEntity> renderer, CustomPlayerEntity entity,
            boolean disableRot
    ) {
        entity.setMainModel(ModelIdUtil.getMainId(modelId));
        entity.setTexture(textureId);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) pPosX, (float) pPosY, 1050.0F);
        GlStateManager.scale(1.0F, 1.0F, -1.0F);

        GlStateManager.translate(0.0D, disableRot ? 5.5D : 0.0D, 1000.0D);
        GlStateManager.scale(pScale, pScale, pScale);
        GlStateManager.rotate(180.0F, 0, 0, 1);
        rotateAndEnableLighting();
        applyGuiLighting(ModelIdUtil.getMainId(modelId));
        float xp = disableRot ? 0.0F : -10.0F;
        GlStateManager.rotate(xp, 1, 0, 0);

        float yBodyRot = player.renderYawOffset;
        float yBodyRotO = player.prevRenderYawOffset;
        float yRot = player.rotationYaw;
        float yRotO = player.prevRotationYaw;
        float xRot = player.rotationPitch;
        float xRotO = player.prevRotationPitch;
        float yHeadRot = player.rotationYawHead;
        float yHeadRotO = player.prevRotationYawHead;

        ItemStack[] itemStacks = new ItemStack[EntityEquipmentSlot.values().length];
        int i = 0;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            itemStacks[i] = player.getItemStackFromSlot(slot);
            if (slot == EntityEquipmentSlot.MAINHAND) {
                player.inventory.mainInventory.set(player.inventory.currentItem, ItemStack.EMPTY);
            } else if (slot == EntityEquipmentSlot.OFFHAND) {
                player.inventory.offHandInventory.set(0, ItemStack.EMPTY);
            } else {
                player.inventory.armorInventory.set(slot.getIndex(), ItemStack.EMPTY);
            }
            i++;
        }

        float renderYRot = disableRot ? 180.0F : 200.0F;
        player.renderYawOffset = renderYRot;
        player.prevRenderYawOffset = renderYRot;
        player.rotationYaw = renderYRot;
        player.prevRotationYaw = renderYRot;
        player.rotationPitch = 0.0F;
        player.prevRotationPitch = 0.0F;
        player.rotationYawHead = player.rotationYaw;
        player.prevRotationYawHead = player.rotationYaw;

        RenderManager dispatcher = Minecraft.getMinecraft().getRenderManager();
        xp = 180.0F - xp;
        dispatcher.setPlayerViewY(xp);
        dispatcher.setRenderShadow(false);
        GlStateManager.pushMatrix();
        if (player.getRidingEntity() instanceof EntityLivingBase vehicle) {
            float vehicleYRot = vehicle.rotationYaw;
            GlStateManager.rotate(vehicleYRot - renderYRot, 0, 1, 0);
            player.rotationYawHead = vehicleYRot;
            player.prevRotationYawHead = vehicleYRot;
        }
        renderer.render(player, entity, 0, 0, 0, 0.0F, 1.0F);
        GlStateManager.popMatrix();
        dispatcher.setRenderShadow(true);

        player.renderYawOffset = yBodyRot;
        player.prevRenderYawOffset = yBodyRotO;
        player.rotationYaw = yRot;
        player.prevRotationYaw = yRotO;
        player.rotationPitch = xRot;
        player.prevRotationPitch = xRotO;
        player.rotationYawHead = yHeadRot;
        player.prevRotationYawHead = yHeadRotO;

        i = 0;
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack itemStack = itemStacks[i];
            if (slot == EntityEquipmentSlot.MAINHAND) {
                player.inventory.mainInventory.set(player.inventory.currentItem, itemStack);
            } else if (slot == EntityEquipmentSlot.OFFHAND) {
                player.inventory.offHandInventory.set(0, itemStack);
            } else {
                player.inventory.armorInventory.set(slot.getIndex(), itemStack);
            }
            i++;
        }

        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    public static void renderPlayerEntity(EntityPlayer player, double posX, double posY, float scale, float yawOffset, int z) {
        if (player == null) return;
        boolean guiPreview = Minecraft.getMinecraft().currentScreen instanceof com.elfmcys.yesstevemodel.client.gui.PlayerModelScreen
                || Minecraft.getMinecraft().currentScreen instanceof com.elfmcys.yesstevemodel.client.gui.ModernModelConfigScreen;
        float body = player.renderYawOffset, previousBody = player.prevRenderYawOffset;
        float head = player.rotationYawHead, previousHead = player.prevRotationYawHead;
        float pitch = player.rotationPitch, previousPitch = player.prevRotationPitch;
        RenderManager renderDispatcher = Minecraft.getMinecraft().getRenderManager();
        float previousView = renderDispatcher.playerViewY;
        boolean previousShadow = renderDispatcher.isRenderShadow();
        boolean previousPreview = playerPreviewRender;
        boolean previousDepth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        GlStateManager.enableDepth();
        if (guiPreview) {
            player.renderYawOffset = player.prevRenderYawOffset = 180;
            player.rotationYawHead = player.prevRotationYawHead = 180;
            player.rotationPitch = player.prevRotationPitch = 0;
        }
        GlStateManager.enableColorMaterial();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) posX + scale * 0.5f, (float) posY + scale * 2, z);
        GlStateManager.scale(1, 1, -1);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.rotate(180.0F, 0, 0, 1);
        rotateAndEnableLighting();
        if (Minecraft.getMinecraft().currentScreen instanceof com.elfmcys.yesstevemodel.client.gui.ModernModelConfigScreen) {
            com.elfmcys.yesstevemodel.event.CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> applyGuiLighting(ModelIdUtil.getMainId(cap.getModelId())));
        }
        float yRot = player.renderYawOffset + yawOffset - 180;
        GlStateManager.rotate(yRot, 0, 1, 0);
        yRot = 180.0F - yRot;
        renderDispatcher.setPlayerViewY(yRot);
        renderDispatcher.setRenderShadow(false);
        try {
            playerPreviewRender = guiPreview;
            renderDispatcher.renderEntity(player, 0, 0, 0, 0.0F, 1.0F, false);
        } finally {
            playerPreviewRender = previousPreview;
            player.renderYawOffset = body; player.prevRenderYawOffset = previousBody;
            player.rotationYawHead = head; player.prevRotationYawHead = previousHead;
            player.rotationPitch = pitch; player.prevRotationPitch = previousPitch;
            renderDispatcher.setPlayerViewY(previousView);
            renderDispatcher.setRenderShadow(previousShadow);
            if (!previousDepth) GlStateManager.disableDepth();
            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    private static void applyGuiLighting(ResourceLocation main) {
        com.elfmcys.yesstevemodel.client.animation.modern.ModernAssets.Bundle bundle = com.elfmcys.yesstevemodel.client.animation.modern.ModernAssets.MODELS.get(main);
        if (bundle != null && bundle.options.guiNoLighting) GlStateManager.disableLighting();
    }

    private static void rotateAndEnableLighting() {
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
    }

    public static void scissor(int screenX, int screenY, int boxWidth, int boxHeight) {
        final Minecraft mc = Minecraft.getMinecraft();
        int scale = new ScaledResolution(mc).getScaleFactor();

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(
                screenX * scale,
                mc.displayHeight - (screenY * scale + boxHeight * scale),
                Math.max(0, boxWidth * scale),
                Math.max(0, boxHeight * scale)
        );
    }
}
