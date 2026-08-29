package com.witcherywalls.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelVillageGuard extends ModelBiped
{
    public ModelRenderer bipedHeadNose;
    public ModelRenderer bipedBodyRobe;

    public ModelVillageGuard()
    {
        super(0.0F, 0.0F, 64, 64);

        bipedHead = new ModelRenderer(this, 28, 46);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, 0.0F);

        bipedHeadNose = new ModelRenderer(this, 52, 46);
        bipedHeadNose.setRotationPoint(0.0F, -2.0F, 0.0F);
        bipedHeadNose.addBox(-1.0F, -1.0F, -6.0F, 2, 4, 2, 0.0F);
        bipedHead.addChild(bipedHeadNose);

        bipedBodyRobe = new ModelRenderer(this, 0, 38);
        bipedBodyRobe.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBodyRobe.addBox(-4.0F, 0.0F, -3.0F, 8, 18, 6, 0.5F);
        bipedBody.addChild(bipedBodyRobe);
    }

    @Override
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTickTime)
    {
        this.rightArmPose = ModelBiped.ArmPose.EMPTY;
        this.leftArmPose = ModelBiped.ArmPose.EMPTY;
        ItemStack mainHand = entity.getHeldItem(EnumHand.MAIN_HAND);
        if (!mainHand.isEmpty() && mainHand.getItem() == Items.BOW && entity.isHandActive())
        {
            if (entity.getPrimaryHand() == EnumHandSide.RIGHT)
            {
                this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
            else
            {
                this.leftArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
        }
        else if (!mainHand.isEmpty())
        {
            if (entity.getPrimaryHand() == EnumHandSide.RIGHT)
            {
                this.rightArmPose = ModelBiped.ArmPose.ITEM;
            }
            else
            {
                this.leftArmPose = ModelBiped.ArmPose.ITEM;
            }
        }
        super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
    }

    @Override
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
    {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        bipedRightLeg.rotationPointY += 0.3F;
        bipedLeftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
        bipedRightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount * 0.5F;
        bipedLeftLeg.rotateAngleY = 0.0F;
        bipedRightLeg.rotateAngleY = 0.0F;
    }
}
