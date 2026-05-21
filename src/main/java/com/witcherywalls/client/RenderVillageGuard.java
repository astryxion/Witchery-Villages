package com.witcherywalls.client;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.entity.EntityVillageGuard;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderVillageGuard extends RenderBiped<EntityVillageGuard>
{
    private static final ResourceLocation TEXTURE = new ResourceLocation(WitcheryWallsMod.MODID, "textures/entities/villageguard.png");

    public RenderVillageGuard(RenderManager manager)
    {
        super(manager, new ModelVillageGuard(), 0.5F);
        // Ensure armor renders (required for some custom biped setups).
        boolean hasArmorLayer = false;
        for (Object layer : this.layerRenderers)
        {
            if (layer instanceof LayerBipedArmor)
            {
                hasArmorLayer = true;
                break;
            }

        }
        if (!hasArmorLayer)
        {
            this.addLayer(new LayerBipedArmor(this));
            this.addLayer(new LayerHeldItem(this));
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVillageGuard entity)
    {
        return TEXTURE;
    }
}
