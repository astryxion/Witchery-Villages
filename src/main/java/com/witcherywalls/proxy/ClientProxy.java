package com.witcherywalls.proxy;

import com.witcherywalls.client.RenderVillageGuard;
import com.witcherywalls.entity.EntityVillageGuard;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        RenderingRegistry.registerEntityRenderingHandler(EntityVillageGuard.class, RenderVillageGuard::new);
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
    }
}
