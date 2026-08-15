package com.witcherywalls.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.Random;

/**
 * 1.12 {@code StructureComponent} placement: local coords, coordBaseMode offsets,
 * then mirror/rotation on block states.
 */
public final class VillagePieceBuilder
{
    private final Level level;
    private final BoundingBox box;
    private final Direction facing;
    private final VillagePalette palette;
    private final Rotation rotation;
    private final Mirror mirror;

    public VillagePieceBuilder(Level level, BoundingBox box, Direction facing, VillagePalette palette)
    {
        this.level = level;
        this.box = box;
        this.facing = facing;
        this.palette = palette;

        switch (facing)
        {
            case SOUTH ->
            {
                this.mirror = Mirror.LEFT_RIGHT;
                this.rotation = Rotation.NONE;
            }
            case WEST ->
            {
                this.mirror = Mirror.LEFT_RIGHT;
                this.rotation = Rotation.CLOCKWISE_90;
            }
            case EAST ->
            {
                this.mirror = Mirror.NONE;
                this.rotation = Rotation.CLOCKWISE_90;
            }
            default ->
            {
                this.mirror = Mirror.NONE;
                this.rotation = Rotation.NONE;
            }
        }
    }

    public VillagePalette palette()
    {
        return palette;
    }

    public BoundingBox box()
    {
        return box;
    }

    public static BoundingBox boxFor(int x, int y, int z, int xMin, int yMin, int zMin,
                                     int width, int height, int depth, Direction facing)
    {
        return switch (facing)
        {
            case NORTH -> new BoundingBox(
                    x + xMin, y + yMin, z - depth + 1 + zMin,
                    x + width - 1 + xMin, y + height - 1 + yMin, z + zMin);
            case SOUTH -> new BoundingBox(
                    x + xMin, y + yMin, z + zMin,
                    x + width - 1 + xMin, y + height - 1 + yMin, z + depth - 1 + zMin);
            case WEST -> new BoundingBox(
                    x - depth + 1 + zMin, y + yMin, z + xMin,
                    x + zMin, y + height - 1 + yMin, z + width - 1 + xMin);
            case EAST -> new BoundingBox(
                    x + zMin, y + yMin, z + xMin,
                    x + depth - 1 + zMin, y + height - 1 + yMin, z + width - 1 + xMin);
            default -> new BoundingBox(
                    x + xMin, y + yMin, z + zMin,
                    x + width - 1 + xMin, y + height - 1 + yMin, z + depth - 1 + zMin);
        };
    }

    public static int averageGroundY(Level level, BoundingBox box)
    {
        long sum = 0;
        int count = 0;

        for (int x = box.minX(); x <= box.maxX(); x += 2)
        {
            for (int z = box.minZ(); z <= box.maxZ(); z += 2)
            {
                if (!level.hasChunk(x >> 4, z >> 4))
                {
                    continue;
                }
                sum += level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                count++;
            }
        }

        if (count == 0)
        {
            return level.getMinBuildHeight();
        }
        return (int) (sum / count);
    }

    public int getX(int x, int z)
    {
        return switch (facing)
        {
            case WEST -> box.maxX() - z;
            case EAST -> box.minX() + z;
            default -> box.minX() + x;
        };
    }

    public int getY(int y)
    {
        return box.minY() + y;
    }

    public int getZ(int x, int z)
    {
        return switch (facing)
        {
            case NORTH -> box.maxZ() - z;
            case SOUTH -> box.minZ() + z;
            default -> box.minZ() + x;
        };
    }

    public BlockPos worldPos(int x, int y, int z)
    {
        return new BlockPos(getX(x, z), getY(y), getZ(x, z));
    }

    public boolean contains(BlockPos pos)
    {
        return box.isInside(pos);
    }

    public void placeBlock(Block block, int meta, int x, int y, int z)
    {
        setBlock(x, y, z, fromMeta(block, meta));
    }

    public void placeBiomeBlock(BlockState paletted, int meta, int x, int y, int z)
    {
        placeBlock(paletted.getBlock(), meta, x, y, z);
    }

    public void fill(int x, int y, int z, int w, int h, int d, Block block)
    {
        BlockState state = palette.apply(block.defaultBlockState());
        if (state.hasProperty(RotatedPillarBlock.AXIS))
        {
            state = state.setValue(RotatedPillarBlock.AXIS, axisForBox(w, h, d));
        }
        if (state.getBlock() instanceof IronBarsBlock)
        {
            fillIronBars(x, y, z, x + w - 1, y + h - 1, z + d - 1, state.getBlock());
            return;
        }
        fillInclusive(x, y, z, x + w - 1, y + h - 1, z + d - 1, state);
    }

    public void fillInclusive(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state)
    {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        for (int y = minY; y <= maxY; y++)
        {
            for (int x = minX; x <= maxX; x++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    setBlock(x, y, z, state);
                }
            }
        }
    }

    public void placeStructureLog(int structureMeta, int x, int y, int z)
    {
        BlockState state = palette.apply(Blocks.OAK_LOG.defaultBlockState());
        if (state.hasProperty(RotatedPillarBlock.AXIS))
        {
            state = state.setValue(RotatedPillarBlock.AXIS, axisForStructureMeta(structureMeta));
        }
        setBlock(x, y, z, state);
    }

    public void placeFencePost(Block fenceBlock, boolean north, boolean south, boolean east, boolean west,
                               int x, int y, int z)
    {
        BlockPos pos = worldPos(x, y, z);
        if (!box.isInside(pos))
        {
            return;
        }
        BlockState state = paneState(fenceBlock, north, south, east, west);
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockState neighbor = VillageWallPlacementQueue.getPlanned(level, pos.relative(direction));
            if (isSolidAttach(neighbor, direction.getOpposite()))
            {
                state = state.setValue(connectionProperty(direction), true);
            }
        }
        VillageWallPlacementQueue.enqueue(level, pos, state, false);
    }

    public void placeTorch(int x, int y, int z)
    {
        BlockPos pos = worldPos(x, y, z);
        if (!box.isInside(pos))
        {
            return;
        }

        BlockState state = torchStateFor(pos);
        VillageWallPlacementQueue.enqueue(level, pos, state, false);
    }

    public void placeChest(int x, int y, int z, Random random)
    {
        BlockPos pos = worldPos(x, y, z);
        if (!box.isInside(pos) || !level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4))
        {
            return;
        }

        BlockState chest = correctChestFacing(pos);
        level.setBlock(pos, chest, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container)
        {
            container.setLootTable(BuiltInLootTables.VILLAGE_WEAPONSMITH, random.nextLong());
        }
    }

    /**
     * 1.12 {@code BlockChest.correctFacing}: face away from a neighboring wall, into open space.
     */
    private BlockState correctChestFacing(BlockPos pos)
    {
        Direction againstWall = null;
        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            BlockState neighbor = VillageWallPlacementQueue.getPlanned(level, pos.relative(direction));
            if (neighbor.is(Blocks.CHEST))
            {
                return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH);
            }
            if (isSolidAttach(neighbor, direction.getOpposite()))
            {
                if (againstWall != null)
                {
                    againstWall = null;
                    break;
                }
                againstWall = direction;
            }
        }

        if (againstWall != null)
        {
            return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, againstWall.getOpposite());
        }

        Direction facing = Direction.NORTH;
        if (isSolidAttach(VillageWallPlacementQueue.getPlanned(level, pos.relative(facing)), facing.getOpposite()))
        {
            facing = facing.getOpposite();
        }
        if (isSolidAttach(VillageWallPlacementQueue.getPlanned(level, pos.relative(facing)), facing.getOpposite()))
        {
            facing = facing.getClockWise();
        }
        if (isSolidAttach(VillageWallPlacementQueue.getPlanned(level, pos.relative(facing)), facing.getOpposite()))
        {
            facing = facing.getOpposite();
        }
        return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
    }

    public void clearUpwards(int x, int y, int z)
    {
        BlockPos.MutableBlockPos cursor = worldPos(x, y, z).mutable();
        if (!box.isInside(cursor) || !level.hasChunk(cursor.getX() >> 4, cursor.getZ() >> 4))
        {
            return;
        }

        int maxY = level.getMaxBuildHeight() - 1;
        while (cursor.getY() <= maxY && !level.getBlockState(cursor).isAir())
        {
            VillageWallPlacementQueue.enqueue(level, cursor.immutable(), Blocks.AIR.defaultBlockState(), false);
            cursor.move(Direction.UP);
        }
    }

    public void fillDown(int x, int y, int z, BlockState state)
    {
        BlockPos.MutableBlockPos cursor = worldPos(x, y, z).mutable();
        if (!box.isInside(cursor))
        {
            return;
        }

        int minY = level.getMinBuildHeight();
        while (cursor.getY() >= minY)
        {
            BlockState existing = level.hasChunk(cursor.getX() >> 4, cursor.getZ() >> 4)
                    ? level.getBlockState(cursor)
                    : Blocks.AIR.defaultBlockState();
            if (!existing.isAir() && existing.getFluidState().isEmpty() && !existing.canBeReplaced())
            {
                break;
            }
            VillageWallPlacementQueue.enqueue(level, cursor.immutable(), transform(state), false);
            cursor.move(Direction.DOWN);
        }
    }

    public void spawnGuards(int x, int y, int z, int count, Random random, VillageGuardSpawnQueue.Site site)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            return;
        }

        int spawned = 0;
        for (int guardNumber = spawned; guardNumber <= count; guardNumber++)
        {
            BlockPos pos = worldPos(x, y, z);
            if (!box.isInside(pos) || !serverLevel.hasChunk(pos.getX() >> 4, pos.getZ() >> 4))
            {
                break;
            }
            VillageGuardSpawnQueue.schedule(serverLevel, pos, site);
        }
    }

    private void setBlock(int x, int y, int z, BlockState state)
    {
        BlockPos pos = worldPos(x, y, z);
        if (!box.isInside(pos))
        {
            return;
        }
        VillageWallPlacementQueue.enqueue(level, pos, transform(state), false);
    }

    private void setBlockUntransformed(int x, int y, int z, BlockState state)
    {
        BlockPos pos = worldPos(x, y, z);
        if (!box.isInside(pos))
        {
            return;
        }
        VillageWallPlacementQueue.enqueue(level, pos, state, false);
    }

    private void fillIronBars(int x1, int y1, int z1, int x2, int y2, int z2, Block bars)
    {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        for (int y = minY; y <= maxY; y++)
        {
            for (int x = minX; x <= maxX; x++)
            {
                for (int z = minZ; z <= maxZ; z++)
                {
                    boolean north = z > minZ || isSolidAttach(
                            VillageWallPlacementQueue.getPlanned(level, worldPos(x, y, z).relative(localToWorld(Direction.NORTH))),
                            localToWorld(Direction.NORTH).getOpposite());
                    boolean south = z < maxZ || isSolidAttach(
                            VillageWallPlacementQueue.getPlanned(level, worldPos(x, y, z).relative(localToWorld(Direction.SOUTH))),
                            localToWorld(Direction.SOUTH).getOpposite());
                    boolean west = x > minX || isSolidAttach(
                            VillageWallPlacementQueue.getPlanned(level, worldPos(x, y, z).relative(localToWorld(Direction.WEST))),
                            localToWorld(Direction.WEST).getOpposite());
                    boolean east = x < maxX || isSolidAttach(
                            VillageWallPlacementQueue.getPlanned(level, worldPos(x, y, z).relative(localToWorld(Direction.EAST))),
                            localToWorld(Direction.EAST).getOpposite());
                    setBlockUntransformed(x, y, z, paneState(bars, north, south, east, west));
                }
            }
        }
    }

    private static boolean isSolidAttach(BlockState state, Direction face)
    {
        return !state.isAir() && state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, face);
    }

    private BlockState paneState(Block block, boolean north, boolean south, boolean east, boolean west)
    {
        BlockState state = block.defaultBlockState();
        if (!state.hasProperty(BlockStateProperties.NORTH))
        {
            return state;
        }
        return state
                .setValue(connectionProperty(localToWorld(Direction.NORTH)), north)
                .setValue(connectionProperty(localToWorld(Direction.SOUTH)), south)
                .setValue(connectionProperty(localToWorld(Direction.EAST)), east)
                .setValue(connectionProperty(localToWorld(Direction.WEST)), west);
    }

    private static net.minecraft.world.level.block.state.properties.BooleanProperty connectionProperty(Direction direction)
    {
        return switch (direction)
        {
            case SOUTH -> BlockStateProperties.SOUTH;
            case EAST -> BlockStateProperties.EAST;
            case WEST -> BlockStateProperties.WEST;
            default -> BlockStateProperties.NORTH;
        };
    }

    private Direction localToWorld(Direction local)
    {
        BlockPos origin = worldPos(0, 0, 0);
        BlockPos step = switch (local)
        {
            case EAST -> worldPos(1, 0, 0);
            case WEST -> worldPos(-1, 0, 0);
            case SOUTH -> worldPos(0, 0, 1);
            case NORTH -> worldPos(0, 0, -1);
            default -> origin;
        };
        return Direction.getNearest(step.getX() - origin.getX(), 0, step.getZ() - origin.getZ());
    }

    private BlockState torchStateFor(BlockPos pos)
    {
        if (isTorchSupport(VillageWallPlacementQueue.getPlanned(level, pos.below()), Direction.UP))
        {
            return Blocks.TORCH.defaultBlockState();
        }

        Direction preferred = switch (facing)
        {
            case SOUTH -> Direction.EAST;
            case WEST -> Direction.WEST;
            case NORTH -> Direction.SOUTH;
            case EAST -> Direction.NORTH;
            default -> Direction.NORTH;
        };
        if (isTorchSupport(VillageWallPlacementQueue.getPlanned(level, pos.relative(preferred.getOpposite())), preferred))
        {
            return Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, preferred);
        }

        for (Direction direction : Direction.Plane.HORIZONTAL)
        {
            if (isTorchSupport(VillageWallPlacementQueue.getPlanned(level, pos.relative(direction.getOpposite())), direction))
            {
                return Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, direction);
            }
        }

        return Blocks.TORCH.defaultBlockState();
    }

    private static boolean isTorchSupport(BlockState state, Direction face)
    {
        return !state.isAir() && state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, face);
    }

    private BlockState transform(BlockState state)
    {
        if (mirror != Mirror.NONE)
        {
            state = state.mirror(mirror);
        }
        if (rotation != Rotation.NONE)
        {
            state = state.rotate(rotation);
        }
        return state;
    }

    private static BlockState fromMeta(Block block, int meta)
    {
        if (block instanceof StairBlock)
        {
            Direction facing = switch (meta & 3)
            {
                case 0 -> Direction.EAST;
                case 1 -> Direction.WEST;
                case 2 -> Direction.SOUTH;
                default -> Direction.NORTH;
            };
            Half half = (meta & 4) != 0 ? Half.TOP : Half.BOTTOM;
            return block.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
        }
        if (block instanceof LadderBlock)
        {
            Direction facing = switch (meta)
            {
                case 3 -> Direction.SOUTH;
                case 4 -> Direction.WEST;
                case 5 -> Direction.EAST;
                default -> Direction.NORTH;
            };
            return block.defaultBlockState().setValue(LadderBlock.FACING, facing);
        }
        if (block instanceof SlabBlock)
        {
            SlabType type = (meta & 8) != 0 ? SlabType.TOP : SlabType.BOTTOM;
            return block.defaultBlockState().setValue(SlabBlock.TYPE, type);
        }
        if (block instanceof RotatedPillarBlock)
        {
            Direction.Axis axis = switch (meta / 4)
            {
                case 1 -> Direction.Axis.X;
                case 2 -> Direction.Axis.Z;
                default -> Direction.Axis.Y;
            };
            return block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
        }
        return block.defaultBlockState();
    }

    private static Direction.Axis axisForBox(int w, int h, int d)
    {
        if (w >= h && w >= d && w > 1)
        {
            return Direction.Axis.X;
        }
        if (d >= h && d >= w && d > 1)
        {
            return Direction.Axis.Z;
        }
        return Direction.Axis.Y;
    }

    private Direction.Axis axisForStructureMeta(int structureMeta)
    {
        int rawMeta = structureMeta / 4;
        if (rawMeta == 0)
        {
            return Direction.Axis.Y;
        }
        if (facing == Direction.NORTH || facing == Direction.SOUTH)
        {
            return rawMeta == 2 ? Direction.Axis.X : Direction.Axis.Z;
        }
        return rawMeta == 1 ? Direction.Axis.Z : Direction.Axis.X;
    }
}
