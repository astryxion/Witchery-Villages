package com.witcherywalls;

import com.witcherywalls.entity.VillageGuard;
import com.witcherywalls.init.ModBlockEntities;
import com.witcherywalls.init.ModBlocks;
import com.witcherywalls.init.ModEntities;
import com.witcherywalls.init.ModItems;
import com.witcherywalls.config.WitcheryWallsConfig;
import com.witcherywalls.worldgen.VillageWallChunkHandler;
import com.witcherywalls.worldgen.VillageWallScheduler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
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
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, WitcheryWallsConfig.COMMON_SPEC);
        ModBlocks.BLOCKS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModEntities.ENTITY_TYPES.register(modBus);
        ModItems.ITEMS.register(modBus);
        modBus.addListener(this::addAttributes);
        modBus.addListener(this::addCreativeTabs);
        modBus.addListener(this::onConfigLoad);

        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(this::onServerTick);
        forgeBus.register(VillageWallChunkHandler.class);
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }

    private void onConfigLoad(final ModConfigEvent.Loading event)
    {
        if (event.getConfig().getSpec() == WitcheryWallsConfig.COMMON_SPEC)
        {
            WitcheryWallsConfig.applyGuardCompatibilityDefault(event.getConfig());
        }
    }

    private void addAttributes(final EntityAttributeCreationEvent event)
    {
        event.put(ModEntities.VILLAGE_GUARD.get(), VillageGuard.createAttributes().build());
    }

    private void addCreativeTabs(final BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS && WitcheryWallsConfig.spawnVillageGuards())
        {
            event.accept(ModItems.VILLAGE_GUARD_SPAWN_EGG.get());
        }
    }

    private void onServerTick(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END)
        {
            VillageWallScheduler.tick(event.getServer());
        }
    }
}
