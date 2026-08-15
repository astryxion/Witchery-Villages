package com.witcherywalls.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class VillageWallScheduler
{
    private static final List<PendingGeneration> PENDING = new ArrayList<>();
    private static final int DELAY_TICKS = 100;
    private static final int VILLAGE_DEDUP_RADIUS = 80;
    private static final int MAX_STARTS_PER_TICK = 1;

    private VillageWallScheduler()
    {
    }

    public static void schedule(ServerLevel level, BlockPos center)
    {
        if (VillageWallSavedData.get(level).isNearProcessed(center))
        {
            return;
        }

        long radiusSquared = (long) VILLAGE_DEDUP_RADIUS * VILLAGE_DEDUP_RADIUS;
        for (PendingGeneration pending : PENDING)
        {
            if (pending.level == level && pending.center.distSqr(center) <= radiusSquared)
            {
                return;
            }
        }

        long executeTick = level.getServer().getTickCount() + DELAY_TICKS;
        PENDING.add(new PendingGeneration(level, center.immutable(), executeTick));
    }

    public static void tick(MinecraftServer server)
    {
        VillageWallPlacementQueue.tick();
        VillageWallDeferred.tick(server);
        VillageGuardSpawnQueue.tick();

        long currentTick = server.getTickCount();

        List<PendingGeneration> ready = new ArrayList<>();
        Iterator<PendingGeneration> iterator = PENDING.iterator();

        while (iterator.hasNext())
        {
            PendingGeneration pending = iterator.next();

            if (currentTick >= pending.executeTick)
            {
                ready.add(pending);
                iterator.remove();
            }
        }

        int started = 0;
        for (PendingGeneration pending : ready)
        {
            if (started >= MAX_STARTS_PER_TICK || VillageWallPlacementQueue.isBusy())
            {
                pending.executeTick = currentTick + 20;
                PENDING.add(pending);
                continue;
            }

            if (VillageWallService.tryGenerate(pending.level, pending.center))
            {
                started++;
            }
        }
    }

    private static class PendingGeneration
    {
        private final ServerLevel level;
        private final BlockPos center;
        private long executeTick;

        private PendingGeneration(ServerLevel level, BlockPos center, long executeTick)
        {
            this.level = level;
            this.center = center;
            this.executeTick = executeTick;
        }
    }
}
