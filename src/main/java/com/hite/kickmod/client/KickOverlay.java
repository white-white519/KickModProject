package com.hite.kickmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "kickmod", value = Dist.CLIENT)
public class KickOverlay {
    private static String message = "";
    private static int messageTimer = 0;
    private static int messageColor = 0xFF5555;

    public static void showMessage(String text, int color, int duration) {
        message = text;
        messageColor = color;
        messageTimer = duration;
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (messageTimer <= 0) return;
        messageTimer--;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        GuiGraphics guiGraphics = event.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int textWidth = font.width(message);
        int x = (screenWidth - textWidth) / 2;
        int y = screenHeight / 2 - 40;

        float alpha = Math.min(1.0f, messageTimer / 20f);
        int alphaInt = (int)(alpha * 255);
        int colorWithAlpha = (alphaInt << 24) | (messageColor & 0xFFFFFF);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        guiGraphics.drawString(font, message, x + 1, y + 1, 0xAA000000, false);
        guiGraphics.drawString(font, message, x, y, colorWithAlpha, false);
        RenderSystem.disableBlend();
    }
}