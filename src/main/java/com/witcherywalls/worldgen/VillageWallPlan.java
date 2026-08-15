package com.witcherywalls.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public final class VillageWallPlan
{
    final int minX;
    final int minZ;
    final int yCoord;
    final boolean desert;
    final byte[][] grid;
    final int[][] foundations;
    final int[][] heights;
    final boolean[][] scanned;
    final boolean[][] placed;
    int guardDist;

    VillageWallPlan(int minX, int minZ, int yCoord, boolean desert, byte[][] grid, int[][] foundations, int[][] heights)
    {
        this.minX = minX;
        this.minZ = minZ;
        this.yCoord = yCoord;
        this.desert = desert;
        this.grid = grid;
        this.foundations = foundations;
        this.heights = heights;
        this.scanned = new boolean[grid.length][grid[0].length];
        this.placed = new boolean[grid.length][grid[0].length];
    }

    private VillageWallPlan(int minX, int minZ, int yCoord, boolean desert, byte[][] grid)
    {
        this(minX, minZ, yCoord, desert, grid,
                new int[grid.length][grid[0].length],
                new int[grid.length][grid[0].length]);
    }

    int remaining()
    {
        int count = 0;
        for (int k = 1; k < grid.length - 1; k++)
        {
            for (int z = 1; z < grid[k].length - 1; z++)
            {
                if (grid[k][z] >= 2 && !placed[k][z])
                {
                    count++;
                }
            }
        }
        return count;
    }

    boolean intersects(ChunkPos pos)
    {
        int chunkMinX = pos.getMinBlockX();
        int chunkMaxX = pos.getMaxBlockX();
        int chunkMinZ = pos.getMinBlockZ();
        int chunkMaxZ = pos.getMaxBlockZ();
        int wallMinX = minX + 1;
        int wallMaxX = minX + grid.length - 2;
        int wallMinZ = minZ + 1;
        int wallMaxZ = minZ + grid[0].length - 2;
        return wallMinX <= chunkMaxX && wallMaxX >= chunkMinX && wallMinZ <= chunkMaxZ && wallMaxZ >= chunkMinZ;
    }

    CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putInt("minX", minX);
        tag.putInt("minZ", minZ);
        tag.putInt("yCoord", yCoord);
        tag.putBoolean("desert", desert);
        tag.putInt("w", grid.length);
        tag.putInt("h", grid[0].length);
        tag.putByteArray("grid", flatten(grid));
        tag.putInt("guardDist", guardDist);
        return tag;
    }

    static VillageWallPlan load(CompoundTag tag)
    {
        int width = tag.getInt("w");
        int height = tag.getInt("h");
        byte[][] grid = inflate(tag.getByteArray("grid"), width, height);
        VillageWallPlan plan = new VillageWallPlan(
                tag.getInt("minX"),
                tag.getInt("minZ"),
                tag.getInt("yCoord"),
                tag.getBoolean("desert"),
                grid);
        plan.guardDist = tag.getInt("guardDist");
        return plan;
    }

    private static byte[] flatten(byte[][] grid)
    {
        byte[] flat = new byte[grid.length * grid[0].length];
        int i = 0;
        for (byte[] row : grid)
        {
            System.arraycopy(row, 0, flat, i, row.length);
            i += row.length;
        }
        return flat;
    }

    private static byte[][] inflate(byte[] flat, int width, int height)
    {
        byte[][] grid = new byte[width][height];
        int i = 0;
        for (int x = 0; x < width; x++)
        {
            System.arraycopy(flat, i, grid[x], 0, height);
            i += height;
        }
        return grid;
    }
}
