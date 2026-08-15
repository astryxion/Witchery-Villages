package com.witcherywalls.worldgen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public final class VillageKeepGenerator
{
    private VillageKeepGenerator()
    {
    }

    public static void generate(VillagePieceBuilder builder, Random random)
    {
        BlockState air = Blocks.AIR.defaultBlockState();
        builder.fillInclusive(1, 1, 1, 14, 26, 14, air);

        drawTower(builder, 0, 0);
        drawTower(builder, 8, 4);

        builder.fill(7, 0, 2, 3, 1, 3, Blocks.COBBLESTONE);
        builder.fill(7, 4, 3, 3, 1, 2, Blocks.COBBLESTONE);
        builder.fill(7, 5, 2, 3, 1, 1, Blocks.COBBLESTONE);
        builder.placeBlock(Blocks.COBBLESTONE, 0, 8, 6, 2);

        int logMeta = 8;
        for (int x = 7; x <= 9; x++)
        {
            builder.placeBlock(Blocks.OAK_LOG, logMeta, x, 4, 2);
        }

        Block fenceBlock = builder.palette().apply(Blocks.OAK_FENCE.defaultBlockState()).getBlock();
        for (int fx = 7; fx <= 9; fx++)
        {
            boolean east = fx < 9;
            boolean west = fx > 7;
            builder.placeFencePost(fenceBlock, false, false, east, west, fx, 3, 3);
        }
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 7, 3, 2);
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 7, 3, 4);
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 9, 3, 2);
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 9, 3, 4);

        int stairMetaN = 3;
        int stairMetaS = 2;
        for (int x = 7; x <= 9; x++)
        {
            builder.placeBlock(Blocks.STONE_BRICK_STAIRS, stairMetaN, x, 0, 1);
            builder.placeBlock(Blocks.STONE_BRICK_STAIRS, stairMetaS, x, 0, 4);
        }

        builder.fill(2, 0, 9, 4, 16, 1, Blocks.COBBLESTONE);
        builder.fill(2, 0, 14, 4, 16, 1, Blocks.COBBLESTONE);
        builder.fill(1, 0, 10, 1, 16, 4, Blocks.COBBLESTONE);
        builder.fill(6, 0, 10, 1, 16, 4, Blocks.COBBLESTONE);
        builder.fill(2, 0, 10, 4, 1, 4, Blocks.COBBLESTONE);
        builder.fill(1, 4, 9, 6, 1, 6, Blocks.COBBLESTONE);
        builder.fill(1, 9, 9, 6, 1, 6, Blocks.COBBLESTONE);
        builder.fill(1, 14, 9, 6, 1, 6, Blocks.COBBLESTONE);
        builder.fill(3, 16, 9, 2, 1, 1, Blocks.COBBLESTONE);
        builder.fill(3, 16, 14, 2, 1, 1, Blocks.COBBLESTONE);
        builder.fill(1, 16, 11, 1, 1, 2, Blocks.COBBLESTONE);
        builder.fill(6, 16, 11, 1, 1, 2, Blocks.COBBLESTONE);
        builder.fill(3, 1, 14, 2, 3, 1, Blocks.OAK_LOG);
        builder.fill(1, 1, 11, 1, 3, 2, Blocks.OAK_LOG);
        builder.fill(3, 11, 9, 2, 2, 1, Blocks.IRON_BARS);
        builder.fill(3, 6, 14, 2, 2, 1, Blocks.IRON_BARS);
        builder.fill(3, 11, 14, 2, 2, 1, Blocks.IRON_BARS);
        builder.fill(1, 6, 11, 1, 2, 2, Blocks.IRON_BARS);
        builder.fill(1, 11, 11, 1, 2, 2, Blocks.IRON_BARS);
        builder.fill(6, 11, 11, 1, 2, 2, Blocks.IRON_BARS);
        builder.fill(4, 1, 9, 1, 2, 1, Blocks.AIR);
        builder.fill(4, 5, 9, 1, 2, 1, Blocks.AIR);
        builder.fill(6, 1, 11, 1, 2, 1, Blocks.AIR);
        builder.fill(6, 5, 11, 1, 2, 1, Blocks.AIR);
        builder.placeStructureLog(8, 4, 7, 9);
        builder.placeStructureLog(4, 6, 7, 11);

        for (int h = 1; h <= 14; h++)
        {
            builder.placeBlock(Blocks.LADDER, 2, 2, h, 10);
        }
        builder.placeTorch(2, 2, 13);
        builder.placeTorch(2, 6, 13);
        builder.placeTorch(2, 11, 13);

        builder.fill(11, 0, 9, 3, 19, 1, Blocks.COBBLESTONE);
        builder.fill(11, 0, 13, 3, 19, 1, Blocks.COBBLESTONE);
        builder.fill(10, 0, 10, 1, 19, 3, Blocks.COBBLESTONE);
        builder.fill(14, 0, 10, 1, 19, 3, Blocks.COBBLESTONE);
        builder.fill(11, 0, 10, 3, 1, 3, Blocks.COBBLESTONE);
        builder.fill(10, 4, 9, 5, 1, 5, Blocks.COBBLESTONE);
        builder.fill(10, 9, 9, 5, 1, 5, Blocks.COBBLESTONE);
        builder.fill(10, 14, 9, 5, 1, 5, Blocks.COBBLESTONE);
        builder.fill(10, 19, 9, 5, 1, 5, Blocks.COBBLESTONE);
        builder.fill(12, 1, 13, 1, 3, 1, Blocks.OAK_LOG);
        builder.fill(14, 1, 11, 1, 3, 1, Blocks.OAK_LOG);
        builder.fill(12, 6, 13, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(12, 11, 9, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(12, 16, 9, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(12, 11, 13, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(12, 16, 13, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(14, 6, 11, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(14, 11, 11, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(14, 16, 11, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(10, 11, 11, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(10, 16, 11, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(12, 5, 9, 1, 2, 1, Blocks.AIR);
        builder.fill(12, 1, 9, 1, 2, 1, Blocks.AIR);
        builder.fill(10, 5, 11, 1, 2, 1, Blocks.AIR);
        builder.fill(10, 1, 11, 1, 2, 1, Blocks.AIR);
        builder.placeStructureLog(8, 12, 7, 9);
        builder.placeStructureLog(4, 10, 7, 11);

        for (int h = 1; h <= 14; h++)
        {
            builder.placeBlock(Blocks.LADDER, 2, 11, h, 10);
        }
        builder.placeTorch(11, 2, 12);
        builder.placeTorch(11, 6, 12);
        builder.placeTorch(11, 11, 12);
        builder.placeTorch(11, 16, 12);
        builder.placeBlock(Blocks.OAK_LOG, 0, 11, 19, 10);

        builder.fill(10, 20, 9, 5, 2, 5, Blocks.OAK_PLANKS);
        builder.fill(11, 22, 10, 3, 2, 3, Blocks.OAK_PLANKS);
        builder.fill(12, 24, 11, 1, 2, 1, Blocks.OAK_PLANKS);
        builder.fill(11, 20, 10, 3, 2, 3, Blocks.AIR);

        int n = 3;
        int s = 2;
        int w = 0;
        int e = 1;
        for (int x = 9; x <= 15; x++)
        {
            builder.placeBlock(Blocks.OAK_STAIRS, n, x, 20, 8);
            builder.placeBlock(Blocks.OAK_STAIRS, s, x, 20, 14);
        }
        for (int x = 10; x <= 14; x++)
        {
            builder.placeBlock(Blocks.OAK_STAIRS, n, x, 22, 9);
            builder.placeBlock(Blocks.OAK_STAIRS, s, x, 22, 13);
        }
        for (int x = 11; x <= 13; x++)
        {
            builder.placeBlock(Blocks.OAK_STAIRS, n, x, 24, 10);
            builder.placeBlock(Blocks.OAK_STAIRS, s, x, 24, 12);
        }
        for (int z = 9; z <= 13; z++)
        {
            builder.placeBlock(Blocks.OAK_STAIRS, w, 9, 20, z);
            builder.placeBlock(Blocks.OAK_STAIRS, e, 15, 20, z);
        }
        for (int z = 10; z <= 12; z++)
        {
            builder.placeBlock(Blocks.OAK_STAIRS, w, 10, 22, z);
            builder.placeBlock(Blocks.OAK_STAIRS, e, 14, 22, z);
        }
        builder.placeBlock(Blocks.OAK_STAIRS, w, 11, 24, 11);
        builder.placeBlock(Blocks.OAK_STAIRS, e, 13, 24, 11);

        builder.fill(7, 0, 11, 3, 1, 2, Blocks.COBBLESTONE);
        builder.fill(7, 4, 11, 3, 1, 1, Blocks.COBBLESTONE);
        builder.fill(7, 1, 12, 3, 5, 1, Blocks.COBBLESTONE);
        builder.placeBlock(Blocks.COBBLESTONE, 0, 8, 6, 12);
        builder.fill(7, 1, 12, 1, 4, 1, Blocks.OAK_LOG);
        builder.placeTorch(8, 2, 11);
        builder.fill(9, 1, 12, 1, 4, 1, Blocks.OAK_LOG);
        int stoneStairMeta = 3;
        for (int x = 7; x <= 9; x++)
        {
            builder.placeBlock(Blocks.STONE_BRICK_STAIRS, stoneStairMeta, x, 0, 10);
        }
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 7, 3, 11);
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 9, 3, 11);

        builder.fill(3, 0, 6, 2, 1, 3, Blocks.COBBLESTONE);
        builder.fill(4, 4, 6, 1, 1, 3, Blocks.COBBLESTONE);
        builder.fill(3, 1, 6, 1, 5, 3, Blocks.COBBLESTONE);
        builder.placeBlock(Blocks.COBBLESTONE, 0, 3, 6, 7);
        builder.fill(3, 1, 6, 1, 4, 1, Blocks.OAK_LOG);
        builder.placeTorch(4, 2, 7);
        builder.fill(3, 1, 8, 1, 4, 1, Blocks.OAK_LOG);
        int eastStairMeta = 1;
        for (int z = 6; z <= 8; z++)
        {
            builder.placeBlock(Blocks.STONE_BRICK_STAIRS, eastStairMeta, 5, 0, z);
        }
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 4, 3, 6);
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 4, 3, 8);

        builder.fill(12, 0, 6, 2, 1, 3, Blocks.COBBLESTONE);
        builder.fill(12, 4, 6, 1, 1, 3, Blocks.COBBLESTONE);
        builder.fill(13, 1, 6, 1, 5, 3, Blocks.COBBLESTONE);
        builder.placeBlock(Blocks.COBBLESTONE, 0, 13, 6, 7);
        builder.fill(13, 1, 6, 1, 4, 1, Blocks.OAK_LOG);
        builder.placeTorch(12, 2, 7);
        builder.fill(13, 1, 8, 1, 4, 1, Blocks.OAK_LOG);
        int westStairMeta = 0;
        for (int z = 6; z <= 8; z++)
        {
            builder.placeBlock(Blocks.STONE_BRICK_STAIRS, westStairMeta, 11, 0, z);
        }
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 12, 3, 6);
        builder.placeBlock(Blocks.SMOOTH_STONE_SLAB, 11, 12, 3, 8);

        builder.placeChest(13, 20, 12, random);

        BlockState cobble = builder.palette().apply(Blocks.COBBLESTONE.defaultBlockState());
        for (int j = 1; j < 15; j++)
        {
            for (int k = 1; k < 15; k++)
            {
                builder.clearUpwards(k, 26, j);
                builder.fillDown(k, -1, j, cobble);
            }
        }

        builder.spawnGuards(7, 1, 7, 2, random, VillageGuardSpawnQueue.Site.KEEP);
        builder.spawnGuards(5, 10, 4, 3, random, VillageGuardSpawnQueue.Site.KEEP);
        builder.spawnGuards(13, 10, 4, 3, random, VillageGuardSpawnQueue.Site.KEEP);
    }

    private static void drawTower(VillagePieceBuilder builder, int offsetX, int flipX)
    {
        builder.fill(3 + offsetX, 0, 1, 3, 11, 1, Blocks.COBBLESTONE);
        builder.fill(3 + offsetX, 0, 5, 3, 11, 1, Blocks.COBBLESTONE);
        builder.fill(2 + offsetX, 0, 2, 1, 11, 3, Blocks.COBBLESTONE);
        builder.fill(6 + offsetX, 0, 2, 1, 11, 3, Blocks.COBBLESTONE);
        builder.fill(3 + offsetX, 0, 2, 3, 1, 3, Blocks.COBBLESTONE);
        builder.fill(2 + offsetX, 4, 1, 5, 1, 5, Blocks.COBBLESTONE);
        builder.fill(2 + offsetX, 9, 1, 5, 1, 5, Blocks.COBBLESTONE);
        builder.placeBlock(Blocks.COBBLESTONE, 0, 4 + offsetX, 11, 1);
        builder.placeBlock(Blocks.COBBLESTONE, 0, 4 + offsetX, 11, 5);
        builder.placeBlock(Blocks.COBBLESTONE, 0, 2 + offsetX, 11, 3);
        builder.placeBlock(Blocks.COBBLESTONE, 0, 6 + offsetX, 11, 3);
        builder.fill(4 + offsetX, 1, 1, 1, 3, 1, Blocks.OAK_LOG);
        builder.fill(2 + offsetX + flipX, 1, 3, 1, 3, 1, Blocks.OAK_LOG);
        builder.fill(4 + offsetX, 6, 1, 1, 2, 1, Blocks.IRON_BARS);
        builder.fill(2 + offsetX + flipX, 6, 3, 1, 2, 1, Blocks.IRON_BARS);
        builder.placeStructureLog(8, 4 + offsetX, 7, 5);
        builder.placeStructureLog(4, 6 + offsetX - flipX, 7, 3);
        builder.fill(4 + offsetX, 5, 5, 1, 2, 1, Blocks.AIR);
        builder.fill(4 + offsetX, 1, 5, 1, 2, 1, Blocks.AIR);
        builder.fill(6 + offsetX - flipX, 5, 3, 1, 2, 1, Blocks.AIR);

        for (int h = 1; h <= 9; h++)
        {
            builder.placeBlock(Blocks.LADDER, 2, 3 + offsetX, h, 2);
        }
        builder.placeTorch(3 + offsetX, 2, 4);
        builder.placeTorch(3 + offsetX, 6, 4);
    }
}
