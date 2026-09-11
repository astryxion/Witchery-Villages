package com.witcherywalls.client;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.init.ModEntities;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = WitcheryWallsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WitcheryWallsClient
{
    public static final ModelLayerLocation VILLAGE_GUARD = new ModelLayerLocation(
            new ResourceLocation(WitcheryWallsMod.MODID, "village_guard"), "main");

    private WitcheryWallsClient()
    {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> createConfigScreen(parent)));
    }

    private static Screen createConfigScreen(Screen parent)
    {
        return new WitcheryWallsConfigScreen(parent);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(VILLAGE_GUARD, VillageGuardModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.VILLAGE_GUARD.get(), VillageGuardRenderer::new);
    }
}
