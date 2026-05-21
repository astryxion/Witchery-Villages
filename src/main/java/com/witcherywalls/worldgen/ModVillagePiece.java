package com.witcherywalls.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;

public abstract class ModVillagePiece extends StructureVillagePieces.Village
{
    public ModVillagePiece()
    {
    }

    public ModVillagePiece(StructureVillagePieces.Start start, int type)
    {
        super(start, type);
    }

    /** Sets facing plus matching rotation/mirror (do not assign coordBaseMode via reflection). */
    protected static void applyHorizontalFacing(StructureComponent component, int horizontalIndex)
    {
        component.setCoordBaseMode(EnumFacing.getHorizontal(horizontalIndex & 3));
    }

    protected static boolean canPlaceAt(StructureBoundingBox bounds)
    {
        return bounds.maxY < 256;
    }

    protected void placeBlock(World world, Block block, int meta, int x, int y, int z, StructureBoundingBox box)
    {
        setBlockState(world, block.getStateFromMeta(meta), x, y, z, box);
    }

    /**
     * Places a wall torch using Witchery's metadata-0 convention: facing is derived from
     * {@link #coordBaseMode} only (see 1.7.10 StructureComponent.func_151553_a for torches).
     * Must not go through {@link #setBlockState} or the facing is rotated twice and pops off.
     */
    protected void placeTorch(World world, int x, int y, int z, StructureBoundingBox box)
    {
        EnumFacing facing = getCoordBaseMode();
        int meta = facing != null ? facing.getHorizontalIndex() + 1 : 0;

        BlockPos pos = new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
        if (box.isVecInside(pos))
        {
            world.setBlockState(pos, Blocks.TORCH.getStateFromMeta(meta), 2);
        }
    }

    /** Places a biome-adjusted block type with structure rotation applied to metadata. */
    protected void placeBiomeBlock(World world, IBlockState biomeState, int meta, int x, int y, int z, StructureBoundingBox box)
    {
        placeBlock(world, biomeState.getBlock(), meta, x, y, z, box);
    }

    protected void placeFencePost(World world, Block fenceBlock, boolean north, boolean south, boolean east, boolean west,
                                  int x, int y, int z, StructureBoundingBox box)
    {
        IBlockState state = fenceBlock.getDefaultState()
                .withProperty(BlockFence.NORTH, north)
                .withProperty(BlockFence.SOUTH, south)
                .withProperty(BlockFence.EAST, east)
                .withProperty(BlockFence.WEST, west);
        setBlockState(world, state, x, y, z, box);
    }

    protected void fill(World world, StructureBoundingBox bounds, int x, int y, int z, int w, int h, int d, Block block)
    {
        IBlockState state = block.getDefaultState();
        if (block instanceof BlockLog)
        {
            state = state.withProperty(BlockLog.LOG_AXIS, getLogAxisForBox(w, h, d));
        }
        state = getBiomeSpecificBlockState(state)
        fillWithBlocks(world, bounds, x, y, z, x + w - 1, y + h - 1, z + d - 1, state, state, false);
    }

    private static BlockLog.EnumAxis getLogAxisForBox(int w, int h, int d)
    {
        if (w >= h && w >= d && w > 1)
        {
            return BlockLog.EnumAxis.X;
        }
        if (d >= h && d >= w && d > 1)
        {
            return BlockLog.EnumAxis.Z;
        }
        return BlockLog.EnumAxis.Y;
    }

    protected void placeStructureLog(World world, int structureMeta, int x, int y, int z, StructureBoundingBox box)
    {
        IBlockState state = getBiomeSpecificBlockState(Blocks.LOG.getDefaultState().withProperty(BlockLog.LOG_AXIS, getLogAxisForStructureMeta(structureMeta)));
        setBlockState(world, state, x, y, z, box);
    }

    private BlockLog.EnumAxis getLogAxisForStructureMeta(int structureMeta)
    {
        int rawMeta = structureMeta / 4;
        if (rawMeta == 0)
        {
            return BlockLog.EnumAxis.Y;
        }
        EnumFacing facing = getCoordBaseMode();
        if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH)
        {
            return rawMeta == 2 ? BlockLog.EnumAxis.X : BlockLog.EnumAxis.Z;
        }
        return rawMeta == 1 ? BlockLog.EnumAxis.Z : BlockLog.EnumAxis.X;
    }
}
