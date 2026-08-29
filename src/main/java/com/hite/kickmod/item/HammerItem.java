package com.hite.kickmod.item;

import com.hite.kickmod.ProtectedBlockHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class HammerItem extends Item {
    public HammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 endPos = eyePos.add(lookVec.x * 4.0, lookVec.y * 4.0, lookVec.z * 4.0);

        BlockHitResult hit = level.clip(new ClipContext(eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hit.getBlockPos();
            BlockState state = level.getBlockState(pos);

            if (canProtectBlock(state.getBlock())) {
                if (ProtectedBlockHandler.isProtected(level, pos)) {
                    ProtectedBlockHandler.removeProtection(level, pos);
                    player.sendSystemMessage(Component.literal("§a[KickMod] You removed protection!"));
                    level.players().forEach(p -> {
                        if (p != player) {
                            p.sendSystemMessage(Component.literal("§e" + player.getName().getString() + " §aremoved protection from a door!"));
                        }
                    });
                    level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.6F, 1.2F);
                } else {
                    ProtectedBlockHandler.addProtection(level, pos);
                    player.sendSystemMessage(Component.literal("§a[KickMod] You protected the door!"));
                    level.players().forEach(p -> {
                        if (p != player) {
                            p.sendSystemMessage(Component.literal("§e" + player.getName().getString() + " §cprotected a door with a hammer! §f(cannot be kicked)"));
                        }
                    });
                    level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.8F, 1.0F);
                    if (!player.isCreative()) {
                        stack.hurtAndBreak(1, player, (p) -> {});
                    }
                }
            } else {
                player.displayClientMessage(Component.literal("§e[KickMod] This block cannot be protected!"), false);
                level.playSound(null, pos, SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        } else {
            player.displayClientMessage(Component.literal("§e[KickMod] Aim at a door, trapdoor, or glass!"), false);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    private boolean canProtectBlock(net.minecraft.world.level.block.Block block) {
        return block instanceof DoorBlock ||
               block instanceof TrapDoorBlock ||
               KickHandler.isGlassBlock(block);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§7Right-click on door/trapdoor/glass:"));
        tooltip.add(Component.literal("§7- Protect block (cannot be kicked)"));
        tooltip.add(Component.literal("§6§oConsumes durability when applying protection"));
        tooltip.add(Component.literal("§a§oRight-click on protected block to remove protection"));
    }
}