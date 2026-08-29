package com.witcherywalls.worldgen;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.config.WitcheryWallsConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Random;

@Mod.EventBusSubscriber(modid = WitcheryWallsMod.MODID)
public final class VillageStreetGuardEvents
{
    private VillageStreetGuardEvents()
    {
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || event.world.isRemote || event.world.villageCollection == null)
        {
            return;
        }
        if (!WitcheryWallsConfig.spawnStreetGuards())
        {
            return;
        }

        World world = event.world;
        if ((world.getTotalWorldTime() & 39L) != 0L)
        {
            return;
        }

        VillageStreetGuardData data = VillageStreetGuardData.get(world);
        Random random = world.rand;
        for (Village village : world.villageCollection.getVillageList())
        {
            if (village.getNumVillageDoors() < 3)
            {
                continue;
            }

            BlockPos center = village.getCenter();
            if (data.isProcessed(center))
            {
                continue;
            }

            int spawned = VillageStreetGuardSpawner.spawn(world, village, random);
            if (spawned >= 0)
            {
                data.markProcessed(center);
            }
        }
    }
}
