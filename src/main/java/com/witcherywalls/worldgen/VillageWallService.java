package com.witcherywalls.worldgen;

import com.witcherywalls.WitcheryWallsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Random;

public final class VillageWallService
{
    private static final int MIN_PATH_BLOCKS = 8;
    private static final float WALL_GENERATION_CHANCE = 0.5f;

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

        if (!looksLikeVillage(level, center))
        {
            WitcheryWallsMod.getLogger().debug("Skipping wall generation at {} - not enough village paths nearby", center);
            return false;
        }

        List<VillageWallGenerator.StructureBounds> bounds = VillagePathScanner.scanPaths(level, center);

        if (bounds.isEmpty())
        {
            WitcheryWallsMod.getLogger().debug("Skipping wall generation at {} - no path bounds found", center);
            return false;
        }

        Random random = new Random(level.getSeed() ^ center.asLong() ^ 0x57414C4CL);
        if (random.nextFloat() >= WALL_GENERATION_CHANCE)
        {
            WitcheryWallsMod.getLogger().debug("Skipping wall generation at {} - random roll failed", center);
            data.markProcessed(center);
            return false;
        }

        boolean desert = level.getBiome(center).unwrapKey()
                .map(key -> key.location().getPath().contains("desert"))
                .orElse(false);

        int groundY = VillagePathScanner.getAveragePathY(level, center);

        if (!VillageWallGenerator.placeWalls(level, bounds, center.getX(), groundY, center.getZ(), desert))
        {
            WitcheryWallsMod.getLogger().warn("Failed to generate village walls around {}", center);
            return false;
        }

        data.markProcessed(center);
        WitcheryWallsMod.getLogger().info("Queued village walls around {} ({} path segments)", center, bounds.size());
        return true;
    }

    private static boolean looksLikeVillage(ServerLevel level, BlockPos center)
    {
        int pathCount = 0;
        int radius = 40;

        for (int x = center.getX() - radius; x <= center.getX() + radius; x++)
        {
            for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++)
            {
                int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, x, z);
                for (int y = surfaceY - 4; y <= surfaceY + 4; y++)
                {
                    if (VillagePathScanner.isPathBlock(level, new BlockPos(x, y, z)))
                    {
                        pathCount++;
                        if (pathCount >= MIN_PATH_BLOCKS)
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
