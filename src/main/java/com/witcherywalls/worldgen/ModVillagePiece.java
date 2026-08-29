package com.witcherywalls.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockTorch;
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
     * Places a standing or wall torch from neighboring support, matching 1.20.1
     * VillagePieceBuilder.torchStateFor. Written directly so StructureComponent
     * does not rotate the facing a second time.
     */
    protected void placeTorch(World world, int x, int y, int z, StructureBoundingBox box)
    {
        BlockPos pos = new BlockPos(getXWithOffset(x, z), getYWithOffset(y), getZWithOffset(x, z));
        if (!box.isVecInside(pos))
        {
            return;
        }
        world.setBlockState(pos, torchStateFor(world, pos), 2);
    }

    private IBlockState torchStateFor(World world, BlockPos pos)
    {
        if (isTorchSupport(world, pos.down(), EnumFacing.UP))
        {
            return Blocks.TORCH.getDefaultState();
        }

        EnumFacing preferred = preferredTorchFacing();
        if (isTorchSupport(world, pos.offset(preferred.getOpposite()), preferred))
        {
            return Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, preferred);
        }

        for (EnumFacing direction : EnumFacing.HORIZONTALS)
        {
            if (isTorchSupport(world, pos.offset(direction.getOpposite()), direction))
            {
                return Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, direction);
            }
        }

        return Blocks.TORCH.getDefaultState();
    }

    private EnumFacing preferredTorchFacing()
    {
        EnumFacing facing = getCoordBaseMode();
        if (facing == EnumFacing.SOUTH)
        {
            return EnumFacing.EAST;
        }
        if (facing == EnumFacing.WEST)
        {
            return EnumFacing.WEST;
        }
        if (facing == EnumFacing.NORTH)
        {
            return EnumFacing.SOUTH;
        }
        return EnumFacing.NORTH;
    }

    private static boolean isTorchSupport(World world, BlockPos pos, EnumFacing face)
    {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock().isAir(state, world, pos))
        {
            return false;
        }
        if (face == EnumFacing.UP)
        {
            return state.isSideSolid(world, pos, EnumFacing.UP) || state.getBlock().canPlaceTorchOnTop(state, world, pos);
        }
        return state.isSideSolid(world, pos, face);
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
        IBlockState state = getBiomeSpecificBlockState(block.getDefaultState());
        if (state.getBlock() instanceof BlockLog)
        {
            state = state.withProperty(BlockLog.LOG_AXIS, getLogAxisForBox(w, h, d));
        }
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
        IBlockState state = getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
        if (state.getBlock() instanceof BlockLog)
        {
            state = state.withProperty(BlockLog.LOG_AXIS, getLogAxisForStructureMeta(structureMeta));
        }
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
