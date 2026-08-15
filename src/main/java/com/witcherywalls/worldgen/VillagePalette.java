package com.witcherywalls.worldgen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

/**
 * 1.12 {@code StructureVillagePieces.Village#getBiomeSpecificBlockState} palettes.
 */
public final class VillagePalette
{
    public enum Type
    {
        PLAINS,
        DESERT,
        SAVANNA,
        TAIGA
    }

    private final Type type;

    public VillagePalette(Type type)
    {
        this.type = type;
    }

    public static VillagePalette fromVillageStructure(ResourceLocation key)
    {
        if (key == null)
        {
            return new VillagePalette(Type.PLAINS);
        }

        String path = key.getPath();
        if (path.contains("desert"))
        {
            return new VillagePalette(Type.DESERT);
        }
        if (path.contains("savanna"))
        {
            return new VillagePalette(Type.SAVANNA);
        }
        if (path.contains("taiga") || path.contains("snowy"))
        {
            return new VillagePalette(Type.TAIGA);
        }
        return new VillagePalette(Type.PLAINS);
    }

    public Type type()
    {
        return type;
    }

    public BlockState apply(BlockState state)
    {
        Block block = state.getBlock();

        return switch (type)
        {
            case DESERT -> applyDesert(state, block);
            case SAVANNA -> applySavanna(state, block);
            case TAIGA -> applyTaiga(state, block);
            case PLAINS -> state;
        };
    }

    private static BlockState applyDesert(BlockState state, Block block)
    {
        if (isLog(state) || block == Blocks.COBBLESTONE || block == Blocks.GRAVEL)
        {
            return Blocks.SANDSTONE.defaultBlockState();
        }
        if (isPlanks(state))
        {
            return Blocks.SMOOTH_SANDSTONE.defaultBlockState();
        }
        if (block == Blocks.OAK_STAIRS || block == Blocks.COBBLESTONE_STAIRS || block == Blocks.STONE_STAIRS)
        {
            return copyStairFacing(Blocks.SANDSTONE_STAIRS.defaultBlockState(), state);
        }
        return state;
    }

    private static BlockState applySavanna(BlockState state, Block block)
    {
        if (isLog(state))
        {
            return copyLogAxis(Blocks.ACACIA_LOG.defaultBlockState(), state);
        }
        if (isPlanks(state))
        {
            return Blocks.ACACIA_PLANKS.defaultBlockState();
        }
        if (block == Blocks.OAK_STAIRS)
        {
            return copyStairFacing(Blocks.ACACIA_STAIRS.defaultBlockState(), state);
        }
        if (block == Blocks.COBBLESTONE)
        {
            return Blocks.ACACIA_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }
        if (block == Blocks.OAK_FENCE)
        {
            return Blocks.ACACIA_FENCE.defaultBlockState();
        }
        return state;
    }

    private static BlockState applyTaiga(BlockState state, Block block)
    {
        if (isLog(state))
        {
            return copyLogAxis(Blocks.SPRUCE_LOG.defaultBlockState(), state);
        }
        if (isPlanks(state))
        {
            return Blocks.SPRUCE_PLANKS.defaultBlockState();
        }
        if (block == Blocks.OAK_STAIRS)
        {
            return copyStairFacing(Blocks.SPRUCE_STAIRS.defaultBlockState(), state);
        }
        if (block == Blocks.OAK_FENCE)
        {
            return Blocks.SPRUCE_FENCE.defaultBlockState();
        }
        return state;
    }

    private static boolean isLog(BlockState state)
    {
        return state.is(BlockTags.LOGS);
    }

    private static boolean isPlanks(BlockState state)
    {
        return state.is(BlockTags.PLANKS);
    }

    private static BlockState copyStairFacing(BlockState target, BlockState source)
    {
        if (source.hasProperty(StairBlock.FACING) && target.hasProperty(StairBlock.FACING))
        {
            target = target.setValue(StairBlock.FACING, source.getValue(StairBlock.FACING));
        }
        if (source.hasProperty(StairBlock.HALF) && target.hasProperty(StairBlock.HALF))
        {
            target = target.setValue(StairBlock.HALF, source.getValue(StairBlock.HALF));
        }
        return target;
    }

    private static BlockState copyLogAxis(BlockState target, BlockState source)
    {
        if (source.hasProperty(RotatedPillarBlock.AXIS) && target.hasProperty(RotatedPillarBlock.AXIS))
        {
            return target.setValue(RotatedPillarBlock.AXIS, source.getValue(RotatedPillarBlock.AXIS));
        }
        return target;
    }
}
