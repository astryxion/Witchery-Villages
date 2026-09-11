package com.witcherywalls.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class WitcheryWallsConfig
{
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.IntValue WALL_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue KEEP_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue WATCH_TOWER_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue VILLAGE_WALL_DEDUP_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> STRUCTURE_BLACKLIST;
    public static final ForgeConfigSpec.BooleanValue SPAWN_VILLAGE_GUARDS;
    public static final ForgeConfigSpec.BooleanValue VILLAGE_GUARDS_DEFAULT_APPLIED;

    private static ModConfig commonConfig;

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

        VILLAGE_WALL_DEDUP_RADIUS = builder
                .comment("Minimum distance between village centers that get town walls.",
                        "Nearby villages within this radius are skipped so dense dimensions",
                        "(e.g. Chaos Persists rainbow) do not stack overlapping wall rings.",
                        "Default 160; lower for more walls, higher for cleaner spacing.")
                .defineInRange("villageWallDedupRadius", 160, 40, 512);

        STRUCTURE_BLACKLIST = builder
                .comment("Structure IDs that should never receive walls, keeps, or towers.",
                        "Use full resource locations, one per entry (e.g. minecraft:village_plains).",
                        "Matching is case-insensitive.")
                .defineListAllowEmpty(
                        Collections.singletonList("structureBlacklist"),
                        ArrayList::new,
                        WitcheryWallsConfig::isValidStructureIdEntry);

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

    public static void captureConfig(ModConfig config)
    {
        if (config.getSpec() == COMMON_SPEC)
        {
            commonConfig = config;
        }
    }

    public static void save()
    {
        if (commonConfig != null)
        {
            commonConfig.save();
        }
    }

    public static boolean spawnVillageGuards()
    {
        return COMMON_SPEC.isLoaded() && SPAWN_VILLAGE_GUARDS.get();
    }

    public static int villageWallDedupRadius()
    {
        if (!COMMON_SPEC.isLoaded())
        {
            return 160;
        }
        return VILLAGE_WALL_DEDUP_RADIUS.get();
    }

    public static boolean isStructureBlacklisted(ResourceLocation structureId)
    {
        if (structureId == null || !COMMON_SPEC.isLoaded())
        {
            return false;
        }

        String needle = structureId.toString().toLowerCase(Locale.ROOT);
        for (String entry : STRUCTURE_BLACKLIST.get())
        {
            if (entry != null && needle.equals(entry.trim().toLowerCase(Locale.ROOT)))
            {
                return true;
            }
        }
        return false;
    }

    public static List<String> structureBlacklistEntries()
    {
        if (!COMMON_SPEC.isLoaded())
        {
            return List.of();
        }

        List<String> copy = new ArrayList<>();
        for (String entry : STRUCTURE_BLACKLIST.get())
        {
            if (entry != null && !entry.isBlank())
            {
                copy.add(entry.trim());
            }
        }
        return copy;
    }

    public static void setStructureBlacklist(List<String> entries)
    {
        List<String> cleaned = new ArrayList<>();
        for (String entry : entries)
        {
            if (entry == null)
            {
                continue;
            }
            String trimmed = entry.trim();
            if (!trimmed.isEmpty() && isValidStructureIdEntry(trimmed))
            {
                cleaned.add(trimmed);
            }
        }
        STRUCTURE_BLACKLIST.set(cleaned);
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

    private static boolean isValidStructureIdEntry(Object value)
    {
        if (!(value instanceof String text))
        {
            return false;
        }
        String trimmed = text.trim();
        return !trimmed.isEmpty() && ResourceLocation.isValidResourceLocation(trimmed);
    }
}
