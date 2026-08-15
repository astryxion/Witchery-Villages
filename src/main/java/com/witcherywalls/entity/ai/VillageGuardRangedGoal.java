package com.witcherywalls.entity.ai;

import com.witcherywalls.entity.VillageGuard;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;

import java.util.EnumSet;

/**
 * Walk into range, draw the bow, then shoot. No skeleton strafing.
 */
public class VillageGuardRangedGoal extends Goal
{
    private final VillageGuard guard;
    private final double speedModifier;
    private final int attackInterval;
    private final float attackRadiusSqr;
    private int seeTime;
    private int attackTime = -1;

    public VillageGuardRangedGoal(VillageGuard guard, double speedModifier, int attackInterval, float attackRadius)
    {
        this.guard = guard;
        this.speedModifier = speedModifier;
        this.attackInterval = attackInterval;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        LivingEntity target = this.guard.getTarget();
        return target != null && target.isAlive() && this.guard.getMainHandItem().getItem() instanceof BowItem;
    }

    @Override
    public boolean canContinueToUse()
    {
        return this.canUse();
    }

    @Override
    public void stop()
    {
        this.seeTime = 0;
        this.attackTime = -1;
        this.guard.getNavigation().stop();
        this.guard.stopUsingItem();
    }

    @Override
    public void tick()
    {
        LivingEntity target = this.guard.getTarget();
        if (target == null)
        {
            return;
        }

        double distanceSqr = this.guard.distanceToSqr(target);
        boolean canSee = this.guard.getSensing().hasLineOfSight(target);
        if (canSee)
        {
            this.seeTime++;
        }
        else
        {
            this.seeTime--;
        }
        this.guard.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (distanceSqr <= this.attackRadiusSqr && this.seeTime >= 5)
        {
            this.guard.getNavigation().stop();
        }
        else
        {
            this.guard.getNavigation().moveTo(target, this.speedModifier);
        }

        if (this.guard.friendlyInLineOfSight())
        {
            this.guard.stopUsingItem();
            return;
        }

        if (this.guard.isUsingItem())
        {
            if (!canSee && this.seeTime < -60)
            {
                this.guard.stopUsingItem();
            }
            else if (canSee)
            {
                int drawTicks = this.guard.getTicksUsingItem();
                if (drawTicks >= 20)
                {
                    this.guard.stopUsingItem();
                    this.guard.performRangedAttack(target, BowItem.getPowerForTime(drawTicks));
                    this.attackTime = this.attackInterval;
                }
            }
        }
        else if (--this.attackTime <= 0 && this.seeTime >= 5)
        {
            this.guard.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.guard, item -> item instanceof BowItem));
        }
    }
}
