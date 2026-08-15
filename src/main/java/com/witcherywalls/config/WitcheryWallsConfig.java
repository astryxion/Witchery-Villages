package com.witcherywalls.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;

import java.util.Random;

public final class WitcheryWallsConfig
{
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.IntValue WALL_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue KEEP_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue WATCH_TOWER_SPAWN_CHANCE;
    public static final ForgeConfigSpec.BooleanValue SPAWN_VILLAGE_GUARDS;
    public static final ForgeConfigSpec.BooleanValue VILLAGE_GUARDS_DEFAULT_APPLIED;

    static
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Village generation chances. Each village rolls once; 100 is always, 0 is never.")
                .push("generation");

        WALL_SPAWN_CHANCE = builder
                .comment("Chance for a village to generate town walls.")
                .defineInRange("wallSpawnChance", 50, 0, 100);

        KEEP_SPAWN_CHANCE = builder
                .comment("Chance for a village to generate a town keep.")
                .defineInRange("keepSpawnChance", 50, 0, 100);

        WATCH_TOWER_SPAWN_CHANCE = builder
                .comment("Chance for a village to generate watch towers.")
                .defineInRange("watchTowerSpawnChance", 50, 0, 100);

        builder.pop();

        builder.comment("Witchery village guards. These are separate from the Guard Villagers mod.")
                .push("guards");

        SPAWN_VILLAGE_GUARDS = builder
                .comment("Spawn Witchery village guards on keeps, towers, walls, and streets.",
                        "Also controls the spawn egg and /summon witcherywalls:village_guard.",
                        "If Guard Villagers is also installed, the first launch leaves this off so those guards are used instead.",
                        "Turn this on to let both kinds of guards spawn together. After you change it, it will not be overwritten.")
                .define("spawnVillageGuards", false);

        VILLAGE_GUARDS_DEFAULT_APPLIED = builder
                .comment("Set automatically on first launch. Leave this true so your spawnVillageGuards choice is kept.")
                .define("villageGuardsDefaultApplied", false);

        builder.pop();
        COMMON_SPEC = builder.build();
    }

    private WitcheryWallsConfig()
    {
    }

    public static boolean spawnVillageGuards()
    {
        return COMMON_SPEC.isLoaded() && SPAWN_VILLAGE_GUARDS.get();
    }

    public static void applyGuardCompatibilityDefault(ModConfig config)
    {
        if (VILLAGE_GUARDS_DEFAULT_APPLIED.get())
        {
            return;
        }

        boolean guardVillagersLoaded = ModList.get().isLoaded("guardvillagers");
        SPAWN_VILLAGE_GUARDS.set(!guardVillagersLoaded);
        VILLAGE_GUARDS_DEFAULT_APPLIED.set(true);
        config.save();

        if (guardVillagersLoaded)
        {
            LogManager.getLogger().info(
                    "Guard Villagers is installed, so Witchery village guards are off. Set spawnVillageGuards to true to use both.");
        }
        else
        {
            LogManager.getLogger().info("Witchery village guards enabled (Guard Villagers was not found).");
        }
    }

    public static boolean roll(Random random, ForgeConfigSpec.IntValue chance)
    {
        int value = chance.get();
        if (value >= 100)
        {
            return true;
        }
        if (value <= 0)
        {
            return false;
        }
        return random.nextInt(100) < value;
    }
}
