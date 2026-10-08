package com.elfmcys.yesstevemodel.client.animation.modern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** A persistent in-memory pack; custom sounds survive F3+T resource reloads. */
public final class ModelSoundPack implements IResourcePack {
    public static final String DOMAIN = "ysm_model_sounds";
    public static final ModelSoundPack INSTANCE = new ModelSoundPack();
    private final Map<ResourceLocation, byte[]> data = new ConcurrentHashMap<>();
    private boolean installed;

    public void put(ResourceLocation sound, byte[] bytes) {
        data.put(new ResourceLocation(DOMAIN, "sounds/" + sound.getPath() + ".ogg"), bytes);
    }

    public void clear() { data.clear(); }

    public synchronized void install() {
        if (installed) return;
        Minecraft mc = Minecraft.getMinecraft();
        List<IResourcePack> packs = ReflectionHelper.getPrivateValue(FMLClientHandler.class, FMLClientHandler.instance(), "resourcePackList");
        packs.add(this);
        ((SimpleReloadableResourceManager) mc.getResourceManager()).reloadResourcePack(this);
        installed = true;
    }

    @Override public InputStream getInputStream(ResourceLocation location) throws FileNotFoundException {
        byte[] bytes = data.get(location);
        if (bytes == null) throw new FileNotFoundException(location.toString());
        return new ByteArrayInputStream(bytes);
    }
    @Override public boolean resourceExists(ResourceLocation location) { return data.containsKey(location); }
    @Override public Set<String> getResourceDomains() { return Collections.singleton(DOMAIN); }
    @Override public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer serializer, String section) { return null; }
    @Override public BufferedImage getPackImage() { return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB); }
    @Override public String getPackName() { return "YSM model sounds"; }
}
