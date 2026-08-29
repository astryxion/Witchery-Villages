package com.witcherywalls;

import com.witcherywalls.proxy.CommonProxy;
import com.witcherywalls.worldgen.VillageStructureRegistration;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = WitcheryWallsMod.MODID,
        name = WitcheryWallsMod.NAME,
        version = WitcheryWallsMod.VERSION,
        acceptedMinecraftVersions = "[1.12.2]"
)
public class WitcheryWallsMod
{
    public static final String MODID = "witcherywalls";
    public static final String NAME = "Witchery Walls";
    public static final String VERSION = "1.12.2-1.2.0";

    @Mod.Instance(MODID)
    public static WitcheryWallsMod instance;

    @SidedProxy(clientSide = "com.witcherywalls.proxy.ClientProxy", serverSide = "com.witcherywalls.proxy.CommonProxy")
    public static CommonProxy proxy;

    private static Logger logger;

    public static Logger getLogger()
    {
        return logger;
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        ConfigManager.sync(MODID, Config.Type.INSTANCE);
        ModEntities.register();
        ModBlocks.register();
        ModTileEntities.register();
        VillageStructureRegistration.preInit();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        VillageStructureRegistration.init();
        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit(event);
    }
}
