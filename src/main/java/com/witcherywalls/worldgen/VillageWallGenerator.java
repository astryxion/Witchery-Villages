package com.witcherywalls.worldgen;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.entity.EntityVillageGuard;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import java.util.List;

public final class VillageWallGenerator
{
    private VillageWallGenerator()
    {
    }

    public static void placeWalls(World world, List<StructureBounds> boundsList, int xCoord, int yCoord, int zCoord, Biome biome, boolean desert)
    {
        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        WitcheryWallsMod.getLogger().debug("Generating town walls at [{}, {}, {}]", xCoord, yCoord, zCoord);

        for (StructureBounds bounds : boundsList)
        {
            minX = Math.min(bounds.minX, minX);
            minZ = Math.min(bounds.minZ, minZ);
            maxX = Math.max(bounds.maxX, maxX);
            maxZ = Math.max(bounds.maxZ, maxZ);
        }

        if (maxX == Integer.MIN_VALUE || minX == Integer.MAX_VALUE || maxZ == Integer.MIN_VALUE || minZ == Integer.MAX_VALUE)
        {
            return;
        }

        byte[][] grid = new byte[maxX - minX + 3][maxZ - minZ + 3];
        short[][] heights = new short[maxX - minX + 3][maxZ - minZ + 3];
        short[][] foundations = new short[maxX - minX + 3][maxZ - minZ + 3];

        for (StructureBounds bounds : boundsList)
        {
            int w = bounds.maxX - bounds.minX + 1;
            int wMid = w / 2 + bounds.minX - 1;
            int h = bounds.maxZ - bounds.minZ + 1;
            int hMid = h / 2 + bounds.minZ - 1;

            for (int m = bounds.minX; m <= bounds.maxX; m++)
            {
                for (int z = bounds.minZ; z <= bounds.maxZ; z++)
                {
                    int mx = m - minX + 1;
                    int mz = z - minZ + 1;

                    if (!bounds.ew && (z == bounds.minZ || z == bounds.maxZ) && m >= wMid - 1 && m <= wMid + 1)
                    {
                        grid[mx][mz] = 3;
                    }
                    else if (bounds.ew && (m == bounds.minX || m == bounds.maxX) && z >= hMid - 1 && z <= hMid + 1)
                    {
                        grid[mx][mz] = 3;
                    }
                    else
                    {
                        grid[mx][mz] = 2;
                    }
                }
            }
        }

        int range = 7;
        for (int x = 1; x < grid.length - range; x++)
        {
            for (int z = 1; z < grid[x].length - range; z++)
            {
                if (grid[x][z] == 2)
                {
                    for (int p = 1; p < range; p++)
                    {
                        if (grid[x + p][z] == 2 && grid[x + p - 1][z] == 0)
                        {
                            for (int p2 = p; p2 > 0; p2--)
                            {
                                grid[x + p2][z] = 2;
                            }
                        }
                        if (grid[x][z + p] == 2 && grid[x][z + p - 1] == 0)
                        {
                            for (int p2 = p; p2 > 0; p2--)
                            {
                                grid[x][z + p2] = 2;
                            }
                        }
                    }
                }
            }
        }

        for (int x = 1; x < grid.length - 1; x++)
        {
            for (int z = 1; z < grid[x].length - 1; z++)
            {
                boolean n = grid[x][z - 1] == 0;
                boolean s = grid[x][z + 1] == 0;
                boolean e = grid[x + 1][z] == 0;
                boolean w = grid[x - 1][z] == 0;
                boolean ne = grid[x + 1][z - 1] == 0;
                boolean sw = grid[x - 1][z + 1] == 0;
                boolean se = grid[x + 1][z + 1] == 0;
                boolean nw = grid[x - 1][z - 1] == 0;

                if (!n && !s && !e && !w && !ne && !se && !nw && !sw)
                {
                    grid[x][z] = 1;
                }
            }
        }

        Block blockBase = Blocks.STONEBRICK;
        Block blockFence = Blocks.OAK_FENCE;
        Block stairsBlock = Blocks.STONE_BRICK_STAIRS;
        int blockBaseMeta = 0;

        if (desert)
        {
            blockBase = Blocks.SANDSTONE;
            stairsBlock = Blocks.SANDSTONE_STAIRS;
            blockBaseMeta = 2;
        }

        for (int k = 1; k < grid.length - 1; k++)
        {
            for (int z = 1; z < grid[k].length - 1; z++)
            {
                if (grid[k][z] >= 2)
                {
                    int dx = minX + k;
                    int dz = minZ + z;
                    int foundation = findFoundationY(world, dx, dz, yCoord);
                    int startY = foundation + 9;

                    foundations[k][z] = (short) Math.min(Math.max(foundation, 0), 32767);
                    heights[k][z] = (short) Math.min(Math.max(startY, 0), 32767);
                }
            }
        }

        for (int k = 1; k < grid.length - 1; k++)
        {
            for (int z = 1; z < grid[k].length - 1; z++)
            {
                if (grid[k][z] >= 2 && foundations[k][z] > 0)
                {
                    int dx = minX + k;
                    int dz = minZ + z;

                    if (hasFluidAt(world, dx, dz, foundations[k][z]))
                    {
                        int nearFoundation = Math.max(Math.max(foundations[k - 1][z], foundations[k + 1][z]),
                                Math.max(foundations[k][z + 1], foundations[k][z - 1]));

                        if (nearFoundation > foundations[k][z])
                        {
                            foundations[k][z] = (short) nearFoundation;
                            heights[k][z] = (short) Math.min(Math.max(nearFoundation + 9, 0), 32767);
                        }
                    }
                }
            }
        }

        for (int smooth = 0; smooth < 6; smooth++)
        {
            for (int k = 1; k < grid.length - 1; k++)
            {
                for (int z = 1; z < grid[k].length - 1; z++)
                {
                    if (grid[k][z] >= 2 && heights[k][z] > 0)
                    {
                        int near = Math.max(Math.max(heights[k - 1][z], heights[k + 1][z]), Math.max(heights[k][z + 1], heights[k][z - 1]));

                        if (near > 0)
                        {
                            int startY = heights[k][z];

                            if (near > startY)
                            {
                                startY = near - 1;
                            }
                            else if (near < startY)
                            {
                                startY = near + 1;
                            }

                            heights[k][z] = (short) Math.min(Math.max(startY, 0), 32767);
                        }
                    }
                }
            }
        }

        int guardDist = 0;

        for (int k = 1; k < grid.length - 1; k++)
        {
            for (int z = 1; z < grid[k].length - 1; z++)
            {
                boolean n = grid[k][z - 1] >= 2;
                boolean s = grid[k][z + 1] >= 2;
                boolean e = grid[k + 1][z] >= 2;
                boolean w = grid[k - 1][z] >= 2;
                boolean ne = grid[k + 1][z - 1] >= 2;
                boolean sw = grid[k - 1][z + 1] >= 2;
                boolean se = grid[k + 1][z + 1] >= 2;
                boolean nw = grid[k - 1][z - 1] >= 2;

                if (grid[k][z] >= 2)
                {
                    int dx = minX + k;
                    int dz = minZ + z;
                    int lowestY = foundations[k][z];
                    int startY = heights[k][z];

                    if (hasFluidAt(world, dx, dz, lowestY))
                    {
                        int nearFoundation = Math.max(Math.max(foundations[k - 1][z], foundations[k + 1][z]),
                                Math.max(foundations[k][z + 1], foundations[k][z - 1]));

                        if (nearFoundation > 0)
                        {
                            lowestY = nearFoundation;
                        }
                        else
                        {
                            while (lowestY > 1 && hasFluidAt(world, dx, dz, lowestY - 1))
                            {
                                lowestY--;
                            }
                        }
                    }

                    if (startY <= lowestY)
                    {
                        continue;
                    }

                    for (int dy = startY; dy > lowestY; dy--)
                    {
                        if (dy == startY)
                        {
                            if (!ne && !n && !e)
                            {
                                setBlock(world, dx + 2, dy, dz - 2, blockBase, blockBaseMeta);
                                setBlock(world, dx + 2, dy, dz - 1, blockBase, blockBaseMeta);
                                setBlock(world, dx + 1, dy, dz - 2, blockBase, blockBaseMeta);
                                setBlock(world, dx + 2, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
                                setBlock(world, dx + 2, dy + 1, dz - 1, blockBase, blockBaseMeta, false);
                                setBlock(world, dx + 1, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
                            }
                            if (!nw && !n && !w)
                            {
                                setBlock(world, dx - 2, dy, dz - 2, blockBase, blockBaseMeta);
                                setBlock(world, dx - 1, dy, dz - 2, blockBase, blockBaseMeta);
                                setBlock(world, dx - 2, dy, dz - 1, blockBase, blockBaseMeta);
                                setBlock(world, dx - 2, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
                                setBlock(world, dx - 1, dy + 1, dz - 2, blockBase, blockBaseMeta, false);
                                setBlock(world, dx - 2, dy + 1, dz - 1, blockBase, blockBaseMeta, false);
                            }
                            if (!se && !s && !e)
                            {
                                setBlock(world, dx + 2, dy, dz + 2, blockBase, blockBaseMeta);
                                setBlock(world, dx + 1, dy, dz + 2, blockBase, blockBaseMeta);
                                setBlock(world, dx + 2, dy, dz + 1, blockBase, blockBaseMeta);
                                setBlock(world, dx + 2, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
                                setBlock(world, dx + 1, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
                                setBlock(world, dx + 2, dy + 1, dz + 1, blockBase, blockBaseMeta, false);
                            }
                            if (!sw && !s && !w)
                            {
                                setBlock(world, dx - 2, dy, dz + 2, blockBase, blockBaseMeta);
                                setBlock(world, dx - 1, dy, dz + 2, blockBase, blockBaseMeta);
                                setBlock(world, dx - 2, dy, dz + 1, blockBase, blockBaseMeta);
                                setBlock(world, dx - 2, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
                                setBlock(world, dx - 1, dy + 1, dz + 2, blockBase, blockBaseMeta, false);
                                setBlock(world, dx - 2, dy + 1, dz + 1, blockBase, blockBaseMeta, false);
                            }
                            if (!n && !ne && !nw)
                            {
                                setBlock(world, dx, dy, dz - 2, blockBase, blockBaseMeta);
                                setBlock(world, dx, dy + 1, dz - 2, stairsBlock, 0, false);
                            }
                            if (!e && !se && !ne)
                            {
                                setBlock(world, dx + 2, dy, dz, blockBase, blockBaseMeta);
                                setBlock(world, dx + 2, dy + 1, dz, stairsBlock, 2, false);
                            }
                            if (!s && !se && !sw)
                            {
                                setBlock(world, dx, dy, dz + 2, blockBase, blockBaseMeta);
                                setBlock(world, dx, dy + 1, dz + 2, stairsBlock, 0, false);
                            }
                            if (!w && !nw && !sw)
                            {
                                setBlock(world, dx - 2, dy, dz, blockBase, blockBaseMeta);
                                setBlock(world, dx - 2, dy + 1, dz, stairsBlock, 2, false);
                            }

                            if (++guardDist > 200)
                            {
                                spawnGuard(world, dx, dy, dz);
                                guardDist = 0;
                            }
                        }
                        else
                        {
                            int distCheck = 4;
                            boolean gate = grid[k][z] == 3
                                    && ((k > distCheck && k < grid.length - distCheck && grid[k - distCheck][z] == 2 && grid[k + distCheck][z] == 2)
                                    || (z > distCheck && z < grid[k].length - distCheck && grid[k][z - distCheck] == 2 && grid[k][z + distCheck] == 2));

                            if (gate && dy == startY - 3)
                            {
                                world.setBlockState(new BlockPos(dx, dy, dz), blockFence.getStateFromMeta(0), 2);
                                if (grid[k + 1][z] != 3 || grid[k - 1][z] != 3)
                                {
                                    if (grid[k + 1][z] == 3)
                                    {
                                        world.setBlockState(new BlockPos(dx, dy, dz - 1), stairsBlock.getStateFromMeta(5), 2);
                                        world.setBlockState(new BlockPos(dx, dy, dz + 1), stairsBlock.getStateFromMeta(5), 2);
                                    }
                                    else if (grid[k - 1][z] == 3)
                                    {
                                        world.setBlockState(new BlockPos(dx, dy, dz - 1), stairsBlock.getStateFromMeta(4), 2);
                                        world.setBlockState(new BlockPos(dx, dy, dz + 1), stairsBlock.getStateFromMeta(4), 2);
                                    }
                                    else if (grid[k][z + 1] != 3 || grid[k][z - 1] != 3)
                                    {
                                        if (grid[k][z - 1] == 3)
                                        {
                                            world.setBlockState(new BlockPos(dx - 1, dy, dz), stairsBlock.getStateFromMeta(6), 2);
                                            world.setBlockState(new BlockPos(dx + 1, dy, dz), stairsBlock.getStateFromMeta(6), 2);
                                        }
                                        else if (grid[k][z + 1] == 3)
                                        {
                                            world.setBlockState(new BlockPos(dx - 1, dy, dz), stairsBlock.getStateFromMeta(7), 2);
                                            world.setBlockState(new BlockPos(dx + 1, dy, dz), stairsBlock.getStateFromMeta(7), 2);
                                        }
                                    }
                                }
                            }

                            if (!gate || dy > startY - 3)
                            {
                                setBlock(world, dx, dy, dz, blockBase, blockBaseMeta);

                                boolean ng = grid[k][z - 1] == 3;
                                boolean sg = grid[k][z + 1] == 3;
                                boolean eg = grid[k + 1][z] == 3;
                                boolean wg = grid[k - 1][z] == 3;

                                if (!ng)
                                {
                                    setBlock(world, dx, dy, dz - 1, blockBase, blockBaseMeta);
                                }
                                if (!ng && !eg)
                                {
                                    setBlock(world, dx + 1, dy, dz - 1, blockBase, blockBaseMeta);
                                }
                                if (!ng && !wg)
                                {
                                    setBlock(world, dx - 1, dy, dz - 1, blockBase, blockBaseMeta);
                                }
                                if (!eg)
                                {
                                    setBlock(world, dx + 1, dy, dz, blockBase, blockBaseMeta);
                                }
                                if (!sg)
                                {
                                    setBlock(world, dx, dy, dz + 1, blockBase, blockBaseMeta);
                                }
                                if (!sg && !eg)
                                {
                                    setBlock(world, dx + 1, dy, dz + 1, blockBase, blockBaseMeta);
                                }
                                if (!sg && !wg)
                                {
                                    setBlock(world, dx - 1, dy, dz + 1, blockBase, blockBaseMeta);
                                }
                                if (!wg)
                                {
                                    setBlock(world, dx - 1, dy, dz, blockBase, blockBaseMeta);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void spawnGuard(World world, int x, int y, int z)
    {
        EntityVillageGuard guard = new EntityVillageGuard(world);
        guard.setPosition(x + 0.5D, y, z + 0.5D);
        guard.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(x, y, z)), null);
        world.spawnEntity(guard);
        guard.syncEquipmentToClients();
    }

    private static void setBlock(World world, int x, int y, int z, Block block, int meta)
    {
        setBlock(world, x, y, z, block, meta, true);
    }

    private static final int SOLID_THRESHOLD = 9;
    private static final int SHALLOW_WATER_PROBE = 4;

    private static int findFoundationY(World world, int dx, int dz, int yCoord)
    {
        for (int dy = yCoord; dy > 1; dy--)
        {
            if (hasFluidAt(world, dx, dz, dy))
            {
                int surface = dy;

                for (int probe = surface; probe > surface - SHALLOW_WATER_PROBE && probe > 1; probe--)
                {
                    if (!hasFluidAt(world, dx, dz, probe) && countSolidAt(world, dx, dz, probe) >= SOLID_THRESHOLD)
                    {
                        return probe;
                    }
                }

                return surface;
            }

            if (countSolidAt(world, dx, dz, dy) >= SOLID_THRESHOLD)
            {
                return dy;
            }
        }

        return 1;
    }

    private static boolean hasFluidAt(World world, int dx, int dz, int y)
    {
        for (int ddx = dx - 1; ddx <= dx + 1; ddx++)
        {
            for (int ddz = dz - 1; ddz <= dz + 1; ddz++)
            {
                Material material = world.getBlockState(new BlockPos(ddx, y, ddz)).getMaterial();

                if (material == Material.WATER || material == Material.LAVA)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static int countSolidAt(World world, int dx, int dz, int y)
    {
        int solidCount = 0;

        for (int ddx = dx - 1; ddx <= dx + 1; ddx++)
        {
            for (int ddz = dz - 1; ddz <= dz + 1; ddz++)
            {
                IBlockState state = world.getBlockState(new BlockPos(ddx, y, ddz));
                Material material = state.getMaterial();
                boolean replaceable = material.isReplaceable() || material == Material.PLANTS
                        || material == Material.VINE || material == Material.LEAVES;

                if (state.isFullBlock() && !replaceable)
                {
                    solidCount++;
                }
            }
        }

        return solidCount;
    }

    private static boolean canReplaceForWall(IBlockState existing)
    {
        Material material = existing.getMaterial();
        return material.isReplaceable()
                || material == Material.PLANTS
                || material == Material.VINE
                || material == Material.LEAVES
                || material == Material.WATER
                || material == Material.LAVA;
    }

    private static void setBlock(World world, int x, int y, int z, Block block, int meta, boolean replaceSoftBlocks)
    {
        if (!replaceSoftBlocks)
        {
            world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(meta), 2);
            return;
        }

        IBlockState existing = world.getBlockState(new BlockPos(x, y, z));

        if (canReplaceForWall(existing))
        {
            world.setBlockState(new BlockPos(x, y, z), block.getStateFromMeta(meta), 2);
        }
    }

    public static class StructureBounds extends StructureBoundingBox
    {
        public final boolean ew;

        public StructureBounds(StructureVillagePieces.Road path, int expansionX, int expansionZ)
        {
            this(path.getBoundingBox(), expansionX, expansionZ);
        }

        public StructureBounds(StructureBoundingBox bb, int expansionX, int expansionZ)
        {
            this(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, expansionX, expansionZ);
        }

        public StructureBounds(int x, int y, int z, int x2, int y2, int z2, int expansionX, int expansionZ)
        {
            ew = x2 - x > z2 - z;
            if (ew)
            {
                minX = x - expansionZ;
                maxX = x2 + expansionZ;
                minZ = z - expansionX;
                maxZ = z2 + expansionX;
            }
            else
            {
                minX = x - expansionX;
                maxX = x2 + expansionX;
                minZ = z - expansionZ;
                maxZ = z2 + expansionZ;
            }
            minY = y;
            maxY = y2;
        }
    }
}
