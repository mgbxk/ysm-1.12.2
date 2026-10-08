package com.elfmcys.yesstevemodel.client.gui.button;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.event.CapabilityEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class TextureCountButton extends FlatColorButton {
    public TextureCountButton(int x, int y) {
        super(x, y, 20, 20, "", (b) -> {
        });
    }

    @Override
    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        this.updateDisplayString();
        super.drawButton(mc, mouseX, mouseY, partialTicks);
    }

    private void updateDisplayString() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        CapabilityEvent.getModelInfoCap(player).ifPresent(cap -> {
            ResourceLocation modelId = cap.getModelId();
            if (ClientModelManager.MODELS.containsKey(modelId)) {
                this.displayString = String.valueOf(ClientModelManager.MODELS.get(modelId).size());
            }
        });
    }
}
