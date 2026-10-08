package com.elfmcys.yesstevemodel.client.animation.modern;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = YesSteveModel.MOD_ID, value = Side.CLIENT)
public final class ModelSounds {
    private static final Set<Audio> PLAYING = Collections.newSetFromMap(new ConcurrentHashMap<Audio, Boolean>());
    private static final Set<String> WARNED = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private ModelSounds() {}

    public static final class Context {
        private final List<Audio> sounds = new ArrayList<>();
        public void stop() {
            for (Audio sound : sounds) sound.done = true;
            sounds.clear();
        }
    }

    public static void stopAll() {
        for (Audio sound : PLAYING) sound.done = true;
        PLAYING.clear();
    }

    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
        for (Audio sound : PLAYING) {
            if (sound.done || Minecraft.getMinecraft().world != sound.owner.world || (++sound.age > 20 && !handler.isSoundPlaying(sound))) {
                sound.done = true;
                handler.stopSound(sound);
                PLAYING.remove(sound);
            }
        }
    }

    public static void play(ResourceLocation model, EntityPlayer player, String event, Context context) {
        if (player == null || Minecraft.getMinecraft().world != player.world) return;
        JsonObject parameters;
        if (event.startsWith("{")) parameters = YesSteveModel.GSON.fromJson(event, JsonObject.class);
        else {
            parameters = new JsonObject();
            parameters.addProperty("effect", event.startsWith("\"") ? YesSteveModel.GSON.fromJson(event, String.class) : event);
        }
        String effect = parameters.get("effect").getAsString();
        ModernAssets.Bundle assets = ModernAssets.MODELS.get(model);
        ResourceLocation id = assets == null ? null : assets.sounds.get(effect);
        boolean custom = id != null;
        if (!custom && effect.indexOf(':') >= 0) id = new ResourceLocation(effect);
        if (id == null) {
            if (WARNED.add(model + ":" + effect)) YesSteveModel.LOGGER.warn("Missing model sound {} in {}", effect, model);
            return;
        }
        float volume = parameters.has("volume") ? parameters.get("volume").getAsFloat() : 1;
        float pitch = parameters.has("pitch") ? parameters.get("pitch").getAsFloat() : 1;
        boolean loop = parameters.has("loop") && parameters.get("loop").getAsBoolean();
        if (custom) ModelSoundPack.INSTANCE.install();
        Audio sound = new Audio(id, player, custom, Math.max(0, volume), Math.max(0.01f, pitch), loop);
        context.sounds.removeIf(audio -> audio.done || !PLAYING.contains(audio));
        context.sounds.add(sound);
        PLAYING.add(sound);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
    }

    public static void playState(ResourceLocation model, EntityPlayer player, JsonElement events, Context context) {
        if (events == null) return;
        if (events.isJsonArray()) for (JsonElement event : events.getAsJsonArray()) play(model, player, event.toString(), context);
        else play(model, player, events.toString(), context);
    }

    private static final class Audio extends PositionedSound implements ITickableSound {
        private final EntityPlayer owner;
        private final boolean custom;
        private boolean done;
        private int age;

        Audio(ResourceLocation id, EntityPlayer player, boolean custom, float volume, float pitch, boolean loop) {
            super(id, SoundCategory.PLAYERS);
            this.owner = player;
            this.custom = custom;
            this.volume = volume;
            this.pitch = pitch;
            this.repeat = loop;
            updatePosition();
        }

        @Override public SoundEventAccessor createAccessor(SoundHandler handler) {
            if (!custom) return super.createAccessor(handler);
            this.sound = new Sound(positionedSoundLocation.toString(), 1, 1, 1, Sound.Type.FILE, false);
            SoundEventAccessor accessor = new SoundEventAccessor(positionedSoundLocation, null);
            accessor.addSound(this.sound);
            return accessor;
        }

        private void updatePosition() {
            xPosF = (float) owner.posX;
            yPosF = (float) owner.posY;
            zPosF = (float) owner.posZ;
        }

        @Override public void update() {
            if (Minecraft.getMinecraft().world != owner.world || owner.isDead) done = true;
            updatePosition();
            // SoundManager removes finished non-looping tickable sounds itself.
            if (done) PLAYING.remove(this);
        }

        @Override public boolean isDonePlaying() { return done; }
    }
}
