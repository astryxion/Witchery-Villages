package com.witcherywalls.entity.ai;

import com.witcherywalls.entity.EntityVillageGuard;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;

/**
 * Retaliate when hurt, but never against villagers, golems, or other guards.
 * Alerts nearby guards the same way 1.20.1 HurtByTarget.setAlertOthers() does.
 */
public class EntityAIGuardHurtByTarget extends EntityAIHurtByTarget
{
    private final EntityVillageGuard guard;

    public EntityAIGuardHurtByTarget(EntityVillageGuard guard)
    {
        super(guard, true);
        this.guard = guard;
    }

    @Override
    public boolean shouldExecute()
    {
        EntityLivingBase attacker = this.taskOwner.getRevengeTarget();
        if (attacker != null && !this.guard.isValidGuardTarget(attacker))
        {
            return false;
        }
        return super.shouldExecute();
    }
}
