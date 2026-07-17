package com.witcherywalls;

import com.witcherywalls.init.ModBlockEntities;
import com.witcherywalls.init.ModBlocks;
import com.witcherywalls.worldgen.VillageWallChunkHandler;
import com.witcherywalls.worldgen.VillageWallScheduler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(WitcheryWallsMod.MODID)
public class WitcheryWallsMod
{
    public static final String MODID = "witcherywalls";
    private static final Logger LOGGER = LogManager.getLogger();

    public WitcheryWallsMod()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(this::onServerTick);
        forgeBus.register(VillageWallChunkHandler.class);
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }

    private void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            VillageWallScheduler.tick(event.getServer().getTickCount());
        }
    }
}
