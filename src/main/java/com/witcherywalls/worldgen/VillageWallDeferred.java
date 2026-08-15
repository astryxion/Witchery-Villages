package com.witcherywalls.worldgen;

import com.witcherywalls.WitcheryWallsMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.concurrent.ConcurrentLinkedQueue;

public final class VillageWallDeferred
{
    private static final int COLUMNS_PER_TICK = 48;
    private static final ConcurrentLinkedQueue<LoadedChunk> LOADED_CHUNKS = new ConcurrentLinkedQueue<>();

    private VillageWallDeferred()
    {
    }

    public static void add(ServerLevel level, VillageWallPlan plan)
    {
        VillageWallSavedData.get(level).addPlan(plan);
        WitcheryWallsMod.getLogger().info("Deferred {} wall columns until nearby chunks load", plan.remaining());
    }

    public static void notifyChunkLoaded(ServerLevel level, ChunkPos pos)
    {
        if (VillageWallSavedData.get(level).hasPlans())
        {
            LOADED_CHUNKS.add(new LoadedChunk(level, pos));
        }
    }

    public static void tick(MinecraftServer server)
    {
        LoadedChunk loaded;
        while ((loaded = LOADED_CHUNKS.poll()) != null)
        {
            resumeChunk(loaded.level, loaded.pos);
        }

        for (ServerLevel level : server.getAllLevels())
        {
            VillageWallSavedData data = VillageWallSavedData.get(level);
            if (!data.hasPlans())
            {
                continue;
            }

            int budget = COLUMNS_PER_TICK;
            for (VillageWallPlan plan : data.getPlans())
            {
                if (budget <= 0)
                {
                    break;
                }
                budget -= VillageWallGenerator.resume(level, plan, budget);
            }
            data.removeCompletedPlans();
        }
    }

    private static void resumeChunk(ServerLevel level, ChunkPos pos)
    {
        VillageWallSavedData data = VillageWallSavedData.get(level);
        for (VillageWallPlan plan : data.getPlans())
        {
            if (plan.intersects(pos))
            {
                VillageWallGenerator.resume(level, plan, COLUMNS_PER_TICK);
            }
        }
        data.removeCompletedPlans();
    }

    private record LoadedChunk(ServerLevel level, ChunkPos pos)
    {
    }
}
