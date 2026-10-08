package com.elfmcys.yesstevemodel.model.modern;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/** Keeps LabPBR channels intact. Maps must share the base texture's dimensions. */
public final class PbrImages {
    private PbrImages() {}

    public static BufferedImage decode(byte[] bytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        if (image == null) throw new IOException("Invalid PBR/base image");
        return image;
    }

    public static void validate(byte[] base, byte[] normal, byte[] specular) throws IOException {
        BufferedImage image = decode(base);
        for (byte[] map : new byte[][]{normal, specular}) {
            if (map == null) continue;
            BufferedImage layer = decode(map);
            if (layer.getWidth() != image.getWidth() || layer.getHeight() != image.getHeight()) {
                throw new IOException("PBR map dimensions must match the uv texture");
            }
        }
    }

    public static BufferedImage layer(byte[] data, int width, int height, int fallback) throws IOException {
        if (data != null) return decode(data);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];
        java.util.Arrays.fill(pixels, fallback);
        image.setRGB(0, 0, width, height, pixels, 0, width);
        return image;
    }
}
