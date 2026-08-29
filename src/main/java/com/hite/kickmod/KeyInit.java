package com.hite.kickmod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = KickMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyInit {
    public static final String KEY_CATEGORY = "key.category.kickmod";
    public static final String KEY_KICK = "key.kickmod.kick";

    public static final KeyMapping KICK_KEY = new KeyMapping(
        KEY_KICK,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_X,
        KEY_CATEGORY
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KICK_KEY);
    }
}