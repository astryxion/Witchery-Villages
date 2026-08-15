package com.witcherywalls.worldgen;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.config.WitcheryWallsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Random;

public final class VillageWallService
{
    private VillageWallService()
    {
    }

    public static boolean tryGenerate(ServerLevel level, BlockPos center)
    {
        VillageWallSavedData data = VillageWallSavedData.get(level);

        if (data.isNearProcessed(center))
        {
            return false;
        }

        long startedAt = System.nanoTime();

        VillageStructureLocator.LocatedVillage located = VillageStructureLocator.find(level, center);
        if (located == null)
        {
            WitcheryWallsMod.getLogger().debug("Skipping wall generation at {} - not inside a village structure", center);
            return false;
        }

        if (located.abandoned())
        {
            data.markProcessed(center);
            WitcheryWallsMod.getLogger().info("Skipping abandoned village at {}", center);
            return true;
        }

        List<VillageWallGenerator.StructureBounds> bounds = located.bounds();
        int groundY = center.getY();
        boolean desert = located.desert();
        Random chanceRandom = new Random(level.getSeed() ^ center.asLong() ^ 0x57414C4CL);
        boolean placeWall = WitcheryWallsConfig.roll(chanceRandom, WitcheryWallsConfig.WALL_SPAWN_CHANCE);

        VillageBuildingService.place(level, center, located);

        if (placeWall)
        {
            if (!VillageWallGenerator.placeWalls(level, bounds, center.getX(), groundY, center.getZ(), desert))
            {
                WitcheryWallsMod.getLogger().warn("Failed to generate village walls around {}", center);
                return false;
            }
        }
        else
        {
            WitcheryWallsMod.getLogger().info("Skipped village walls at {} ({}% chance)",
                    center, WitcheryWallsConfig.WALL_SPAWN_CHANCE.get());
        }

        data.markProcessed(center);
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
        if (placeWall)
        {
            WitcheryWallsMod.getLogger().info("Queued village walls around {} via structure ({} segments, {} ms)",
                    center, bounds.size(), elapsedMs);
        }
        return true;
    }
}
