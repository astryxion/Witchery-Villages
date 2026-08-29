package com.witcherywalls.worldgen;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.storage.loot.LootTableList;

import java.util.List;
import java.util.Random;

public class ComponentVillageWatchTower extends ModVillagePiece
{
    private boolean hasMadeChest;
    private int guardsSpawned;

    public ComponentVillageWatchTower()
    {
    }

    public ComponentVillageWatchTower(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox bounds, int coordMode)
    {
        super(start, componentType);
        applyHorizontalFacing(this, coordMode);
        boundingBox = bounds;
    }

    public static ComponentVillageWatchTower construct(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand,
                                                     int x, int y, int z, int facing, int componentType)
    {
        StructureBoundingBox bounds = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 8, 23, 8, EnumFacing.getHorizontal(facing & 3));
        return canPlaceAt(bounds) && StructureComponent.findIntersecting(pieces, bounds) == null
                ? new ComponentVillageWatchTower(start, componentType, rand, bounds, facing)
                : null;
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tagCompound)
    {
        super.writeStructureToNBT(tagCompound);
        tagCompound.setBoolean("Chest", hasMadeChest);
        tagCompound.setInteger("Guards", guardsSpawned);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tagCompound, net.minecraft.world.gen.structure.template.TemplateManager manager)
    {
        super.readStructureFromNBT(tagCompound, manager);
        hasMadeChest = tagCompound.getBoolean("Chest");
        guardsSpawned = tagCompound.getInteger("Guards");
    }

    @Override
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox bounds)
    {
        if (averageGroundLvl < 0)
        {
            averageGroundLvl = getAverageGroundLevel(world, bounds);

            if (averageGroundLvl < 0)
            {
                return true;
            }

            boundingBox.offset(0, averageGroundLvl - boundingBox.maxY + 23 - 1, 0);
        }

        IBlockState cobble = getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
        IBlockState air = Blocks.AIR.getDefaultState();
        IBlockState fence = getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
        IBlockState plank = getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
        IBlockState oakStairs = getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState());
        IBlockState ladder = Blocks.LADDER.getDefaultState();
        IBlockState torch = Blocks.TORCH.getDefaultState();
        IBlockState woodSlab = getBiomeSpecificBlockState(Blocks.WOODEN_SLAB.getDefaultState());

        fillWithBlocks(world, bounds, 2, 0, 2, 6, 17, 6, cobble, cobble, false);
        fillWithBlocks(world, bounds, 3, 13, 3, 5, 14, 5, air, air, false);
        fillWithBlocks(world, bounds, 2, 16, 3, 6, 17, 5, air, air, false);
        fillWithBlocks(world, bounds, 3, 16, 2, 5, 17, 6, air, air, false);

        fillWithBlocks(world, bounds, 3, 15, 1, 5, 16, 1, cobble, cobble, false);
        fillWithBlocks(world, bounds, 4, 14, 1, 4, 17, 1, cobble, cobble, false);
        fillWithBlocks(world, bounds, 3, 15, 7, 5, 16, 7, cobble, cobble, false);
        fillWithBlocks(world, bounds, 4, 14, 7, 4, 17, 7, cobble, cobble, false);
        fillWithBlocks(world, bounds, 1, 15, 3, 1, 16, 5, cobble, cobble, false);
        fillWithBlocks(world, bounds, 1, 14, 4, 1, 17, 4, cobble, cobble, false);
        fillWithBlocks(world, bounds, 7, 15, 3, 7, 16, 5, cobble, cobble, false);
        fillWithBlocks(world, bounds, 7, 14, 4, 7, 17, 4, cobble, cobble, false);

        Block fenceBlock = fence.getBlock();
        placeFencePost(world, fenceBlock, false, true, true, false, 2, 18, 2, bounds);
        placeFencePost(world, fenceBlock, true, false, true, false, 2, 18, 6, bounds);
        placeFencePost(world, fenceBlock, false, true, false, true, 6, 18, 2, bounds);
        placeFencePost(world, fenceBlock, true, false, false, true, 6, 18, 6, bounds);

        fillWithBlocks(world, bounds, 2, 19, 2, 6, 19, 6, plank, plank, false);
        fillWithBlocks(world, bounds, 3, 20, 3, 5, 20, 5, plank, plank, false);
        placeBiomeBlock(world, plank, 0, 4, 19, 4, bounds);

        Block oakStairsBlock = oakStairs.getBlock();
        int n = 3;
        int s = 2;
        int w = 0;
        int e = 1;

        for (int sx = 2; sx <= 6; sx++)
        {
            placeBlock(world, oakStairsBlock, n, sx, 19, 1, bounds);
        }
        for (int sx = 2; sx <= 6; sx++)
        {
            placeBlock(world, oakStairsBlock, n, sx, 20, 2, bounds);
        }
        for (int sx = 3; sx <= 5; sx++)
        {
            placeBlock(world, oakStairsBlock, n, sx, 21, 3, bounds);
        }

        for (int sx = 2; sx <= 6; sx++)
        {
            placeBlock(world, oakStairsBlock, s, sx, 19, 7, bounds);
        }
        for (int sx = 2; sx <= 6; sx++)
        {
            placeBlock(world, oakStairsBlock, s, sx, 20, 6, bounds);
        }
        for (int sx = 3; sx <= 5; sx++)
        {
            placeBlock(world, oakStairsBlock, s, sx, 21, 5, bounds);
        }

        for (int sz = 2; sz <= 6; sz++)
        {
            placeBlock(world, oakStairsBlock, w, 1, 19, sz, bounds);
        }
        for (int sz = 2; sz <= 6; sz++)
        {
            placeBlock(world, oakStairsBlock, w, 2, 20, sz, bounds);
        }
        for (int sz = 3; sz <= 5; sz++)
        {
            placeBlock(world, oakStairsBlock, w, 3, 21, sz, bounds);
        }

        for (int sz = 2; sz <= 6; sz++)
        {
            placeBlock(world, oakStairsBlock, e, 7, 19, sz, bounds);
        }
        for (int sz = 2; sz <= 6; sz++)
        {
            placeBlock(world, oakStairsBlock, e, 6, 20, sz, bounds);
        }
        for (int sz = 3; sz <= 5; sz++)
        {
            placeBlock(world, oakStairsBlock, e, 5, 21, sz, bounds);
        }

        placeBlock(world, Blocks.WOODEN_SLAB, 0, 4, 22, 4, bounds);

        fillWithBlocks(world, bounds, 4, 1, 2, 4, 2, 3, air, air, false);
        placeTorch(world, 3, 2, 4, bounds);
        placeTorch(world, 5, 2, 4, bounds);
        placeTorch(world, 4, 14, 3, bounds);
        placeTorch(world, 4, 16, 4, bounds);

        fillWithBlocks(world, bounds, 2, 6, 2, 2, 14, 2, air, air, false);
        fillWithBlocks(world, bounds, 6, 6, 2, 6, 14, 2, air, air, false);
        fillWithBlocks(world, bounds, 6, 6, 6, 6, 14, 6, air, air, false);
        fillWithBlocks(world, bounds, 2, 6, 6, 2, 14, 6, air, air, false);
        fillWithBlocks(world, bounds, 4, 6, 2, 4, 12, 2, air, air, false);
        fillWithBlocks(world, bounds, 4, 6, 6, 4, 12, 6, air, air, false);
        fillWithBlocks(world, bounds, 6, 6, 4, 6, 12, 4, air, air, false);
        fillWithBlocks(world, bounds, 2, 6, 4, 2, 12, 4, air, air, false);
        fillWithBlocks(world, bounds, 2, 9, 2, 6, 9, 6, cobble, cobble, false);

        fillWithBlocks(world, bounds, 3, 0, 1, 5, 4, 1, cobble, cobble, false);
        fillWithBlocks(world, bounds, 4, 1, 1, 4, 3, 1, air, air, false);
        fillWithBlocks(world, bounds, 3, 0, 7, 5, 4, 7, cobble, cobble, false);
        fillWithBlocks(world, bounds, 4, 1, 7, 4, 3, 7, air, air, false);
        fillWithBlocks(world, bounds, 1, 0, 3, 1, 4, 5, cobble, cobble, false);
        fillWithBlocks(world, bounds, 1, 1, 4, 1, 3, 4, air, air, false);
        fillWithBlocks(world, bounds, 7, 0, 3, 7, 4, 5, cobble, cobble, false);
        fillWithBlocks(world, bounds, 7, 1, 4, 7, 3, 4, air, air, false);

        for (int i = 1; i <= 12; i++)
        {
            placeBlock(world, Blocks.LADDER, 3, 4, i, 4, bounds);
        }
        for (int i = 13; i <= 15; i++)
        {
            placeBlock(world, Blocks.LADDER, 3, 3, i, 5, bounds);
        }

        if (!hasMadeChest)
        {
            BlockPos chestPos = new BlockPos(getXWithOffset(5, 5), getYWithOffset(13), getZWithOffset(5, 5));
            if (bounds.isVecInside(chestPos))
            {
                hasMadeChest = true;
                generateChest(world, bounds, rand, 5, 13, 5, LootTableList.CHESTS_VILLAGE_BLACKSMITH);
            }
        }

        for (int j = 1; j < 7; j++)
        {
            for (int k = 1; k < 7; k++)
            {
                clearCurrentPositionBlocksUpwards(world, k, 23, j, bounds);
                replaceAirAndLiquidDownwards(world, cobble, k, -1, j, bounds);
            }
        }

        final int[] spawned = {guardsSpawned};
        StructureGuardSpawner.spawnGuards(world, bounds, guardsSpawned, 4, 16, 4, 2, new StructureGuardSpawner.GuardSpawnCallback()
        {
            @Override
            public int getX(int localX, int localZ)
            {
                return ComponentVillageWatchTower.this.getXWithOffset(localX, localZ);
            }

            @Override
            public int getY(int localY)
            {
                return ComponentVillageWatchTower.this.getYWithOffset(localY);
            }

            @Override
            public int getZ(int localX, int localZ)
            {
                return ComponentVillageWatchTower.this.getZWithOffset(localX, localZ);
            }

            @Override
            public void onSpawned()
            {
                spawned[0]++;
                guardsSpawned = spawned[0];
            }
        });

        return true;
    }

}
