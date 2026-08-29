package com.hite.kickmod;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.HashSet;
import java.util.Set;

public class ProtectedBlockHandler {
    private static final Set<BlockPos> protectedBlocks = new HashSet<>();

    public static BlockPos getLowerPart(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof DoorBlock) {
            DoubleBlockHalf half = state.getValue(DoorBlock.HALF);
            if (half == DoubleBlockHalf.UPPER) {
                return pos.below();
            }
        }
        return pos;
    }

    public static boolean isProtected(Level level, BlockPos pos) {
        BlockPos lowerPos = getLowerPart(level, pos);
        return protectedBlocks.contains(lowerPos);
    }

    public static void addProtection(Level level, BlockPos pos) {
        BlockPos lowerPos = getLowerPart(level, pos);
        protectedBlocks.add(lowerPos);
        KickMod.LOGGER.info("Block protected at lower part: " + lowerPos);
    }

    public static void removeProtection(Level level, BlockPos pos) {
        BlockPos lowerPos = getLowerPart(level, pos);
        if (protectedBlocks.remove(lowerPos)) {
            KickMod.LOGGER.info("Protection removed at lower part: " + lowerPos);
        } else {
            KickMod.LOGGER.info("Attempted to remove protection at " + lowerPos + " but it wasn't protected");
        }
    }

    public static void clearAllProtections() {
        protectedBlocks.clear();
        KickMod.LOGGER.info("All protections cleared");
    }
}