package com.witcherywalls.worldgen;

import com.witcherywalls.WitcheryWallsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.BlockTags;

import java.util.ArrayDeque;
import java.util.List;

public final class VillageWallGenerator
{
    private static final int SOLID_THRESHOLD = 9;
    private static final int SHALLOW_WATER_PROBE = 4;
    private static final int MAX_SPAN = 384;

    private VillageWallGenerator()
    {
    }

    public static boolean placeWalls(Level level, List<StructureBounds> boundsList, int xCoord, int yCoord, int zCoord, boolean desert)
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
            return false;
        }

        int spanX = maxX - minX;
        int spanZ = maxZ - minZ;
        if (spanX > MAX_SPAN || spanZ > MAX_SPAN)
        {
            WitcheryWallsMod.getLogger().info("Village wall perimeter {}x{} exceeds {}, clipping around [{}, {}]",
                    spanX, spanZ, MAX_SPAN, xCoord, zCoord);
            int half = MAX_SPAN / 2;
            minX = Math.max(minX, xCoord - half);
            maxX = Math.min(maxX, xCoord + half);
            minZ = Math.max(minZ, zCoord - half);
            maxZ = Math.min(maxZ, zCoord + half);
            spanX = maxX - minX;
            spanZ = maxZ - minZ;
            if (spanX < 8 || spanZ < 8)
            {
                WitcheryWallsMod.getLogger().warn("Village wall perimeter too small after clipping ({}x{}), skipping at [{}, {}]",
                        spanX, spanZ, xCoord, zCoord);
                return false;
            }
        }

        byte[][] grid = new byte[maxX - minX + 3][maxZ - minZ + 3];
        int[][] heights = new int[maxX - minX + 3][maxZ - minZ + 3];
        int[][] foundations = new int[maxX - minX + 3][maxZ - minZ + 3];

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
                    if (mx <= 0 || mz <= 0 || mx >= grid.length - 1 || mz >= grid[mx].length - 1)
                    {
                        continue;
                    }

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

        fillEnclosedHoles(grid);

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

        VillageWallPlan plan = new VillageWallPlan(minX, minZ, yCoord, desert, grid, foundations, heights);
        resume(level, plan, Integer.MAX_VALUE);

        int remaining = plan.remaining();
        if (remaining > 0 && level instanceof net.minecraft.server.level.ServerLevel serverLevel)
        {
            VillageWallDeferred.add(serverLevel, plan);
        }

        return true;
    }

    /**
     * Street expansions often leave courtyards and gaps as empty cells. Those holes
     * get their own perimeter, which looks like a wall inside the wall. Fill any
     * empty region that cannot reach the outside of the grid.
     */
    private static void fillEnclosedHoles(byte[][] grid)
    {
        int width = grid.length;
        int depth = grid[0].length;
        boolean[][] outside = new boolean[width][depth];
        ArrayDeque<int[]> queue = new ArrayDeque<>();

        for (int x = 0; x < width; x++)
        {
            markOutside(grid, outside, queue, x, 0);
            markOutside(grid, outside, queue, x, depth - 1);
        }
        for (int z = 0; z < depth; z++)
        {
            markOutside(grid, outside, queue, 0, z);
            markOutside(grid, outside, queue, width - 1, z);
        }

        while (!queue.isEmpty())
        {
            int[] pos = queue.removeFirst();
            int x = pos[0];
            int z = pos[1];
            markOutside(grid, outside, queue, x + 1, z);
            markOutside(grid, outside, queue, x - 1, z);
            markOutside(grid, outside, queue, x, z + 1);
            markOutside(grid, outside, queue, x, z - 1);
        }

        for (int x = 1; x < width - 1; x++)
        {
            for (int z = 1; z < depth - 1; z++)
            {
                if (grid[x][z] == 0 && !outside[x][z])
                {
                    grid[x][z] = 2;
                }
            }
        }
    }

    private static void markOutside(byte[][] grid, boolean[][] outside, ArrayDeque<int[]> queue, int x, int z)
    {
        if (x < 0 || z < 0 || x >= grid.length || z >= grid[x].length)
        {
            return;
        }
        if (grid[x][z] != 0 || outside[x][z])
        {
            return;
        }
        outside[x][z] = true;
        queue.add(new int[] {x, z});
    }

    static int resume(Level level, VillageWallPlan plan, int budget)
    {
        prepareHeights(level, plan);

        int placed = 0;
        for (int k = 1; k < plan.grid.length - 1 && placed < budget; k++)
        {
            for (int z = 1; z < plan.grid[k].length - 1 && placed < budget; z++)
            {
                if (plan.grid[k][z] < 2 || plan.placed[k][z] || !plan.scanned[k][z])
                {
                    continue;
                }

                int dx = plan.minX + k;
                int dz = plan.minZ + z;
                if (!level.hasChunk(dx >> 4, dz >> 4))
                {
                    continue;
                }

                placeWallColumn(level, plan, k, z);
                plan.placed[k][z] = true;
                placed++;
            }
        }

        return placed;
    }

    private static void prepareHeights(Level level, VillageWallPlan plan)
    {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int k = 1; k < plan.grid.length - 1; k++)
        {
            for (int z = 1; z < plan.grid[k].length - 1; z++)
            {
                if (plan.grid[k][z] < 2 || plan.scanned[k][z])
                {
                    continue;
                }

                int dx = plan.minX + k;
                int dz = plan.minZ + z;
                if (!level.hasChunk(dx >> 4, dz >> 4))
                {
                    continue;
                }

                int foundation = findFoundationY(level, dx, dz, plan.yCoord);
                plan.foundations[k][z] = foundation;
                plan.heights[k][z] = foundation + 9;
                plan.scanned[k][z] = true;
            }
        }

        for (int k = 1; k < plan.grid.length - 1; k++)
        {
            for (int z = 1; z < plan.grid[k].length - 1; z++)
            {
                if (plan.grid[k][z] < 2 || !plan.scanned[k][z])
                {
                    continue;
                }

                int dx = plan.minX + k;
                int dz = plan.minZ + z;
                if (!level.hasChunk(dx >> 4, dz >> 4))
                {
                    continue;
                }

                if (hasFluidAt(level, dx, dz, plan.foundations[k][z], cursor))
                {
                    int nearFoundation = maxNeighborFoundation(plan.grid, plan.foundations, k, z);
                    if (nearFoundation != Integer.MIN_VALUE && nearFoundation > plan.foundations[k][z])
                    {
                        plan.foundations[k][z] = nearFoundation;
                        plan.heights[k][z] = nearFoundation + 9;
                    }
                }
            }
        }

        // Single neighbor pass (1.7.10 style). Multi-pass smoothing used to
        // ramp long wall stretches up onto tree canopies.
        for (int k = 1; k < plan.grid.length - 1; k++)
        {
            for (int z = 1; z < plan.grid[k].length - 1; z++)
            {
                if (plan.grid[k][z] < 2 || !plan.scanned[k][z] || plan.placed[k][z])
                {
                    continue;
                }

                int near = maxNeighborHeight(plan.grid, plan.heights, k, z);
                if (near == Integer.MIN_VALUE)
                {
                    continue;
                }

                int startY = plan.heights[k][z];
                if (near > startY)
                {
                    startY = near - 1;
                }
                else if (near < startY)
                {
                    startY = near + 1;
                }
                plan.heights[k][z] = startY;
            }
        }
    }

    private static void placeWallColumn(Level level, VillageWallPlan plan, int k, int z)
    {
        byte[][] grid = plan.grid;
        boolean n = grid[k][z - 1] >= 2;
        boolean s = grid[k][z + 1] >= 2;
        boolean e = grid[k + 1][z] >= 2;
        boolean w = grid[k - 1][z] >= 2;
        boolean ne = grid[k + 1][z - 1] >= 2;
        boolean sw = grid[k - 1][z + 1] >= 2;
        boolean se = grid[k + 1][z + 1] >= 2;
        boolean nw = grid[k - 1][z - 1] >= 2;

        int dx = plan.minX + k;
        int dz = plan.minZ + z;
        int startY = plan.heights[k][z];
        int placementBottom = getWallBottomY(level, dx, dz, plan.foundations[k][z]);

        if (startY <= placementBottom)
        {
            return;
        }

        Block blockBase = plan.desert ? Blocks.SMOOTH_SANDSTONE : Blocks.STONE_BRICKS;
        Block stairsBlock = plan.desert ? Blocks.SANDSTONE_STAIRS : Blocks.STONE_BRICK_STAIRS;
        Block blockFence = Blocks.OAK_FENCE;
        BlockState blockBaseState = blockBase.defaultBlockState();

        for (int dy = startY; dy > placementBottom; dy--)
        {
            if (dy == startY)
            {
                if (!ne && !n && !e)
                {
                    setBlock(level, dx + 2, dy, dz - 2, blockBaseState);
                    setBlock(level, dx + 2, dy, dz - 1, blockBaseState);
                    setBlock(level, dx + 1, dy, dz - 2, blockBaseState);
                    setBlock(level, dx + 2, dy + 1, dz - 2, blockBaseState, false);
                    setBlock(level, dx + 2, dy + 1, dz - 1, blockBaseState, false);
                    setBlock(level, dx + 1, dy + 1, dz - 2, blockBaseState, false);
                }
                if (!nw && !n && !w)
                {
                    setBlock(level, dx - 2, dy, dz - 2, blockBaseState);
                    setBlock(level, dx - 1, dy, dz - 2, blockBaseState);
                    setBlock(level, dx - 2, dy, dz - 1, blockBaseState);
                    setBlock(level, dx - 2, dy + 1, dz - 2, blockBaseState, false);
                    setBlock(level, dx - 1, dy + 1, dz - 2, blockBaseState, false);
                    setBlock(level, dx - 2, dy + 1, dz - 1, blockBaseState, false);
                }
                if (!se && !s && !e)
                {
                    setBlock(level, dx + 2, dy, dz + 2, blockBaseState);
                    setBlock(level, dx + 1, dy, dz + 2, blockBaseState);
                    setBlock(level, dx + 2, dy, dz + 1, blockBaseState);
                    setBlock(level, dx + 2, dy + 1, dz + 2, blockBaseState, false);
                    setBlock(level, dx + 1, dy + 1, dz + 2, blockBaseState, false);
                    setBlock(level, dx + 2, dy + 1, dz + 1, blockBaseState, false);
                }
                if (!sw && !s && !w)
                {
                    setBlock(level, dx - 2, dy, dz + 2, blockBaseState);
                    setBlock(level, dx - 1, dy, dz + 2, blockBaseState);
                    setBlock(level, dx - 2, dy, dz + 1, blockBaseState);
                    setBlock(level, dx - 2, dy + 1, dz + 2, blockBaseState, false);
                    setBlock(level, dx - 1, dy + 1, dz + 2, blockBaseState, false);
                    setBlock(level, dx - 2, dy + 1, dz + 1, blockBaseState, false);
                }
                if (!n && !ne && !nw)
                {
                    setBlock(level, dx, dy, dz - 2, blockBaseState);
                    setBlock(level, dx, dy + 1, dz - 2, stairState(stairsBlock, 0), false);
                }
                if (!e && !se && !ne)
                {
                    setBlock(level, dx + 2, dy, dz, blockBaseState);
                    setBlock(level, dx + 2, dy + 1, dz, stairState(stairsBlock, 2), false);
                }
                if (!s && !se && !sw)
                {
                    setBlock(level, dx, dy, dz + 2, blockBaseState);
                    setBlock(level, dx, dy + 1, dz + 2, stairState(stairsBlock, 0), false);
                }
                if (!w && !nw && !sw)
                {
                    setBlock(level, dx - 2, dy, dz, blockBaseState);
                    setBlock(level, dx - 2, dy + 1, dz, stairState(stairsBlock, 2), false);
                }

                if (++plan.guardDist > 200)
                {
                    spawnWallGuard(level, dx, dy, dz);
                    plan.guardDist = 0;
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
                    setBlock(level, dx, dy, dz, blockFence.defaultBlockState());
                    if (grid[k + 1][z] != 3 || grid[k - 1][z] != 3)
                    {
                        if (grid[k + 1][z] == 3)
                        {
                            setBlock(level, dx, dy, dz - 1, stairState(stairsBlock, 5));
                            setBlock(level, dx, dy, dz + 1, stairState(stairsBlock, 5));
                        }
                        else if (grid[k - 1][z] == 3)
                        {
                            setBlock(level, dx, dy, dz - 1, stairState(stairsBlock, 4));
                            setBlock(level, dx, dy, dz + 1, stairState(stairsBlock, 4));
                        }
                        else if (grid[k][z + 1] != 3 || grid[k][z - 1] != 3)
                        {
                            if (grid[k][z - 1] == 3)
                            {
                                setBlock(level, dx - 1, dy, dz, stairState(stairsBlock, 6));
                                setBlock(level, dx + 1, dy, dz, stairState(stairsBlock, 6));
                            }
                            else if (grid[k][z + 1] == 3)
                            {
                                setBlock(level, dx - 1, dy, dz, stairState(stairsBlock, 7));
                                setBlock(level, dx + 1, dy, dz, stairState(stairsBlock, 7));
                            }
                        }
                    }
                }

                if (!gate || dy > startY - 3)
                {
                    setBlock(level, dx, dy, dz, blockBaseState);

                    boolean ng = grid[k][z - 1] == 3;
                    boolean sg = grid[k][z + 1] == 3;
                    boolean eg = grid[k + 1][z] == 3;
                    boolean wg = grid[k - 1][z] == 3;

                    if (!ng)
                    {
                        setBlock(level, dx, dy, dz - 1, blockBaseState);
                    }
                    if (!ng && !eg)
                    {
                        setBlock(level, dx + 1, dy, dz - 1, blockBaseState);
                    }
                    if (!ng && !wg)
                    {
                        setBlock(level, dx - 1, dy, dz - 1, blockBaseState);
                    }
                    if (!eg)
                    {
                        setBlock(level, dx + 1, dy, dz, blockBaseState);
                    }
                    if (!sg)
                    {
                        setBlock(level, dx, dy, dz + 1, blockBaseState);
                    }
                    if (!sg && !eg)
                    {
                        setBlock(level, dx + 1, dy, dz + 1, blockBaseState);
                    }
                    if (!sg && !wg)
                    {
                        setBlock(level, dx - 1, dy, dz + 1, blockBaseState);
                    }
                    if (!wg)
                    {
                        setBlock(level, dx - 1, dy, dz, blockBaseState);
                    }
                }
            }
        }
    }

    private static void spawnWallGuard(Level level, int x, int y, int z)
    {
        if (level instanceof ServerLevel serverLevel)
        {
            VillageGuardSpawnQueue.schedule(serverLevel, new BlockPos(x, y, z), VillageGuardSpawnQueue.Site.WALL);
        }
    }

    private static BlockState stairState(Block block, int meta)
    {
        Direction facing = switch (meta & 3)
        {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        Half half = (meta & 4) != 0 ? Half.TOP : Half.BOTTOM;
        return block.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half);
    }

    private static void setBlock(Level level, int x, int y, int z, BlockState state)
    {
        setBlock(level, x, y, z, state, true);
    }

    private static int getWallBottomY(Level level, int dx, int dz, int landFoundation)
    {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, dx, dz);
        int oceanFloor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, dx, dz);

        if (surface - oceanFloor <= 1)
        {
            return landFoundation;
        }

        return oceanFloor;
    }

    /**
     * Find solid ground under a wall column by scanning down from the village
     * Y, matching Witchery 1.7.10. Starting from WORLD_SURFACE_WG caused walls
     * to treat tree canopies as terrain and "jump" over foliage.
     */
    private static int findFoundationY(Level level, int dx, int dz, int yCoord)
    {
        int minY = level.getMinBuildHeight() + 1;
        int top = Math.max(yCoord, minY);
        int bottom = minY;

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dy = top; dy >= bottom; dy--)
        {
            if (hasFluidAt(level, dx, dz, dy, cursor))
            {
                int waterSurface = dy;

                for (int probe = waterSurface; probe > waterSurface - SHALLOW_WATER_PROBE && probe >= bottom; probe--)
                {
                    if (!hasFluidAt(level, dx, dz, probe, cursor) && countSolidAt(level, dx, dz, probe, cursor) >= SOLID_THRESHOLD)
                    {
                        return probe;
                    }
                }

                return waterSurface;
            }

            if (countSolidAt(level, dx, dz, dy, cursor) >= SOLID_THRESHOLD)
            {
                return dy;
            }
        }

        int oceanFloor = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, dx, dz);
        return Math.max(bottom, oceanFloor);
    }

    private static int maxNeighborFoundation(byte[][] grid, int[][] foundations, int k, int z)
    {
        int max = Integer.MIN_VALUE;

        if (grid[k - 1][z] >= 2 && foundations[k - 1][z] != 0)
        {
            max = Math.max(max, foundations[k - 1][z]);
        }
        if (grid[k + 1][z] >= 2 && foundations[k + 1][z] != 0)
        {
            max = Math.max(max, foundations[k + 1][z]);
        }
        if (grid[k][z - 1] >= 2 && foundations[k][z - 1] != 0)
        {
            max = Math.max(max, foundations[k][z - 1]);
        }
        if (grid[k][z + 1] >= 2 && foundations[k][z + 1] != 0)
        {
            max = Math.max(max, foundations[k][z + 1]);
        }

        return max;
    }

    private static int maxNeighborHeight(byte[][] grid, int[][] heights, int k, int z)
    {
        int max = Integer.MIN_VALUE;

        if (grid[k - 1][z] >= 2 && heights[k - 1][z] != 0)
        {
            max = Math.max(max, heights[k - 1][z]);
        }
        if (grid[k + 1][z] >= 2 && heights[k + 1][z] != 0)
        {
            max = Math.max(max, heights[k + 1][z]);
        }
        if (grid[k][z - 1] >= 2 && heights[k][z - 1] != 0)
        {
            max = Math.max(max, heights[k][z - 1]);
        }
        if (grid[k][z + 1] >= 2 && heights[k][z + 1] != 0)
        {
            max = Math.max(max, heights[k][z + 1]);
        }

        return max;
    }

    private static boolean hasFluidAt(Level level, int dx, int dz, int y, BlockPos.MutableBlockPos cursor)
    {
        for (int ddx = dx - 1; ddx <= dx + 1; ddx++)
        {
            for (int ddz = dz - 1; ddz <= dz + 1; ddz++)
            {
                FluidState fluid = level.getFluidState(cursor.set(ddx, y, ddz));
                if (!fluid.isEmpty())
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static int countSolidAt(Level level, int dx, int dz, int y, BlockPos.MutableBlockPos cursor)
    {
        int solidCount = 0;

        for (int ddx = dx - 1; ddx <= dx + 1; ddx++)
        {
            for (int ddz = dz - 1; ddz <= dz + 1; ddz++)
            {
                BlockState state = level.getBlockState(cursor.set(ddx, y, ddz));
                if (!state.isAir() && !canReplaceForWall(state))
                {
                    solidCount++;
                }
            }
        }

        return solidCount;
    }

    static boolean canReplaceForWall(BlockState state)
    {
        if (state.isAir() || state.canBeReplaced() || !state.getFluidState().isEmpty())
        {
            return true;
        }

        // 1.7.10 treated wood/leaves/plants as non-foundation so walls sit on dirt,
        // not on trunks. Logs must be replaceable here or findFoundationY climbs trees.
        return state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.is(BlockTags.REPLACEABLE_BY_TREES);
    }

    private static void setBlock(Level level, int x, int y, int z, BlockState state, boolean replaceSoftBlocks)
    {
        VillageWallPlacementQueue.enqueue(level, new BlockPos(x, y, z), state, replaceSoftBlocks);
    }

    public static final class StructureBounds
    {
        public final int minX;
        public final int minY;
        public final int minZ;
        public final int maxX;
        public final int maxY;
        public final int maxZ;
        public final boolean ew;

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
