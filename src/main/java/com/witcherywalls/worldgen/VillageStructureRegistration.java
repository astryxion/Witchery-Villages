package com.witcherywalls.worldgen;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.config.WitcheryWallsConfig;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;

import java.util.List;
import java.util.Random;

public final class VillageStructureRegistration
{
    private static final int GUARD_TOWER_WEIGHT = 4;
    private static final int GUARD_TOWER_MIN = 1;
    private static final int GUARD_TOWER_MAX = 3;

    private static final int TOWN_WALL_WEIGHT = 100;
    private static final int TOWN_KEEP_WEIGHT = 100;

    private VillageStructureRegistration()
    {
    }

    public static void preInit()
    {
        MapGenStructureIO.registerStructureComponent(VillageWallPiece.class, WitcheryWallsMod.MODID + ":village_wall");
        MapGenStructureIO.registerStructureComponent(ComponentVillageKeep.class, WitcheryWallsMod.MODID + ":village_keep");
        MapGenStructureIO.registerStructureComponent(ComponentVillageWatchTower.class, WitcheryWallsMod.MODID + ":village_watch_tower");

        registerComponent(VillageWallPiece.class, TOWN_WALL_WEIGHT, 1, 1);
        registerComponent(ComponentVillageKeep.class, TOWN_KEEP_WEIGHT, 1, 1);
        registerComponent(ComponentVillageWatchTower.class, GUARD_TOWER_WEIGHT, GUARD_TOWER_MIN, GUARD_TOWER_MAX);
    }

    public static void init()
    {
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection())
        {
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.WET)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.END)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.WATER))
            {
                continue;
            }

            BiomeManager.addVillageBiome(biome, true);
        }
    }

    private static void registerComponent(Class<? extends StructureVillagePieces.Village> clazz, int weight, int min, int max)
    {
        VillagerRegistry.instance().registerVillageCreationHandler(new VillageCreationHandler(clazz, weight, min, max));
    }

    private static class VillageCreationHandler implements VillagerRegistry.IVillageCreationHandler
    {
        private final Class<? extends StructureVillagePieces.Village> pieceClass;
        private final int weight;
        private final int min;
        private final int max;

        VillageCreationHandler(Class<? extends StructureVillagePieces.Village> pieceClass, int weight, int min, int max)
        {
            this.pieceClass = pieceClass;
            this.weight = weight;
            this.min = min;
            this.max = max;
        }

        @Override
        public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int size)
        {
            if (!WitcheryWallsConfig.roll(random, chanceFor(pieceClass)))
            {
                return new StructureVillagePieces.PieceWeight(pieceClass, weight, 0);
            }
            int count = max <= min ? min : min + random.nextInt(max - min + 1);
            return new StructureVillagePieces.PieceWeight(pieceClass, weight, count);
        }

        private static int chanceFor(Class<?> clazz)
        {
            if (clazz == VillageWallPiece.class)
            {
                return WitcheryWallsConfig.generation.wallSpawnChance;
            }
            if (clazz == ComponentVillageKeep.class)
            {
                return WitcheryWallsConfig.generation.keepSpawnChance;
            }
            if (clazz == ComponentVillageWatchTower.class)
            {
                return WitcheryWallsConfig.generation.watchTowerSpawnChance;
            }
            return 100;
        }

        @Override
        public Class<?> getComponentClass()
        {
            return pieceClass;
        }

        @Override
        public StructureVillagePieces.Village buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece,
                                                             List<StructureComponent> pieces, Random random,
                                                             int p1, int p2, int p3, EnumFacing facing, int p5)
        {
            int facingIndex = facing.getHorizontalIndex() & 3;

            if (pieceClass == VillageWallPiece.class)
            {
                return VillageWallPiece.create(startPiece, pieces, random, p1, p2, p3, facingIndex, p5);
            }
            if (pieceClass == ComponentVillageWatchTower.class)
            {
                return ComponentVillageWatchTower.construct(startPiece, pieces, random, p1, p2, p3, facingIndex, p5);
            }
            if (pieceClass == ComponentVillageKeep.class)
            {
                return ComponentVillageKeep.construct(startPiece, pieces, random, p1, p2, p3, facingIndex, p5);
            }
            return null;
        }
    }
}
