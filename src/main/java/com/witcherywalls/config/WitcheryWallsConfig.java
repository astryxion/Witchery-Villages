package com.witcherywalls.config;

import com.witcherywalls.ModEntities;
import com.witcherywalls.WitcheryWallsMod;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

@Config(modid = WitcheryWallsMod.MODID, name = WitcheryWallsMod.MODID)
@Mod.EventBusSubscriber(modid = WitcheryWallsMod.MODID)
public final class WitcheryWallsConfig
{
    @Config.Comment("Village generation chances. Each village rolls once; 100 is always, 0 is never.")
    @Config.LangKey("config.witcherywalls.generation")
    public static final Generation generation = new Generation();

    @Config.Comment("Witchery village guards.")
    @Config.LangKey("config.witcherywalls.guards")
    public static final Guards guards = new Guards();

    public static class Generation
    {
        @Config.Name("wallSpawnChance")
        @Config.LangKey("config.witcherywalls.wallSpawnChance")
        @Config.Comment("Chance for a village to generate town walls.")
        @Config.RangeInt(min = 0, max = 100)
        public int wallSpawnChance = 50;

        @Config.Name("keepSpawnChance")
        @Config.LangKey("config.witcherywalls.keepSpawnChance")
        @Config.Comment("Chance for a village to generate a town keep.")
        @Config.RangeInt(min = 0, max = 100)
        public int keepSpawnChance = 50;

        @Config.Name("watchTowerSpawnChance")
        @Config.LangKey("config.witcherywalls.watchTowerSpawnChance")
        @Config.Comment("Chance for a village to generate watch towers.")
        @Config.RangeInt(min = 0, max = 100)
        public int watchTowerSpawnChance = 50;
    }

    public static class Guards
    {
        @Config.Name("spawnVillageGuards")
        @Config.LangKey("config.witcherywalls.spawnVillageGuards")
        @Config.Comment({
                "Spawn Witchery village guards on keeps, towers, walls, and streets.",
                "Also controls the spawn egg and /summon witcherywalls:village_guard."
        })
        public boolean spawnVillageGuards = true;

        @Config.Name("spawnStreetGuards")
        @Config.LangKey("config.witcherywalls.spawnStreetGuards")
        @Config.Comment("Spawn 6 village guards along village streets. Requires Spawn Village Guards.")
        public boolean spawnStreetGuards = true;
    }

    private WitcheryWallsConfig()
    {
    }

    public static boolean spawnVillageGuards()
    {
        return guards.spawnVillageGuards;
    }

    public static boolean spawnStreetGuards()
    {
        return spawnVillageGuards() && guards.spawnStreetGuards;
    }

    public static boolean roll(Random random, int chance)
    {
        if (chance >= 100)
        {
            return true;
        }
        if (chance <= 0)
        {
            return false;
        }
        return random.nextInt(100) < chance;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if (WitcheryWallsMod.MODID.equals(event.getModID()))
        {
            ConfigManager.sync(WitcheryWallsMod.MODID, Config.Type.INSTANCE);
            ModEntities.updateSpawnEggVisibility();
        }
    }
}
