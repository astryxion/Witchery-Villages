package com.witcherywalls.entity.ai;

import com.witcherywalls.entity.VillageGuard;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

/**
 * Protects villagers, iron golems, and other guards from nearby aggressors.
 * Intentionally has no player-reputation or mayor logic.
 */
public class DefendVillageGuardGoal extends TargetGoal
{
    private final VillageGuard guard;
    private LivingEntity villageAggressorTarget;

    public DefendVillageGuardGoal(VillageGuard guard)
    {
        super(guard, true, true);
        this.guard = guard;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse()
    {
        AABB search = this.guard.getBoundingBox().inflate(10.0D, 8.0D, 10.0D);
        List<LivingEntity> nearby = this.guard.level().getEntitiesOfClass(LivingEntity.class, search);
        this.villageAggressorTarget = null;

        for (LivingEntity entity : nearby)
        {
            if (entity instanceof Mob mob && this.guard.isVillageDefender(mob.getTarget()) && this.isValidAggressor(mob))
            {
                this.villageAggressorTarget = mob;
                return true;
            }
        }

        for (LivingEntity entity : nearby)
        {
            if (!this.guard.isVillageDefender(entity))
            {
                continue;
            }

            LivingEntity attacker = entity.getLastHurtByMob();
            if (this.isValidAggressor(attacker) && entity.tickCount - entity.getLastHurtByMobTimestamp() < 100)
            {
                this.villageAggressorTarget = attacker;
                return true;
            }
        }

        return false;
    }

    @Override
    public void start()
    {
        this.guard.setTarget(this.villageAggressorTarget);
        super.start();
    }

    private boolean isValidAggressor(LivingEntity entity)
    {
        if (entity == null || entity == this.guard || !entity.isAlive())
        {
            return false;
        }
        if (this.guard.isVillageDefender(entity) || !this.guard.canAttack(entity))
        {
            return false;
        }
        if (entity instanceof TamableAnimal tamable && tamable.isTame())
        {
            return false;
        }
        if (entity instanceof Player player && (player.isSpectator() || player.isCreative()))
        {
            return false;
        }
        return true;
    }
}
