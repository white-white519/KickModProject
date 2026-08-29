package com.hite.kickmod;

import com.hite.kickmod.client.KickOverlay;
import com.hite.kickmod.client.PlayerAnimationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = KickMod.MODID)
public class KickHandler {
    private static int kickCooldown = 0;
    private static int consecutiveKicks = 0;
    private static long lastKickTime = 0;

    private static final Map<BlockPos, Integer> ironBlockHits = new HashMap<>();

    private static Block currentTargetBlock = null;
    private static Entity currentTargetEntity = null;
    private static long lastTargetTime = 0;

    private static final int COOLDOWN_TICKS = 60; // 3 секунды
    private static final int IRON_BLOCK_HITS = 4;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        if (kickCooldown > 0) {
            kickCooldown--;
        }

        if (System.currentTimeMillis() - lastKickTime > 1000) {
            consecutiveKicks = 0;
        }

        if (KeyInit.KICK_KEY.consumeClick()) {
            KickMod.LOGGER.info("Kick key pressed! (X)");
            performKick();
        }

        updateTarget();
    }

    private static void updateTarget() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) {
            currentTargetBlock = null;
            currentTargetEntity = null;
            return;
        }

        double reachDistance = 4.0;
        Vec3 eyePos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getViewVector(1.0F);
        AABB searchBox = player.getBoundingBox().expandTowards(lookVec.scale(reachDistance)).inflate(1.0);
        List<Entity> entities = level.getEntities(player, searchBox, e -> e instanceof LivingEntity && e != player);

        Entity closestEntity = null;
        double closestDistance = reachDistance;

        for (Entity entity : entities) {
            AABB entityBox = entity.getBoundingBox().inflate(0.3);
            Vec3 hitPos = entityBox.clip(eyePos, eyePos.add(lookVec.scale(reachDistance))).orElse(null);
            if (hitPos != null) {
                double distance = eyePos.distanceTo(hitPos);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }

        if (closestEntity != null) {
            currentTargetEntity = closestEntity;
            currentTargetBlock = null;
            lastTargetTime = System.currentTimeMillis();
            return;
        } else {
            currentTargetEntity = null;
        }

        HitResult hit = player.pick(4.0, 1.0F, false);
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            Block block = level.getBlockState(pos).getBlock();
            if (canBreakBlock(block)) {
                currentTargetBlock = block;
                currentTargetEntity = null;
                lastTargetTime = System.currentTimeMillis();
            } else {
                currentTargetBlock = null;
            }
        } else {
            currentTargetBlock = null;
        }
    }

    public static Block getCurrentTargetBlock() {
        if (System.currentTimeMillis() - lastTargetTime > 500) return null;
        return currentTargetBlock;
    }

    public static Entity getCurrentTargetEntity() {
        if (System.currentTimeMillis() - lastTargetTime > 500) return null;
        return currentTargetEntity;
    }

    public static int getRequiredHits(Block block) {
        if (block instanceof DoorBlock && isIronBlock(block)) {
            return IRON_BLOCK_HITS;
        }
        if (block instanceof TrapDoorBlock && isIronBlock(block)) {
            return IRON_BLOCK_HITS;
        }
        return 1;
    }

    public static boolean isIronBlock(Block block) {
        String name = block.getName().getString().toLowerCase();
        return name.contains("iron") || name.contains("железн");
    }

    public static boolean isGlassBlock(Block block) {
        return block == Blocks.GLASS ||
               block == Blocks.WHITE_STAINED_GLASS ||
               block == Blocks.ORANGE_STAINED_GLASS ||
               block == Blocks.MAGENTA_STAINED_GLASS ||
               block == Blocks.LIGHT_BLUE_STAINED_GLASS ||
               block == Blocks.YELLOW_STAINED_GLASS ||
               block == Blocks.LIME_STAINED_GLASS ||
               block == Blocks.PINK_STAINED_GLASS ||
               block == Blocks.GRAY_STAINED_GLASS ||
               block == Blocks.LIGHT_GRAY_STAINED_GLASS ||
               block == Blocks.CYAN_STAINED_GLASS ||
               block == Blocks.PURPLE_STAINED_GLASS ||
               block == Blocks.BLUE_STAINED_GLASS ||
               block == Blocks.BROWN_STAINED_GLASS ||
               block == Blocks.GREEN_STAINED_GLASS ||
               block == Blocks.RED_STAINED_GLASS ||
               block == Blocks.BLACK_STAINED_GLASS ||
               block == Blocks.GLASS_PANE ||
               block == Blocks.WHITE_STAINED_GLASS_PANE ||
               block == Blocks.ORANGE_STAINED_GLASS_PANE ||
               block == Blocks.MAGENTA_STAINED_GLASS_PANE ||
               block == Blocks.LIGHT_BLUE_STAINED_GLASS_PANE ||
               block == Blocks.YELLOW_STAINED_GLASS_PANE ||
               block == Blocks.LIME_STAINED_GLASS_PANE ||
               block == Blocks.PINK_STAINED_GLASS_PANE ||
               block == Blocks.GRAY_STAINED_GLASS_PANE ||
               block == Blocks.LIGHT_GRAY_STAINED_GLASS_PANE ||
               block == Blocks.CYAN_STAINED_GLASS_PANE ||
               block == Blocks.PURPLE_STAINED_GLASS_PANE ||
               block == Blocks.BLUE_STAINED_GLASS_PANE ||
               block == Blocks.BROWN_STAINED_GLASS_PANE ||
               block == Blocks.GREEN_STAINED_GLASS_PANE ||
               block == Blocks.RED_STAINED_GLASS_PANE ||
               block == Blocks.BLACK_STAINED_GLASS_PANE;
    }

    public static boolean canBreakBlock(Block block) {
        if (block == Blocks.OAK_PLANKS ||
            block == Blocks.SPRUCE_PLANKS ||
            block == Blocks.BIRCH_PLANKS ||
            block == Blocks.JUNGLE_PLANKS ||
            block == Blocks.ACACIA_PLANKS ||
            block == Blocks.DARK_OAK_PLANKS ||
            block == Blocks.MANGROVE_PLANKS ||
            block == Blocks.CHERRY_PLANKS ||
            block == Blocks.BAMBOO_PLANKS ||
            block == Blocks.CRIMSON_PLANKS ||
            block == Blocks.WARPED_PLANKS) {
            return true;
        }

        if (isGlassBlock(block)) return true;
        if (block instanceof DoorBlock) return true;
        if (block instanceof TrapDoorBlock) return true;

        return false;
    }

    private static void performKick() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;

        if (player == null || level == null) return;

        if (kickCooldown > 0) {
            int secondsLeft = (kickCooldown / 20) + 1;
            KickMod.LOGGER.info("Kick on cooldown! Wait " + secondsLeft + " seconds");
            player.playSound(SoundEvents.SHIELD_BLOCK, 0.5F, 1.0F);
            return;
        }

        boolean isSprinting = player.isSprinting();
        int maxConsecutive = isSprinting ? 2 : 1;

        if (consecutiveKicks >= maxConsecutive) {
            KickMod.LOGGER.info("Too many kicks in a row! Wait a moment.");
            player.playSound(SoundEvents.SHIELD_BLOCK, 0.5F, 1.0F);
            kickCooldown = 20;
            return;
        }

        float pitch = player.getXRot();
        boolean lookingDown = pitch > 60;

        if (player instanceof AbstractClientPlayer clientPlayer) {
            PlayerAnimationHandler.playAnimation(clientPlayer, lookingDown);
            KickMod.LOGGER.info("Triggering body animation: " + (lookingDown ? "kick_down" : "kick_forward"));
        }

        Entity targetEntity = getCurrentTargetEntity();

        if (targetEntity != null && targetEntity instanceof LivingEntity) {
            KickMod.LOGGER.info("Kicked entity: " + targetEntity.getName().getString());
            NetworkHandler.INSTANCE.sendToServer(new KickPacket(targetEntity.getId(), isSprinting));
            player.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.0F);

            consecutiveKicks++;
            lastKickTime = System.currentTimeMillis();
            kickCooldown = COOLDOWN_TICKS;
            return;
        }

        BlockPos targetPos;
        BlockState state;
        Block block;

        if (lookingDown) {
            targetPos = player.blockPosition().below();
            state = level.getBlockState(targetPos);
            block = state.getBlock();
        } else {
            HitResult hit = player.pick(4.0, 1.0F, false);
            if (hit.getType() != HitResult.Type.BLOCK) {
                KickMod.LOGGER.info("Kicked nothing");
                player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.5F, 1.0F);
                kickCooldown = COOLDOWN_TICKS / 2;
                return;
            }
            targetPos = ((BlockHitResult) hit).getBlockPos();
            state = level.getBlockState(targetPos);
            block = state.getBlock();
        }

        KickMod.LOGGER.info("Kicked block: " + block.getName().getString());

        if (ProtectedBlockHandler.isProtected(level, targetPos)) {
            String message = getLocalizedKickMessage();
            KickOverlay.showMessage(message, 0xFF5555, 80);
            player.playSound(SoundEvents.SHIELD_BLOCK, 0.5F, 1.5F);
            kickCooldown = COOLDOWN_TICKS / 2;
            return;
        }

        if (block == Blocks.BEDROCK) {
            player.hurt(player.damageSources().generic(), 1.0F);
            player.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.5F);
            kickCooldown = COOLDOWN_TICKS;
            return;
        }

        if (!canBreakBlock(block)) {
            player.playSound(SoundEvents.SHIELD_BLOCK, 0.5F, 0.5F);
            kickCooldown = COOLDOWN_TICKS / 2;
            return;
        }

        NetworkHandler.INSTANCE.sendToServer(new KickPacket(targetPos, isSprinting,
            (block instanceof DoorBlock && isIronBlock(block)) ||
            (block instanceof TrapDoorBlock && isIronBlock(block))));

        playLocalEffects(level, targetPos, state, player, isSprinting, block);

        consecutiveKicks++;
        lastKickTime = System.currentTimeMillis();
        kickCooldown = COOLDOWN_TICKS;
    }

    private static String getLocalizedKickMessage() {
        String lang = Minecraft.getInstance().getLanguageManager().getSelected();
        switch (lang) {
            case "ru_ru": return "§cНЕЛЬЗЯ ВЫБИТЬ!";
            case "en_us": return "§cCAN'T BREAK!";
            case "zh_cn": return "§c无法踢碎！";
            case "zh_tw": return "§c無法踢碎！";
            case "de_de": return "§cKANN NICHT EINTRETEN!";
            case "fr_fr": return "§cIMPOSSIBLE D'ENFONCER!";
            case "es_es": return "§cNO SE PUEDE PATEAR!";
            case "pt_br": return "§cNÃO PODE CHUTAR!";
            case "ja_jp": return "§c蹴れない！";
            case "ko_kr": return "§c찰 수 없음!";
            case "kk_kz": return "§cТЕПУ МҮМКІН ЕМЕС!";
            case "tg_tj": return "§cЛАТТ ЗАДАН МУМКИН НЕСТ!";
            default: return "§cCAN'T BREAK!";
        }
    }

    private static void playLocalEffects(Level level, BlockPos pos, BlockState state, Player player, boolean isSprinting, Block block) {
        player.playSound(SoundEvents.PLAYER_ATTACK_KNOCKBACK, 1.0F, 1.0F);
        level.levelEvent(2001, pos, Block.getId(state));

        if (isGlassBlock(block)) {
            level.playSound(null, pos, SoundEvents.GLASS_HIT, SoundSource.BLOCKS, 0.8F, 1.2F);
            for (int i = 0; i < 10; i++) {
                double x = pos.getX() + 0.5 + (Math.random() - 0.5) * 0.8;
                double y = pos.getY() + 0.5 + (Math.random() - 0.5) * 0.8;
                double z = pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.8;
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), x, y, z, 0, 0, 0);
            }
        } else if (block instanceof DoorBlock && isIronBlock(block)) {
            level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5F, 1.5F);
            for (int i = 0; i < 8; i++) {
                double x = pos.getX() + 0.5 + (Math.random() - 0.5) * 0.8;
                double y = pos.getY() + 0.5 + (Math.random() - 0.5) * 0.8;
                double z = pos.getZ() + 0.5 + (Math.random() - 0.5) * 0.8;
                level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0.05, 0);
            }
        } else if (block instanceof DoorBlock) {
            level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.8F, 1.0F);
        } else if (block instanceof TrapDoorBlock) {
            level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.8F, 1.0F);
        }

        pushPlayer(player, isSprinting ? 0.5 : 0.3);
    }

    private static void pushPlayer(Player player, double strength) {
        Vec3 look = player.getViewVector(1.0F);
        Vec3 push = look.scale(strength);
        player.push(push.x, push.y, push.z);
    }

    public static void destroyDoor(Level level, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf half = state.getValue(DoorBlock.HALF);
        BlockPos otherPart = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherPart);

        ItemStack doorStack = new ItemStack(state.getBlock().asItem());

        ProtectedBlockHandler.removeProtection(level, pos);
        ProtectedBlockHandler.removeProtection(level, otherPart);

        level.removeBlock(pos, false);
        level.removeBlock(otherPart, false);
        Block.popResource(level, pos, doorStack);

        level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.levelEvent(2001, pos, Block.getId(state));
        level.levelEvent(2001, otherPart, Block.getId(otherState));

        level.updateNeighborsAt(pos, Blocks.AIR);
        level.updateNeighborsAt(otherPart, Blocks.AIR);

        level.sendBlockUpdated(pos, state, Blocks.AIR.defaultBlockState(), 3);
        level.sendBlockUpdated(otherPart, otherState, Blocks.AIR.defaultBlockState(), 3);

        KickMod.LOGGER.info("Door destroyed at {} and {}", pos, otherPart);
        pushPlayer(player, 0.5);
    }

    public static void destroyIronDoor(Level level, BlockPos pos, BlockState state, Player player) {
        destroyDoor(level, pos, state, player);
    }
}