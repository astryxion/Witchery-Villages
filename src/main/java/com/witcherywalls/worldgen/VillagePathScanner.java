package com.witcherywalls.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fallback layout detection for villages whose structure pieces are unavailable.
 * Intentionally much cheaper than a full 160-block volume scan: loaded chunks only,
 * surface columns only, and a single pass.
 */
public final class VillagePathScanner
{
    private static final int SCAN_RADIUS = 80;
    private static final int SURFACE_RANGE = 2;
    private static final int EXPANSION_X = 20;
    private static final int EXPANSION_Z = 7;
    private static final int MIN_COMPONENT_BLOCKS = 4;
    private static final int MAX_COMPONENT_DISTANCE = 64;
    private static final int MAX_BOUNDS_SEGMENTS = 24;
    private static final int MIN_PATH_BLOCKS = 8;
    private static final int PROBE_RADIUS = 40;
    private static final int PROBE_STEP = 2;

    private VillagePathScanner()
    {
    }

    public static ScanResult scan(Level level, BlockPos center)
    {
        if (!looksLikeVillage(level, center))
        {
            return ScanResult.none();
        }

        Set<BlockPos> pathBlocks = collectPathBlocks(level, center);
        if (pathBlocks.size() < MIN_PATH_BLOCKS)
        {
            return ScanResult.none();
        }

        List<VillageWallGenerator.StructureBounds> bounds = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        long ySum = 0;
        int yCount = 0;
        long radiusSquared = (long) MAX_COMPONENT_DISTANCE * MAX_COMPONENT_DISTANCE;

        for (BlockPos start : pathBlocks)
        {
            if (visited.contains(start))
            {
                continue;
            }

            Set<BlockPos> component = new HashSet<>();
            floodFillHorizontal(start, pathBlocks, visited, component);

            if (component.size() < MIN_COMPONENT_BLOCKS || !isNearCenter(component, center, radiusSquared))
            {
                continue;
            }

            int[] bbox = computeBounds(component);
            bounds.add(new VillageWallGenerator.StructureBounds(
                    bbox[0], bbox[1], bbox[2], bbox[3], bbox[4], bbox[5], EXPANSION_X, EXPANSION_Z));

            for (BlockPos pos : component)
            {
                if (pos.distSqr(center) <= radiusSquared)
                {
                    ySum += pos.getY();
                    yCount++;
                }
            }

            if (bounds.size() >= MAX_BOUNDS_SEGMENTS)
            {
                break;
            }
        }

        if (bounds.isEmpty())
        {
            return ScanResult.none();
        }

        int averageY = yCount == 0
                ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, center.getX(), center.getZ())
                : (int) (ySum / yCount);
        return new ScanResult(true, bounds, averageY);
    }

    public static boolean isPathBlock(Level level, BlockPos pos)
    {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.DIRT_PATH)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.SMOOTH_SANDSTONE)
                || state.is(Blocks.CUT_SANDSTONE)
                || state.is(Blocks.SMOOTH_SANDSTONE_SLAB)
                || state.is(Blocks.SANDSTONE_SLAB))
        {
            return true;
        }

        if (state.is(Blocks.SAND))
        {
            return isSandStreet(level, pos);
        }

        if (state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE))
        {
            return isStreetStone(level, pos);
        }

        if (state.is(BlockTags.PLANKS))
        {
            return isPlankStreet(level, pos);
        }

        if (state.getBlock() instanceof SlabBlock && state.is(BlockTags.WOODEN_SLABS))
        {
            return isPlankStreet(level, pos.below()) || isPlankStreet(level, pos.above());
        }

        return false;
    }

    public static boolean isProtectedFromWall(BlockState state)
    {
        if (isVillageStructureBlock(state))
        {
            return true;
        }

        return state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(Blocks.GLASS_PANE)
                || state.is(Blocks.GLASS)
                || state.is(Blocks.CRAFTING_TABLE)
                || state.is(Blocks.FURNACE)
                || state.is(Blocks.CHEST)
                || state.is(Blocks.TRAPPED_CHEST)
                || state.is(BlockTags.TRAPDOORS)
                || state.is(Blocks.LADDER)
                || state.is(Blocks.BRICKS)
                || state.is(Blocks.STONE_BRICKS);
    }

    private static boolean looksLikeVillage(Level level, BlockPos center)
    {
        int pathCount = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = center.getX() - PROBE_RADIUS; x <= center.getX() + PROBE_RADIUS; x += PROBE_STEP)
        {
            for (int z = center.getZ() - PROBE_RADIUS; z <= center.getZ() + PROBE_RADIUS; z += PROBE_STEP)
            {
                if (!level.hasChunk(x >> 4, z >> 4))
                {
                    continue;
                }

                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                for (int y = surfaceY - 1; y <= surfaceY; y++)
                {
                    if (isPathBlock(level, cursor.set(x, y, z)))
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

    private static boolean isStreetStone(Level level, BlockPos pos)
    {
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.is(Blocks.DIRT_PATH)
                    || neighbor.is(Blocks.GRAVEL)
                    || neighbor.is(Blocks.COBBLESTONE)
                    || neighbor.is(Blocks.MOSSY_COBBLESTONE)
                    || neighbor.is(BlockTags.PLANKS))
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isPlankStreet(Level level, BlockPos pos)
    {
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.PLANKS))
        {
            return false;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.is(Blocks.DIRT_PATH)
                    || neighbor.is(Blocks.GRAVEL)
                    || neighbor.is(Blocks.COBBLESTONE)
                    || neighbor.is(BlockTags.PLANKS)
                    || neighbor.getBlock() instanceof SlabBlock)
            {
                return true;
            }
        }

        return false;
    }

    private static boolean isSandStreet(Level level, BlockPos pos)
    {
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockState neighbor = level.getBlockState(pos.relative(direction));
            if (neighbor.is(Blocks.DIRT_PATH)
                    || neighbor.is(Blocks.GRAVEL)
                    || neighbor.is(Blocks.SMOOTH_SANDSTONE)
                    || neighbor.is(Blocks.CUT_SANDSTONE)
                    || neighbor.is(Blocks.SMOOTH_SANDSTONE_SLAB)
                    || neighbor.is(Blocks.SANDSTONE_SLAB))
            {
                return true;
            }
        }

        return false;
    }

    private static int[] computeBounds(Set<BlockPos> blocks)
    {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : blocks)
        {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        return new int[] {minX, minY, minZ, maxX, maxY, maxZ};
    }

    private static boolean isVillageStructureBlock(BlockState state)
    {
        return state.is(Blocks.HAY_BLOCK)
                || state.is(Blocks.BELL)
                || state.is(Blocks.BARREL)
                || state.is(Blocks.COMPOSTER)
                || state.is(Blocks.LECTERN)
                || state.is(Blocks.GRINDSTONE)
                || state.is(Blocks.SMITHING_TABLE)
                || state.is(Blocks.STONECUTTER)
                || state.is(Blocks.LOOM)
                || state.is(Blocks.CARTOGRAPHY_TABLE)
                || state.is(Blocks.FLETCHING_TABLE)
                || state.is(Blocks.BLAST_FURNACE)
                || state.is(Blocks.SMOKER)
                || state.is(Blocks.CAULDRON)
                || state.is(Blocks.WATER_CAULDRON)
                || state.is(Blocks.LAVA_CAULDRON)
                || state.is(Blocks.POWDER_SNOW_CAULDRON)
                || state.is(BlockTags.BEDS)
                || state.is(BlockTags.DOORS)
                || state.is(BlockTags.FENCES)
                || state.is(BlockTags.WOODEN_PRESSURE_PLATES)
                || state.is(Blocks.TORCH)
                || state.is(Blocks.WALL_TORCH)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)
                || state.is(Blocks.FARMLAND)
                || state.is(Blocks.WHEAT)
                || state.is(Blocks.CARROTS)
                || state.is(Blocks.POTATOES)
                || state.is(Blocks.BEETROOTS);
    }

    private static Set<BlockPos> collectPathBlocks(Level level, BlockPos center)
    {
        Set<BlockPos> pathBlocks = new HashSet<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = center.getX() - SCAN_RADIUS; x <= center.getX() + SCAN_RADIUS; x++)
        {
            for (int z = center.getZ() - SCAN_RADIUS; z <= center.getZ() + SCAN_RADIUS; z++)
            {
                if (!level.hasChunk(x >> 4, z >> 4))
                {
                    continue;
                }

                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                for (int y = surfaceY - SURFACE_RANGE; y <= surfaceY + SURFACE_RANGE; y++)
                {
                    if (isPathBlock(level, cursor.set(x, y, z)))
                    {
                        pathBlocks.add(cursor.immutable());
                    }
                }
            }
        }

        return pathBlocks;
    }

    private static boolean isNearCenter(Set<BlockPos> component, BlockPos center, long radiusSquared)
    {
        for (BlockPos pos : component)
        {
            if (pos.distSqr(center) <= radiusSquared)
            {
                return true;
            }
        }

        return false;
    }

    private static void floodFillHorizontal(BlockPos start, Set<BlockPos> pathBlocks, Set<BlockPos> visited, Set<BlockPos> component)
    {
        List<BlockPos> queue = new ArrayList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty())
        {
            BlockPos current = queue.remove(queue.size() - 1);
            component.add(current);

            BlockPos north = current.north();
            BlockPos south = current.south();
            BlockPos east = current.east();
            BlockPos west = current.west();
            tryVisit(north, pathBlocks, visited, queue);
            tryVisit(south, pathBlocks, visited, queue);
            tryVisit(east, pathBlocks, visited, queue);
            tryVisit(west, pathBlocks, visited, queue);
        }
    }

    private static void tryVisit(BlockPos neighbor, Set<BlockPos> pathBlocks, Set<BlockPos> visited, List<BlockPos> queue)
    {
        if (!visited.contains(neighbor) && pathBlocks.contains(neighbor))
        {
            visited.add(neighbor);
            queue.add(neighbor);
        }
    }

    public record ScanResult(boolean found, List<VillageWallGenerator.StructureBounds> bounds, int averageY)
    {
        private static ScanResult none()
        {
            return new ScanResult(false, List.of(), 0);
        }
    }
}
