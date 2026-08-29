package com.witcherywalls.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 1.12.2 LayerBipedArmor uses a vanilla ModelBiped that recomputes its own walk
 * cycle. Village guards use a half-swing villager walk, so leather boots/leggings
 * drift off the legs. 1.20.1 HumanoidArmorLayer copies parent poses instead;
 * this layer does the same.
 */
@SideOnly(Side.CLIENT)
public class LayerVillageGuardArmor extends LayerBipedArmor
{
    private final RenderLivingBase<?> guardRenderer;

    public LayerVillageGuardArmor(RenderLivingBase<?> renderer)
    {
        super(renderer);
        this.guardRenderer = renderer;
    }

    @Override
    protected void initArmor()
    {
        this.modelLeggings = new ModelCopiedBipedArmor(0.5F);
        this.modelArmor = new ModelCopiedBipedArmor(1.0F);
    }

    private class ModelCopiedBipedArmor extends ModelBiped
    {
        public ModelCopiedBipedArmor(float modelSize)
        {
            super(modelSize);
        }

        @Override
        public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
        {
            ModelBiped parent = (ModelBiped) LayerVillageGuardArmor.this.guardRenderer.getMainModel();
            copyPose(this.bipedHead, parent.bipedHead);
            copyPose(this.bipedHeadwear, parent.bipedHeadwear);
            copyPose(this.bipedBody, parent.bipedBody);
            copyPose(this.bipedRightArm, parent.bipedRightArm);
            copyPose(this.bipedLeftArm, parent.bipedLeftArm);
            copyPose(this.bipedRightLeg, parent.bipedRightLeg);
            copyPose(this.bipedLeftLeg, parent.bipedLeftLeg);
        }

        private void copyPose(ModelRenderer dest, ModelRenderer src)
        {
            dest.rotationPointX = src.rotationPointX;
            dest.rotationPointY = src.rotationPointY;
            dest.rotationPointZ = src.rotationPointZ;
            dest.rotateAngleX = src.rotateAngleX;
            dest.rotateAngleY = src.rotateAngleY;
            dest.rotateAngleZ = src.rotateAngleZ;
        }
    }
}
