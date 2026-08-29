package com.hite.kickmod;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KickPacket {
    private final BlockPos pos;
    private final boolean isSprinting;
    private final boolean isIronBlock;
    private final boolean hitEntity;
    private final int entityId;

    public KickPacket(BlockPos pos, boolean isSprinting, boolean isIronBlock) {
        this.pos = pos;
        this.isSprinting = isSprinting;
        this.isIronBlock = isIronBlock;
        this.hitEntity = false;
        this.entityId = -1;
    }

    public KickPacket(int entityId, boolean isSprinting) {
        this.pos = null;
        this.isSprinting = isSprinting;
        this.isIronBlock = false;
        this.hitEntity = true;
        this.entityId = entityId;
    }

    public static void encode(KickPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.hitEntity);
        if (packet.hitEntity) {
            buf.writeInt(packet.entityId);
            buf.writeBoolean(packet.isSprinting);
        } else {
            buf.writeBlockPos(packet.pos);
            buf.writeBoolean(packet.isSprinting);
            buf.writeBoolean(packet.isIronBlock);
        }
    }

    public static KickPacket decode(FriendlyByteBuf buf) {
        boolean hitEntity = buf.readBoolean();
        if (hitEntity) {
            return new KickPacket(buf.readInt(), buf.readBoolean());
        } else {
            return new KickPacket(buf.readBlockPos(), buf.readBoolean(), buf.readBoolean());
        }
    }

    public static void handle(KickPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getSender();
            if (player == null) return;

            if (packet.hitEntity) {
                handleEntityKick(player, packet);
            } else {
                handleBlockKick(player, packet);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleEntityKick(Player player, KickPacket packet) {
        Entity target = player.level().getEntity(packet.entityId);
        if (target instanceof LivingEntity living) {
            float damage = packet.isSprinting ? 3.0F : 1.5F;
            living.hurt(player.damageSources().playerAttack(player), damage);
            Vec3 dir = living.position().subtract(player.position()).normalize();
            double strength = packet.isSprinting ? 2.5 : 1.8;
            living.setDeltaMovement(dir.x * strength, 0.5, dir.z * strength);
            living.hurtMarked = true;
        }
    }

    private static void handleBlockKick(Player player, KickPacket packet) {
        Level level = player.level();
        BlockPos pos = packet.pos;
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (block == Blocks.BEDROCK) {
            player.hurt(player.damageSources().generic(), 1.0F);
            return;
        }

        if (block instanceof DoorBlock) {
            if (packet.isIronBlock) {
                KickHandler.destroyIronDoor(level, pos, state, player);
            } else {
                KickHandler.destroyDoor(level, pos, state, player);
            }
        } else if (block instanceof TrapDoorBlock) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(block.asItem()));
        } else if (KickHandler.isGlassBlock(block)) {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(block.asItem()));
        } else {
            level.removeBlock(pos, false);
            Block.popResource(level, pos, new net.minecraft.world.item.ItemStack(block.asItem()));
        }
    }
}