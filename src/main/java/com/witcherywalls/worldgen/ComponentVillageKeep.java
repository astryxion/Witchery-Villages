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

public class ComponentVillageKeep extends ModVillagePiece
{
    private boolean hasMadeChest;
    private int guardsGround;
    private int guardsMidLeft;
    private int guardsMidRight;

    public ComponentVillageKeep()
    {
    }

    public ComponentVillageKeep(StructureVillagePieces.Start start, int componentType, Random rand, StructureBoundingBox bounds, int coordMode)
    {
        super(start, componentType);
        applyHorizontalFacing(this, coordMode);
        boundingBox = bounds;
    }

    public static ComponentVillageKeep construct(StructureVillagePieces.Start start, List<StructureComponent> pieces, Random rand,
                                               int x, int y, int z, int facing, int componentType)
    {
        StructureBoundingBox bounds = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, 16, 26, 16, EnumFacing.getHorizontal(facing & 3));
        return canPlaceAt(bounds) && StructureComponent.findIntersecting(pieces, bounds) == null
                ? new ComponentVillageKeep(start, componentType, rand, bounds, facing)
                : null;
    }

    @Override
    protected void writeStructureToNBT(NBTTagCompound tagCompound)
    {
        super.writeStructureToNBT(tagCompound);
        tagCompound.setBoolean("Chest", hasMadeChest);
        tagCompound.setInteger("GuardsGround", guardsGround);
        tagCompound.setInteger("GuardsMidLeft", guardsMidLeft);
        tagCompound.setInteger("GuardsMidRight", guardsMidRight);
    }

    @Override
    protected void readStructureFromNBT(NBTTagCompound tagCompound, net.minecraft.world.gen.structure.template.TemplateManager manager)
    {
        super.readStructureFromNBT(tagCompound, manager);
        hasMadeChest = tagCompound.getBoolean("Chest");
        guardsGround = tagCompound.getInteger("GuardsGround");
        guardsMidLeft = tagCompound.getInteger("GuardsMidLeft");
        guardsMidRight = tagCompound.getInteger("GuardsMidRight");
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
            boundingBox.offset(0, averageGroundLvl - boundingBox.maxY + 26 - 1, 0);
        }

        IBlockState air = Blocks.AIR.getDefaultState();

        fillWithBlocks(world, bounds, 1, 1, 1, 14, 26, 14, air, air, false);

        drawTower(world, bounds, 0, 0);
        drawTower(world, bounds, 8, 4);

        fill(world, bounds, 7, 0, 2, 3, 1, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 7, 4, 3, 3, 1, 2, Blocks.COBBLESTONE);
        fill(world, bounds, 7, 5, 2, 3, 1, 1, Blocks.COBBLESTONE);
        placeBlock(world, Blocks.COBBLESTONE, 0, 8, 6, 2, bounds);

        int logMeta = 8;
        for (int x = 7; x <= 9; x++)
        {
            placeBlock(world, Blocks.LOG, logMeta, x, 4, 2, bounds);
        }

        Block fenceBlock = getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState()).getBlock();
        for (int fx = 7; fx <= 9; fx++)
        {
            boolean east = fx < 9;
            boolean west = fx > 7;
            placeFencePost(world, fenceBlock, false, false, east, west, fx, 3, 3, bounds);
        }
        placeBlock(world, Blocks.STONE_SLAB, 11, 7, 3, 2, bounds);
        placeBlock(world, Blocks.STONE_SLAB, 11, 7, 3, 4, bounds);
        placeBlock(world, Blocks.STONE_SLAB, 11, 9, 3, 2, bounds);
        placeBlock(world, Blocks.STONE_SLAB, 11, 9, 3, 4, bounds);

        int stairMetaN = 3;
        int stairMetaS = 2;
        for (int x = 7; x <= 9; x++)
        {
            placeBlock(world, Blocks.STONE_BRICK_STAIRS, stairMetaN, x, 0, 1, bounds);
            placeBlock(world, Blocks.STONE_BRICK_STAIRS, stairMetaS, x, 0, 4, bounds);
        }

        fill(world, bounds, 2, 0, 9, 4, 16, 1, Blocks.COBBLESTONE);
        fill(world, bounds, 2, 0, 14, 4, 16, 1, Blocks.COBBLESTONE);
        fill(world, bounds, 1, 0, 10, 1, 16, 4, Blocks.COBBLESTONE);
        fill(world, bounds, 6, 0, 10, 1, 16, 4, Blocks.COBBLESTONE);
        fill(world, bounds, 2, 0, 10, 4, 1, 4, Blocks.COBBLESTONE);
        fill(world, bounds, 1, 4, 9, 6, 1, 6, Blocks.COBBLESTONE);
        fill(world, bounds, 1, 9, 9, 6, 1, 6, Blocks.COBBLESTONE);
        fill(world, bounds, 1, 14, 9, 6, 1, 6, Blocks.COBBLESTONE);
        fill(world, bounds, 3, 16, 9, 2, 1, 1, Blocks.COBBLESTONE);
        fill(world, bounds, 3, 16, 14, 2, 1, 1, Blocks.COBBLESTONE);
        fill(world, bounds, 1, 16, 11, 1, 1, 2, Blocks.COBBLESTONE);
        fill(world, bounds, 6, 16, 11, 1, 1, 2, Blocks.COBBLESTONE);
        fill(world, bounds, 3, 1, 14, 2, 3, 1, Blocks.LOG);
        fill(world, bounds, 1, 1, 11, 1, 3, 2, Blocks.LOG);
        fill(world, bounds, 3, 11, 9, 2, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 3, 6, 14, 2, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 3, 11, 14, 2, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 1, 6, 11, 1, 2, 2, Blocks.IRON_BARS);
        fill(world, bounds, 1, 11, 11, 1, 2, 2, Blocks.IRON_BARS);
        fill(world, bounds, 6, 11, 11, 1, 2, 2, Blocks.IRON_BARS);
        fill(world, bounds, 4, 1, 9, 1, 2, 1, Blocks.AIR);
        fill(world, bounds, 4, 5, 9, 1, 2, 1, Blocks.AIR);
        fill(world, bounds, 6, 1, 11, 1, 2, 1, Blocks.AIR);
        fill(world, bounds, 6, 5, 11, 1, 2, 1, Blocks.AIR);
        placeStructureLog(world, 8, 4, 7, 9, bounds);
        placeStructureLog(world, 4, 6, 7, 11, bounds);

        for (int h = 1; h <= 14; h++)
        {
            placeBlock(world, Blocks.LADDER, 2, 2, h, 10, bounds);
        }
        placeTorch(world, 2, 2, 13, bounds);
        placeTorch(world, 2, 6, 13, bounds);
        placeTorch(world, 2, 11, 13, bounds);

        fill(world, bounds, 11, 0, 9, 3, 19, 1, Blocks.COBBLESTONE);
        fill(world, bounds, 11, 0, 13, 3, 19, 1, Blocks.COBBLESTONE);
        fill(world, bounds, 10, 0, 10, 1, 19, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 14, 0, 10, 1, 19, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 11, 0, 10, 3, 1, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 10, 4, 9, 5, 1, 5, Blocks.COBBLESTONE);
        fill(world, bounds, 10, 9, 9, 5, 1, 5, Blocks.COBBLESTONE);
        fill(world, bounds, 10, 14, 9, 5, 1, 5, Blocks.COBBLESTONE);
        fill(world, bounds, 10, 19, 9, 5, 1, 5, Blocks.COBBLESTONE);
        fill(world, bounds, 12, 1, 13, 1, 3, 1, Blocks.LOG);
        fill(world, bounds, 14, 1, 11, 1, 3, 1, Blocks.LOG);
        fill(world, bounds, 12, 6, 13, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 12, 11, 9, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 12, 16, 9, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 12, 11, 13, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 12, 16, 13, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 14, 6, 11, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 14, 11, 11, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 14, 16, 11, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 10, 11, 11, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 10, 16, 11, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 12, 5, 9, 1, 2, 1, Blocks.AIR);
        fill(world, bounds, 12, 1, 9, 1, 2, 1, Blocks.AIR);
        fill(world, bounds, 10, 5, 11, 1, 2, 1, Blocks.AIR);
        fill(world, bounds, 10, 1, 11, 1, 2, 1, Blocks.AIR);
        placeStructureLog(world, 8, 12, 7, 9, bounds);
        placeStructureLog(world, 4, 10, 7, 11, bounds);

        for (int h = 1; h <= 14; h++)
        {
            placeBlock(world, Blocks.LADDER, 2, 11, h, 10, bounds);
        }
        placeTorch(world, 11, 2, 12, bounds);
        placeTorch(world, 11, 6, 12, bounds);
        placeTorch(world, 11, 11, 12, bounds);
        placeTorch(world, 11, 16, 12, bounds);
        placeBlock(world, Blocks.LOG, 0, 11, 19, 10, bounds);

        fill(world, bounds, 10, 20, 9, 5, 2, 5, Blocks.PLANKS);
        fill(world, bounds, 11, 22, 10, 3, 2, 3, Blocks.PLANKS);
        fill(world, bounds, 12, 24, 11, 1, 2, 1, Blocks.PLANKS);
        fill(world, bounds, 11, 20, 10, 3, 2, 3, Blocks.AIR);

        int n = 3;
        int s = 2;
        int w = 0;
        int e = 1;
        for (int x = 9; x <= 15; x++)
        {
            placeBlock(world, Blocks.OAK_STAIRS, n, x, 20, 8, bounds);
            placeBlock(world, Blocks.OAK_STAIRS, s, x, 20, 14, bounds);
        }
        for (int x = 10; x <= 14; x++)
        {
            placeBlock(world, Blocks.OAK_STAIRS, n, x, 22, 9, bounds);
            placeBlock(world, Blocks.OAK_STAIRS, s, x, 22, 13, bounds);
        }
        for (int x = 11; x <= 13; x++)
        {
            placeBlock(world, Blocks.OAK_STAIRS, n, x, 24, 10, bounds);
            placeBlock(world, Blocks.OAK_STAIRS, s, x, 24, 12, bounds);
        }
        for (int z = 9; z <= 13; z++)
        {
            placeBlock(world, Blocks.OAK_STAIRS, w, 9, 20, z, bounds);
            placeBlock(world, Blocks.OAK_STAIRS, e, 15, 20, z, bounds);
        }
        for (int z = 10; z <= 12; z++)
        {
            placeBlock(world, Blocks.OAK_STAIRS, w, 10, 22, z, bounds);
            placeBlock(world, Blocks.OAK_STAIRS, e, 14, 22, z, bounds);
        }
        placeBlock(world, Blocks.OAK_STAIRS, w, 11, 24, 11, bounds);
        placeBlock(world, Blocks.OAK_STAIRS, e, 13, 24, 11, bounds);

        fill(world, bounds, 7, 0, 11, 3, 1, 2, Blocks.COBBLESTONE);
        fill(world, bounds, 7, 4, 11, 3, 1, 1, Blocks.COBBLESTONE);
        fill(world, bounds, 7, 1, 12, 3, 5, 1, Blocks.COBBLESTONE);
        placeBlock(world, Blocks.COBBLESTONE, 0, 8, 6, 12, bounds);
        fill(world, bounds, 7, 1, 12, 1, 4, 1, Blocks.LOG);
        placeTorch(world, 8, 2, 11, bounds);
        fill(world, bounds, 9, 1, 12, 1, 4, 1, Blocks.LOG);
        int stoneStairMeta = 3;
        for (int x = 7; x <= 9; x++)
        {
            placeBlock(world, Blocks.STONE_BRICK_STAIRS, stoneStairMeta, x, 0, 10, bounds);
        }
        placeBlock(world, Blocks.STONE_SLAB, 11, 7, 3, 11, bounds);
        placeBlock(world, Blocks.STONE_SLAB, 11, 9, 3, 11, bounds);

        fill(world, bounds, 3, 0, 6, 2, 1, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 4, 4, 6, 1, 1, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 3, 1, 6, 1, 5, 3, Blocks.COBBLESTONE);
        placeBlock(world, Blocks.COBBLESTONE, 0, 3, 6, 7, bounds);
        fill(world, bounds, 3, 1, 6, 1, 4, 1, Blocks.LOG);
        placeTorch(world, 4, 2, 7, bounds);
        fill(world, bounds, 3, 1, 8, 1, 4, 1, Blocks.LOG);
        int eastStairMeta = 1;
        for (int z = 6; z <= 8; z++)
        {
            placeBlock(world, Blocks.STONE_BRICK_STAIRS, eastStairMeta, 5, 0, z, bounds);
        }
        placeBlock(world, Blocks.STONE_SLAB, 11, 4, 3, 6, bounds);
        placeBlock(world, Blocks.STONE_SLAB, 11, 4, 3, 8, bounds);

        fill(world, bounds, 12, 0, 6, 2, 1, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 12, 4, 6, 1, 1, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 13, 1, 6, 1, 5, 3, Blocks.COBBLESTONE);
        placeBlock(world, Blocks.COBBLESTONE, 0, 13, 6, 7, bounds);
        fill(world, bounds, 13, 1, 6, 1, 4, 1, Blocks.LOG);
        placeTorch(world, 12, 2, 7, bounds);
        fill(world, bounds, 13, 1, 8, 1, 4, 1, Blocks.LOG);
        int westStairMeta = 0;
        for (int z = 6; z <= 8; z++)
        {
            placeBlock(world, Blocks.STONE_BRICK_STAIRS, westStairMeta, 11, 0, z, bounds);
        }
        placeBlock(world, Blocks.STONE_SLAB, 11, 12, 3, 6, bounds);
        placeBlock(world, Blocks.STONE_SLAB, 11, 12, 3, 8, bounds);

        if (!hasMadeChest)
        {
            BlockPos chestPos = new BlockPos(getXWithOffset(13, 12), getYWithOffset(20), getZWithOffset(13, 12));
            if (bounds.isVecInside(chestPos))
            {
                hasMadeChest = true;
                generateChest(world, bounds, rand, 13, 20, 12, LootTableList.CHESTS_VILLAGE_BLACKSMITH);
            }
        }

        IBlockState cobble = getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
        for (int j = 1; j < 15; j++)
        {
            for (int k = 1; k < 15; k++)
            {
                clearCurrentPositionBlocksUpwards(world, k, 26, j, bounds);
                replaceAirAndLiquidDownwards(world, cobble, k, -1, j, bounds);
            }
        }

        guardsGround = spawnGuardsAt(world, bounds, 7, 1, 7, 2, guardsGround);
        guardsMidLeft = spawnGuardsAt(world, bounds, 5, 10, 4, 3, guardsMidLeft);
        guardsMidRight = spawnGuardsAt(world, bounds, 13, 10, 4, 3, guardsMidRight);

        return true;
    }

    private int spawnGuardsAt(World world, StructureBoundingBox bounds, int x, int y, int z, int count, int alreadySpawned)
    {
        final int[] spawned = {alreadySpawned};
        StructureGuardSpawner.spawnGuards(world, bounds, alreadySpawned, x, y, z, count, new StructureGuardSpawner.GuardSpawnCallback()
        {
            @Override
            public int getX(int localX, int localZ)
            {
                return ComponentVillageKeep.this.getXWithOffset(localX, localZ);
            }

            @Override
            public int getY(int localY)
            {
                return ComponentVillageKeep.this.getYWithOffset(localY);
            }

            @Override
            public int getZ(int localX, int localZ)
            {
                return ComponentVillageKeep.this.getZWithOffset(localX, localZ);
            }

            @Override
            public void onSpawned()
            {
                spawned[0]++;
            }
        });
        return spawned[0];
    }

    public void drawTower(World world, StructureBoundingBox bounds, int offsetX, int flipX)
    {
        fill(world, bounds, 3 + offsetX, 0, 1, 3, 11, 1, Blocks.COBBLESTONE);
        fill(world, bounds, 3 + offsetX, 0, 5, 3, 11, 1, Blocks.COBBLESTONE);
        fill(world, bounds, 2 + offsetX, 0, 2, 1, 11, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 6 + offsetX, 0, 2, 1, 11, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 3 + offsetX, 0, 2, 3, 1, 3, Blocks.COBBLESTONE);
        fill(world, bounds, 2 + offsetX, 4, 1, 5, 1, 5, Blocks.COBBLESTONE);
        fill(world, bounds, 2 + offsetX, 9, 1, 5, 1, 5, Blocks.COBBLESTONE);
        placeBlock(world, Blocks.COBBLESTONE, 0, 4 + offsetX, 11, 1, bounds);
        placeBlock(world, Blocks.COBBLESTONE, 0, 4 + offsetX, 11, 5, bounds);
        placeBlock(world, Blocks.COBBLESTONE, 0, 2 + offsetX, 11, 3, bounds);
        placeBlock(world, Blocks.COBBLESTONE, 0, 6 + offsetX, 11, 3, bounds);
        fill(world, bounds, 4 + offsetX, 1, 1, 1, 3, 1, Blocks.LOG);
        fill(world, bounds, 2 + offsetX + flipX, 1, 3, 1, 3, 1, Blocks.LOG);
        fill(world, bounds, 4 + offsetX, 6, 1, 1, 2, 1, Blocks.IRON_BARS);
        fill(world, bounds, 2 + offsetX + flipX, 6, 3, 1, 2, 1, Blocks.IRON_BARS);
        placeStructureLog(world, 8, 4 + offsetX, 7, 5, bounds);
        placeStructureLog(world, 4, 6 + offsetX - flipX, 7, 3, bounds);
        fill(world, bounds, 4 + offsetX, 5, 5, 1, 2, 1, Blocks.AIR);
        fill(world, bounds, 4 + offsetX, 1, 5, 1, 2, 1, Blocks.AIR);
        fill(world, bounds, 6 + offsetX - flipX, 5, 3, 1, 2, 1, Blocks.AIR);

        for (int h = 1; h <= 9; h++)
        {
            placeBlock(world, Blocks.LADDER, 2, 3 + offsetX, h, 2, bounds);
        }
        placeTorch(world, 3 + offsetX, 2, 4, bounds);
        placeTorch(world, 3 + offsetX, 6, 4, bounds);
    }

}
