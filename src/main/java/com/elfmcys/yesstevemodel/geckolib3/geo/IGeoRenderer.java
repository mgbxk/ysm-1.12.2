package com.elfmcys.yesstevemodel.geckolib3.geo;

import com.elfmcys.yesstevemodel.geckolib3.core.util.Color;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.*;
import com.elfmcys.yesstevemodel.geckolib3.model.provider.GeoModelProvider;
import com.elfmcys.yesstevemodel.geckolib3.util.EModelRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.IRenderCycle;
import com.elfmcys.yesstevemodel.geckolib3.util.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

@SuppressWarnings("unused")
public interface IGeoRenderer<T> {
    MatrixStack MATRIX_STACK = new MatrixStack();
    String GLOW_PREFIX = "ysmGlow";

    @SuppressWarnings("rawtypes")
    GeoModelProvider getGeoModelProvider();

    ResourceLocation getEntityTexture(T entity);

    @Nullable
    default GeoModel getGeoModel() {
        return null;
    }

    default void render(
            GeoModel model, T entity, float partialTicks,
            float red, float green, float blue, float alpha
    ) {
        this.renderEarly(entity, partialTicks, red, green, blue, alpha);
        this.renderLate(entity, partialTicks, red, green, blue, alpha);

        Tessellator tess = Tessellator.getInstance();
        tess.getBuffer().begin(GL11.GL_QUADS, com.elfmcys.yesstevemodel.client.texture.OptifinePbr.vertexFormat(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL));
        // 渲染所有根骨骼
        for (GeoBone group : model.topLevelBones) {
            this.renderRecursively(group, tess, red, green, blue, alpha);
        }
        tess.draw();
        // 由于此时我们至少渲染了一次，因此让我们将循环设置为重复
        this.setCurrentModelRenderCycle(EModelRenderCycle.REPEATED);
    }

    default void renderRecursively(
            GeoBone bone, Tessellator tess,
            float red, float green, float blue, float alpha
    ) {
        MATRIX_STACK.push();
        MATRIX_STACK.prep(bone);
        BufferBuilder buffer = tess.getBuffer();
        if (bone.getName().startsWith(GLOW_PREFIX)) {
            // 先绘制出已有的顶点
            tess.draw();
            // 设置自发光条件
            boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
            GlStateManager.disableLighting();
            float lastX = OpenGlHelper.lastBrightnessX;
            float lastY = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
            // 绘制特殊的顶点
            buffer.begin(GL11.GL_QUADS, com.elfmcys.yesstevemodel.client.texture.OptifinePbr.vertexFormat(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL));
            this.renderCubesOfBone(bone, buffer, red, green, blue, alpha);
            tess.draw();
            // 恢复状态
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
            if (lighting) GlStateManager.enableLighting();
            else GlStateManager.disableLighting();
            // 重新开始绘制
            buffer.begin(GL11.GL_QUADS, com.elfmcys.yesstevemodel.client.texture.OptifinePbr.vertexFormat(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL));
        } else {
            this.renderCubesOfBone(bone, buffer, red, green, blue, alpha);
        }
        this.renderChildBones(bone, tess, red, green, blue, alpha);
        MATRIX_STACK.pop();
    }

    default void renderCubesOfBone(
            GeoBone bone, BufferBuilder buffer,
            float red, float green, float blue, float alpha
    ) {
        if (bone.isHidden()) {
            return;
        }
        for (GeoCube cube : bone.childCubes) {
            if (!bone.cubesAreHidden()) {
                MATRIX_STACK.push();
                this.renderCube(buffer, cube, red, green, blue, alpha);
                MATRIX_STACK.pop();
            }
        }
    }

    default void renderChildBones(
            GeoBone bone, Tessellator tess,
            float red, float green, float blue, float alpha
    ) {
        if (bone.childBonesAreHiddenToo()) {
            return;
        }
        for (GeoBone childBone : bone.childBones) {
            this.renderRecursively(childBone, tess, red, green, blue, alpha);
        }
    }

    default void renderCube(BufferBuilder builder, GeoCube cube, float red, float green, float blue, float alpha) {
        MATRIX_STACK.moveToPivot(cube);
        MATRIX_STACK.rotate(cube);
        MATRIX_STACK.moveBackFromPivot(cube);
        for (GeoQuad quad : cube.quads) {
            if (quad == null) {
                continue;
            }
            Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
            MATRIX_STACK.getNormalMatrix().transform(normal);
            if ((cube.size.y == 0 || cube.size.z == 0) && normal.getX() < 0) {
                normal.x *= -1;
            }
            if ((cube.size.x == 0 || cube.size.z == 0) && normal.getY() < 0) {
                normal.y *= -1;
            }
            if ((cube.size.x == 0 || cube.size.y == 0) && normal.getZ() < 0) {
                normal.z *= -1;
            }
            this.createVerticesOfQuad(quad, normal, builder, red, green, blue, alpha);
        }
    }

    default void createVerticesOfQuad(
            GeoQuad quad, Vector3f normal, BufferBuilder builder,
            float red, float green, float blue, float alpha
    ) {
        for (GeoVertex vertex : quad.vertices) {
            Vector4f vector4f = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1);
            MATRIX_STACK.getModelMatrix().transform(vector4f);
            if (builder.getVertexFormat().getSize() == 56) {
                // OptiFine's extended format stores color before UV, unlike the legacy format.
                builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).color(red, green, blue, alpha)
                        .tex(vertex.textureU, vertex.textureV).normal(normal.getX(), normal.getY(), normal.getZ()).endVertex();
            } else {
                builder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ()).tex(vertex.textureU, vertex.textureV)
                        .color(red, green, blue, alpha).normal(normal.getX(), normal.getY(), normal.getZ()).endVertex();
            }
        }
    }

    default void renderEarly(T entity, float ticks, float red, float green, float blue, float partialTicks) {
        if (this.getCurrentModelRenderCycle() == EModelRenderCycle.INITIAL) {
            float width = this.getWidthScale(entity);
            float height = this.getHeightScale(entity);
            GlStateManager.scale(width, height, width);
        }
    }

    default void renderLate(T entity, float ticks, float red, float green, float blue, float partialTicks) {
    }

    default Color getRenderColor(T entity, float partialTicks) {
        return Color.WHITE;
    }

    default int getInstanceId(T entity) {
        return entity.hashCode();
    }

    @Nonnull
    default IRenderCycle getCurrentModelRenderCycle() {
        return EModelRenderCycle.INITIAL;
    }

    default void setCurrentModelRenderCycle(IRenderCycle cycle) {
    }

    default float getWidthScale(T entity) {
        return 1F;
    }

    default float getHeightScale(T entity) {
        return 1F;
    }
}
