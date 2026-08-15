package com.witcherywalls.worldgen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public final class VillageWatchTowerGenerator
{
    private VillageWatchTowerGenerator()
    {
    }

    public static void generate(VillagePieceBuilder builder, Random random)
    {
        BlockState cobble = builder.palette().apply(Blocks.COBBLESTONE.defaultBlockState());
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState fence = builder.palette().apply(Blocks.OAK_FENCE.defaultBlockState());
        BlockState plank = builder.palette().apply(Blocks.OAK_PLANKS.defaultBlockState());
        BlockState oakStairs = builder.palette().apply(Blocks.OAK_STAIRS.defaultBlockState());

        builder.fillInclusive(2, 0, 2, 6, 17, 6, cobble);
        builder.fillInclusive(3, 13, 3, 5, 14, 5, air);
        builder.fillInclusive(2, 16, 3, 6, 17, 5, air);
        builder.fillInclusive(3, 16, 2, 5, 17, 6, air);

        builder.fillInclusive(3, 15, 1, 5, 16, 1, cobble);
        builder.fillInclusive(4, 14, 1, 4, 17, 1, cobble);
        builder.fillInclusive(3, 15, 7, 5, 16, 7, cobble);
        builder.fillInclusive(4, 14, 7, 4, 17, 7, cobble);
        builder.fillInclusive(1, 15, 3, 1, 16, 5, cobble);
        builder.fillInclusive(1, 14, 4, 1, 17, 4, cobble);
        builder.fillInclusive(7, 15, 3, 7, 16, 5, cobble);
        builder.fillInclusive(7, 14, 4, 7, 17, 4, cobble);

        Block fenceBlock = fence.getBlock();
        builder.placeFencePost(fenceBlock, false, false, false, false, 2, 18, 2);
        builder.placeFencePost(fenceBlock, false, false, false, false, 2, 18, 6);
        builder.placeFencePost(fenceBlock, false, false, false, false, 6, 18, 2);
        builder.placeFencePost(fenceBlock, false, false, false, false, 6, 18, 6);

        builder.fillInclusive(2, 19, 2, 6, 19, 6, plank);
        builder.fillInclusive(3, 20, 3, 5, 20, 5, plank);
        builder.placeBiomeBlock(plank, 0, 4, 19, 4);

        Block oakStairsBlock = oakStairs.getBlock();
        int n = 3;
        int s = 2;
        int w = 0;
        int e = 1;

        for (int sx = 2; sx <= 6; sx++)
        {
            builder.placeBlock(oakStairsBlock, n, sx, 19, 1);
        }
        for (int sx = 2; sx <= 6; sx++)
        {
            builder.placeBlock(oakStairsBlock, n, sx, 20, 2);
        }
        for (int sx = 3; sx <= 5; sx++)
        {
            builder.placeBlock(oakStairsBlock, n, sx, 21, 3);
        }

        for (int sx = 2; sx <= 6; sx++)
        {
            builder.placeBlock(oakStairsBlock, s, sx, 19, 7);
        }
        for (int sx = 2; sx <= 6; sx++)
        {
            builder.placeBlock(oakStairsBlock, s, sx, 20, 6);
        }
        for (int sx = 3; sx <= 5; sx++)
        {
            builder.placeBlock(oakStairsBlock, s, sx, 21, 5);
        }

        for (int sz = 2; sz <= 6; sz++)
        {
            builder.placeBlock(oakStairsBlock, w, 1, 19, sz);
        }
        for (int sz = 2; sz <= 6; sz++)
        {
            builder.placeBlock(oakStairsBlock, w, 2, 20, sz);
        }
        for (int sz = 3; sz <= 5; sz++)
        {
            builder.placeBlock(oakStairsBlock, w, 3, 21, sz);
        }

        for (int sz = 2; sz <= 6; sz++)
        {
            builder.placeBlock(oakStairsBlock, e, 7, 19, sz);
        }
        for (int sz = 2; sz <= 6; sz++)
        {
            builder.placeBlock(oakStairsBlock, e, 6, 20, sz);
        }
        for (int sz = 3; sz <= 5; sz++)
        {
            builder.placeBlock(oakStairsBlock, e, 5, 21, sz);
        }

        builder.placeBlock(Blocks.OAK_SLAB, 0, 4, 22, 4);

        builder.fillInclusive(4, 1, 2, 4, 2, 3, air);
        builder.placeTorch(3, 2, 4);
        builder.placeTorch(5, 2, 4);
        builder.placeTorch(4, 14, 3);
        builder.placeTorch(4, 16, 4);

        builder.fillInclusive(2, 6, 2, 2, 14, 2, air);
        builder.fillInclusive(6, 6, 2, 6, 14, 2, air);
        builder.fillInclusive(6, 6, 6, 6, 14, 6, air);
        builder.fillInclusive(2, 6, 6, 2, 14, 6, air);
        builder.fillInclusive(4, 6, 2, 4, 12, 2, air);
        builder.fillInclusive(4, 6, 6, 4, 12, 6, air);
        builder.fillInclusive(6, 6, 4, 6, 12, 4, air);
        builder.fillInclusive(2, 6, 4, 2, 12, 4, air);
        builder.fillInclusive(2, 9, 2, 6, 9, 6, cobble);

        builder.fillInclusive(3, 0, 1, 5, 4, 1, cobble);
        builder.fillInclusive(4, 1, 1, 4, 3, 1, air);
        builder.fillInclusive(3, 0, 7, 5, 4, 7, cobble);
        builder.fillInclusive(4, 1, 7, 4, 3, 7, air);
        builder.fillInclusive(1, 0, 3, 1, 4, 5, cobble);
        builder.fillInclusive(1, 1, 4, 1, 3, 4, air);
        builder.fillInclusive(7, 0, 3, 7, 4, 5, cobble);
        builder.fillInclusive(7, 1, 4, 7, 3, 4, air);

        for (int i = 1; i <= 12; i++)
        {
            builder.placeBlock(Blocks.LADDER, 3, 4, i, 4);
        }
        for (int i = 13; i <= 15; i++)
        {
            builder.placeBlock(Blocks.LADDER, 3, 3, i, 5);
        }

        builder.placeChest(5, 13, 5, random);

        for (int j = 1; j < 7; j++)
        {
            for (int k = 1; k < 7; k++)
            {
                builder.clearUpwards(k, 23, j);
                builder.fillDown(k, -1, j, cobble);
            }
        }

        builder.spawnGuards(4, 16, 4, 2, random, VillageGuardSpawnQueue.Site.TOWER);
    }
}
