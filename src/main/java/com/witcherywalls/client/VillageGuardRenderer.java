package com.witcherywalls.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.entity.VillageGuard;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

public class VillageGuardRenderer extends HumanoidMobRenderer<VillageGuard, VillageGuardModel<VillageGuard>>
{
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(WitcheryWallsMod.MODID, "textures/entities/villageguard.png");

    public VillageGuardRenderer(EntityRendererProvider.Context context)
    {
        super(context, new VillageGuardModel<>(context.bakeLayer(WitcheryWallsClient.VILLAGE_GUARD)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public void render(VillageGuard entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight)
    {
        this.setModelVisibilities(entity);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void setModelVisibilities(VillageGuard entity)
    {
        HumanoidModel<VillageGuard> model = this.getModel();
        ItemStack mainHand = entity.getMainHandItem();
        ItemStack offHand = entity.getOffhandItem();
        model.setAllVisible(true);
        HumanoidModel.ArmPose mainPose = this.getArmPose(entity, mainHand, offHand, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose offPose = this.getArmPose(entity, mainHand, offHand, InteractionHand.OFF_HAND);
        model.crouching = entity.isCrouching();
        if (entity.getMainArm() == HumanoidArm.RIGHT)
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

    private HumanoidModel.ArmPose getArmPose(VillageGuard entity, ItemStack mainHand, ItemStack offHand,
                                             InteractionHand hand)
    {
        ItemStack stack = hand == InteractionHand.MAIN_HAND ? mainHand : offHand;
        if (stack.isEmpty())
        {
            return HumanoidModel.ArmPose.EMPTY;
        }

        HumanoidModel.ArmPose pose = HumanoidModel.ArmPose.ITEM;
        if (entity.getUseItemRemainingTicks() > 0)
        {
            UseAnim anim = stack.getUseAnimation();
            if (anim == UseAnim.BOW)
            {
                pose = HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            else if (anim == UseAnim.BLOCK)
            {
                pose = HumanoidModel.ArmPose.BLOCK;
            }
        }
        return pose;
    }

    @Override
    protected void scale(VillageGuard entity, PoseStack poseStack, float partialTickTime)
    {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    public ResourceLocation getTextureLocation(VillageGuard entity)
    {
        return TEXTURE;
    }
}
