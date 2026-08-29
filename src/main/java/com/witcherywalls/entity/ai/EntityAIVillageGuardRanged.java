package com.witcherywalls.entity.ai;

import com.witcherywalls.entity.EntityVillageGuard;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.util.EnumHand;

/**
 * Draw the bow and shoot while keeping skeleton-style spacing: close in to range,
 * back up when mobs get near, and strafe instead of walking into melee.
 */
public class EntityAIVillageGuardRanged extends EntityAIBase
{
    private static final float BACK_UP_DISTANCE_SQ = 36.0F;
    private static final float STRAFE_OUTER = 0.75F;
    private static final float STRAFE_INNER = 0.35F;

    private final EntityVillageGuard guard;
    private final double moveSpeedAmp;
    private final int attackInterval;
    private final float maxAttackDistanceSq;
    private int seeTime;
    private int attackTime = -1;
    private int strafingTime = -1;
    private boolean strafingClockwise;
    private boolean strafingBackwards;

    public EntityAIVillageGuardRanged(EntityVillageGuard guard, double moveSpeedAmp, int attackInterval, float maxAttackDistance)
    {
        this.guard = guard;
        this.moveSpeedAmp = moveSpeedAmp;
        this.attackInterval = attackInterval;
        this.maxAttackDistanceSq = maxAttackDistance * maxAttackDistance;
        this.setMutexBits(3);
    }

    @Override
    public boolean shouldExecute()
    {
        EntityLivingBase target = this.guard.getAttackTarget();
        return target != null && target.isEntityAlive()
                && !this.guard.getHeldItemMainhand().isEmpty()
                && this.guard.getHeldItemMainhand().getItem() == Items.BOW;
    }

    @Override
    public boolean shouldContinueExecuting()
    {
        return this.shouldExecute();
    }

    @Override
    public void resetTask()
    {
        this.seeTime = 0;
        this.attackTime = -1;
        this.strafingTime = -1;
        this.guard.getNavigator().clearPath();
        this.guard.resetActiveHand();
        this.guard.setSwingingArms(false);
    }

    @Override
    public void updateTask()
    {
        EntityLivingBase target = this.guard.getAttackTarget();
        if (target == null)
        {
            return;
        }

        double distanceSq = this.guard.getDistanceSq(target);
        boolean canSee = this.guard.getEntitySenses().canSee(target);
        if (canSee)
        {
            this.seeTime++;
        }
        else
        {
            this.seeTime--;
        }
        this.guard.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);

        if (distanceSq <= this.maxAttackDistanceSq && this.seeTime >= 5)
        {
            this.guard.getNavigator().clearPath();
            ++this.strafingTime;
        }
        else
        {
            this.guard.getNavigator().tryMoveToEntityLiving(target, this.moveSpeedAmp);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20)
        {
            if (this.guard.getRNG().nextFloat() < 0.3F)
            {
                this.strafingClockwise = !this.strafingClockwise;
            }
            if (this.guard.getRNG().nextFloat() < 0.3F)
            {
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }

        if (this.strafingTime > -1)
        {
            if (distanceSq > this.maxAttackDistanceSq * STRAFE_OUTER)
            {
                this.strafingBackwards = false;
            }
            else if (distanceSq < this.maxAttackDistanceSq * STRAFE_INNER || distanceSq < BACK_UP_DISTANCE_SQ)
            {
                this.strafingBackwards = true;
            }

            this.guard.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.guard.faceEntity(target, 30.0F, 30.0F);
        }

        if (this.guard.friendlyInLineOfSight())
        {
            this.guard.resetActiveHand();
            return;
        }

        if (this.guard.isHandActive())
        {
            if (!canSee && this.seeTime < -60)
            {
                this.guard.resetActiveHand();
            }
            else if (canSee)
            {
                int drawTicks = this.guard.getItemInUseMaxCount();
                if (drawTicks >= 20)
                {
                    this.guard.resetActiveHand();
                    this.guard.attackEntityWithRangedAttack(target, ItemBow.getArrowVelocity(drawTicks));
                    this.attackTime = this.attackInterval;
                }
            }
        }
        else if (--this.attackTime <= 0 && this.seeTime >= 5)
        {
            this.guard.setActiveHand(EnumHand.MAIN_HAND);
            this.guard.setSwingingArms(true);
        }
    }
}
