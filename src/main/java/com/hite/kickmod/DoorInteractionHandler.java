package com.hite.kickmod;

import com.hite.kickmod.client.KickOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = KickMod.MODID, value = Dist.CLIENT)
public class DoorInteractionHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();

        if (state.getBlock() instanceof DoorBlock) {
            if (ProtectedBlockHandler.isProtected(level, pos)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);

                String message = getLocalizedMessage();
                KickOverlay.showMessage(message, 0xFF5555, 80);
                level.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.5F, 1.5F);
            }
        }
    }

    private static String getLocalizedMessage() {
        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        switch (lang) {
            case "ru_ru": return "§cНЕЛЬЗЯ ВЫБИТЬ!";
            case "en_us": return "§cCAN'T BREAK!";
            default: return "§cCAN'T BREAK!";
        }
    }
}