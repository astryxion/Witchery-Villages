package com.witcherywalls.entity.ai;

import com.witcherywalls.entity.EntityVillageGuard;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

/**
 * Protects villagers, iron golems, and other guards from nearby aggressors.
 * Does not target nearby players by reputation or proximity.
 */
public class EntityAIDefendVillageGuard extends EntityAITarget
{
    private final EntityVillageGuard guard;
    private EntityLivingBase villageAggressorTarget;

    public EntityAIDefendVillageGuard(EntityVillageGuard guard)
    {
        super(guard, true, true);
        this.guard = guard;
        this.setMutexBits(1);
    }

    @Override
    public boolean shouldExecute()
    {
        AxisAlignedBB search = this.guard.getEntityBoundingBox().grow(10.0D, 8.0D, 10.0D);
        List<EntityLivingBase> nearby = this.guard.world.getEntitiesWithinAABB(EntityLivingBase.class, search);
        this.villageAggressorTarget = null;

        for (EntityLivingBase entity : nearby)
        {
            if (entity instanceof EntityLiving)
            {
                EntityLiving mob = (EntityLiving) entity;
                if (this.guard.isVillageDefender(mob.getAttackTarget()) && this.guard.isValidGuardTarget(mob))
                {
                    this.villageAggressorTarget = mob;
                    return true;
                }
            }
        }

        for (EntityLivingBase entity : nearby)
        {
            if (!this.guard.isVillageDefender(entity))
            {
                continue;
            }

            EntityLivingBase attacker = entity.getRevengeTarget();
            if (this.guard.isValidGuardTarget(attacker) && entity.ticksExisted - entity.getRevengeTimer() < 100)
            {
                this.villageAggressorTarget = attacker;
                return true;
            }
        }

        return false;
    }

    @Override
    public void startExecuting()
    {
        this.guard.setAttackTarget(this.villageAggressorTarget);
        super.startExecuting();
    }
}
