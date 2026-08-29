package com.witcherywalls.client;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.entity.EntityVillageGuard;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
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
        this.addLayer(new LayerVillageGuardArmor(this));
    }

    @Override
    public void doRender(EntityVillageGuard entity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        this.setModelVisibilities(entity);
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    private void setModelVisibilities(EntityVillageGuard entity)
    {
        ModelBiped model = (ModelBiped) this.getMainModel();
        ItemStack mainHand = entity.getHeldItemMainhand();
        ItemStack offHand = entity.getHeldItemOffhand();
        model.setVisible(true);
        ModelBiped.ArmPose mainPose = this.getArmPose(entity, mainHand);
        ModelBiped.ArmPose offPose = this.getArmPose(entity, offHand);
        model.isSneak = entity.isSneaking();
        if (entity.getPrimaryHand() == EnumHandSide.RIGHT)
        {
            model.rightArmPose = mainPose;
            model.leftArmPose = offPose;
        }
        else
        {
            model.rightArmPose = offPose;
            model.leftArmPose = mainPose;
        }
    }

    private ModelBiped.ArmPose getArmPose(EntityVillageGuard entity, ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return ModelBiped.ArmPose.EMPTY;
        }

        ModelBiped.ArmPose pose = ModelBiped.ArmPose.ITEM;
        if (entity.getItemInUseCount() > 0)
        {
            EnumAction action = stack.getItemUseAction();
            if (action == EnumAction.BOW)
            {
                pose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
            else if (action == EnumAction.BLOCK)
            {
                pose = ModelBiped.ArmPose.BLOCK;
            }
        }
        return pose;
    }

    @Override
    protected void preRenderCallback(EntityVillageGuard entity, float partialTickTime)
    {
        GlStateManager.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityVillageGuard entity)
    {
        return TEXTURE;
    }
}
