package com.elfmcys.yesstevemodel.client.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class OuterFileTexture extends AbstractTexture {
    private final byte[] data;
    private final byte[] normal;
    private final byte[] specular;

    public OuterFileTexture(byte[] data) {
        this(data, null, null);
    }

    public OuterFileTexture(byte[] data, byte[] normal, byte[] specular) {
        this.data = data;
        this.normal = normal;
        this.specular = specular;
    }

    @Override
    public void loadTexture(@Nonnull IResourceManager resourceManager) {
        this.deleteGlTexture();

        BufferedImage bufferedimage = null;
        try (ByteArrayInputStream is = new ByteArrayInputStream(this.data)) {
            bufferedimage = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (bufferedimage != null) {
            TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufferedimage, false, false);
            OptifinePbr.upload(this, bufferedimage, this.normal, this.specular);
        }
    }
}
