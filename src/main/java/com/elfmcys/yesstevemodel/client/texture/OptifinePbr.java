package com.elfmcys.yesstevemodel.client.texture;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.model.modern.PbrImages;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.VertexFormat;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;

/** Optional OptiFine MultiTex bridge, with no required OptiFine dependency. */
public final class OptifinePbr {
    private static boolean warned;
    private static Method shadersEnabled;
    private static VertexFormat shaderFormat;
    private static boolean checkedFormat;
    private OptifinePbr() {}

    public static VertexFormat vertexFormat(VertexFormat original) {
        if (!checkedFormat) {
            checkedFormat = true;
            try {
                Class<?> config;
                try { config = Class.forName("Config"); } catch (ClassNotFoundException oldName) { config = Class.forName("net.optifine.Config"); }
                shadersEnabled = config.getMethod("isShaders");
                Class<?> formats;
                try { formats = Class.forName("shadersmod.client.SVertexFormat"); } catch (ClassNotFoundException oldName) { formats = Class.forName("net.optifine.shaders.SVertexFormat"); }
                shaderFormat = (VertexFormat) formats.getMethod("makeDefVertexFormatItem").invoke(null);
            } catch (ReflectiveOperationException | LinkageError unavailable) { shaderFormat = null; }
        }
        try {
            if (shaderFormat != null && Boolean.TRUE.equals(shadersEnabled.invoke(null))) return shaderFormat;
        } catch (ReflectiveOperationException ignored) { }
        return original;
    }

    public static void upload(AbstractTexture texture, BufferedImage base, byte[] normal, byte[] specular) {
        if (normal == null && specular == null) return;
        try {
            Class<?> shadersTex;
            try { shadersTex = Class.forName("net.optifine.shaders.ShadersTex"); }
            catch (ClassNotFoundException newerName) { shadersTex = Class.forName("shadersmod.client.ShadersTex"); }
            Object multi = null;
            for (Method method : shadersTex.getMethods()) {
                if (method.getName().equals("getMultiTexID") && method.getParameterTypes().length == 1
                        && method.getParameterTypes()[0].isInstance(texture)) {
                    multi = method.invoke(null, texture);
                    break;
                }
            }
            if (multi == null) throw new ReflectiveOperationException("OptiFine getMultiTexID unavailable");
            int normalId = multi.getClass().getField("norm").getInt(multi);
            int specularId = multi.getClass().getField("spec").getInt(multi);
            TextureUtil.uploadTextureImageAllocate(normalId, PbrImages.layer(normal, base.getWidth(), base.getHeight(), 0xff7f7fff), false, false);
            TextureUtil.uploadTextureImageAllocate(specularId, PbrImages.layer(specular, base.getWidth(), base.getHeight(), 0x00000000), false, false);
        } catch (ClassNotFoundException e) {
            if (!warned) {
                warned = true;
                YesSteveModel.LOGGER.info("Model PBR maps require OptiFine and a PBR-capable shader; displaying base textures");
            }
        } catch (Exception | LinkageError e) {
            if (!warned) {
                warned = true;
                YesSteveModel.LOGGER.warn("Cannot upload model PBR maps to OptiFine", e);
            }
        } finally {
            // Normal/specular uploads change the bound texture. Restore the base ID.
            net.minecraft.client.renderer.GlStateManager.bindTexture(texture.getGlTextureId());
        }
    }
}
