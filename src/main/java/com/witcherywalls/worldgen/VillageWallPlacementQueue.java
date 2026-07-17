package com.witcherywalls.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.Queue;

public final class VillageWallPlacementQueue
{
    private static final int BLOCKS_PER_TICK = 2500;
    private static final Queue<PendingBlock> PENDING = new ArrayDeque<>();

    private VillageWallPlacementQueue()
    {
    }

    public static void enqueue(Level level, BlockPos pos, BlockState state)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            level.setBlock(pos, state, 3);
            return;
        }

        PENDING.add(new PendingBlock(serverLevel, pos.immutable(), state));
    }

    public static void tick()
    {
        int placed = 0;

        while (!PENDING.isEmpty() && placed < BLOCKS_PER_TICK)
        {
            PendingBlock pending = PENDING.poll();
            pending.level.setBlock(pending.pos, pending.state, 3);
            placed++;
        }
    }

    public static boolean isBusy()
    {
        return !PENDING.isEmpty();
    }

    private record PendingBlock(ServerLevel level, BlockPos pos, BlockState state)
    {
    }
}
