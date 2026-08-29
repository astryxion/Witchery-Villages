package com.witcherywalls.worldgen;

import com.witcherywalls.config.WitcheryWallsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Places 6 street guards like 1.20.1 VillageBuildingService.spawnStreetGuards.
 */
public final class VillageStreetGuardSpawner
{
    public static final int STREET_GUARD_COUNT = 6;
    private static final int STREET_GUARD_MIN_DISTANCE = 12;
    private static final int MIN_PATH_BLOCKS = 8;

    private VillageStreetGuardSpawner()
    {
    }

    public static int spawn(World world, Village village, Random random)
    {
        if (!WitcheryWallsConfig.spawnStreetGuards())
        {
            return 0;
        }

        List<BlockPos> paths = findPathBlocks(world, village);
        if (paths.size() < MIN_PATH_BLOCKS)
        {
            return -1;
        }

        Collections.shuffle(paths, random);
        List<BlockPos> chosen = new ArrayList<>();
        int minDist = STREET_GUARD_MIN_DISTANCE;
        while (chosen.size() < STREET_GUARD_COUNT && minDist >= 4)
        {
            chosen.clear();
            for (BlockPos path : paths)
            {
                if (chosen.size() >= STREET_GUARD_COUNT)
                {
                    break;
                }

                BlockPos spawn = path.up();
                if (tooClose(spawn, chosen, minDist))
                {
                    continue;
                }
                chosen.add(spawn);
            }
            minDist -= 4;
        }

        for (BlockPos pos : chosen)
        {
            StructureGuardSpawner.spawnAt(world, pos);
        }
        return chosen.size();
    }

    private static boolean tooClose(BlockPos pos, List<BlockPos> chosen, int minDist)
    {
        int minDistSq = minDist * minDist;
        for (BlockPos other : chosen)
        {
            int dx = pos.getX() - other.getX();
            int dz = pos.getZ() - other.getZ();
            if (dx * dx + dz * dz < minDistSq)
            {
                return true;
            }
        }
        return false;
    }

    private static List<BlockPos> findPathBlocks(World world, Village village)
    {
        BlockPos center = village.getCenter();
        int radius = Math.max(32, village.getVillageRadius());
        List<BlockPos> paths = new ArrayList<>();

        for (int x = center.getX() - radius; x <= center.getX() + radius; x++)
        {
            for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++)
            {
                if (!world.isBlockLoaded(new BlockPos(x, center.getY(), z)))
                {
                    continue;
                }

                BlockPos floor = findPathFloor(world, x, z);
                if (floor != null)
                {
                    paths.add(floor);
                }
            }
        }
        return paths;
    }

    private static BlockPos findPathFloor(World world, int x, int z)
    {
        int top = world.getHeight(x, z);
        for (int y = top; y >= top - 12; y--)
        {
            BlockPos floor = new BlockPos(x, y, z);
            if (isPathBlock(world.getBlockState(floor)))
            {
                return floor;
            }
        }
        return null;
    }

    private static boolean isPathBlock(IBlockState state)
    {
        Block block = state.getBlock();
        return block == Blocks.GRAVEL
                || block == Blocks.GRASS_PATH
                || block == Blocks.SANDSTONE;
    }
}
