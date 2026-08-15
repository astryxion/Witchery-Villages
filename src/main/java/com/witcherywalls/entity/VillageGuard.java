package com.witcherywalls.entity;

import com.witcherywalls.entity.ai.DefendVillageGuardGoal;
import com.witcherywalls.entity.ai.VillageGuardRangedGoal;
import com.witcherywalls.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class VillageGuard extends PathfinderMob implements RangedAttackMob
{
    private int homeCheckTimer;

    public VillageGuard(EntityType<? extends VillageGuard> type, Level level)
    {
        super(type, level);
        this.xpReward = 5;
        this.setPersistenceRequired();
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    @Override
    protected PathNavigation createNavigation(Level level)
    {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanOpenDoors(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, new VillageGuardRangedGoal(this, 1.0D, 20, 15.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true)
        {
            @Override
            public boolean canUse()
            {
                return VillageGuard.this.getTarget() != null
                        && !(VillageGuard.this.getMainHandItem().getItem() instanceof BowItem)
                        && super.canUse();
            }
        });
        this.goalSelector.addGoal(4, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 0.6D, true, 4, () -> false));
        this.goalSelector.addGoal(7, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, AbstractVillager.class, 8.0F));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new HurtByTargetGoal(this, VillageGuard.class, IronGolem.class, AbstractVillager.class).setAlertOthers());
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Ravager.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Witch.class, true));
        this.targetSelector.addGoal(5, new DefendVillageGuardGoal(this));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Raider.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Zombie.class, true, (mob) -> !(mob instanceof ZombifiedPiglin)));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, true, VillageGuard.HOSTILE_EXCEPT_CREEPER));
    }

    private static final Predicate<LivingEntity> HOSTILE_EXCEPT_CREEPER = (entity) ->
            entity instanceof Enemy
                    && !(entity instanceof Creeper)
                    && !(entity instanceof ZombifiedPiglin);

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag)
    {
        this.setPersistenceRequired();
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        BlockPos spawn = this.blockPosition();
        this.restrictTo(spawn, 48);
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    @Override
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        if (--this.homeCheckTimer <= 0)
        {
            this.homeCheckTimer = 70 + this.random.nextInt(50);
            if (!this.hasRestriction())
            {
                this.restrictTo(this.blockPosition(), 48);
            }
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty)
    {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(random.nextInt(5) == 0 ? Items.IRON_CHESTPLATE : Items.LEATHER_CHESTPLATE));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(random.nextInt(5) == 0 ? Items.IRON_HELMET : Items.LEATHER_HELMET));
        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            this.setDropChance(slot, 0.0F);
        }
    }

    @Override
    protected void doPush(Entity entity)
    {
        if (entity instanceof PathfinderMob living && this.isVillageDefender(living.getTarget()))
        {
            this.setTarget(living);
        }
        super.doPush(entity);
    }

    @Override
    public boolean canAttack(LivingEntity target)
    {
        return !this.isVillageDefender(target)
                && !(target instanceof Creeper)
                && !(target instanceof TamableAnimal tamable && tamable.isTame())
                && super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target)
    {
        if (target != null && (this.isVillageDefender(target)
                || target instanceof Creeper
                || (target instanceof TamableAnimal tamable && tamable.isTame())))
        {
            return;
        }
        super.setTarget(target);
    }

    public boolean isVillageDefender(@Nullable LivingEntity entity)
    {
        return entity instanceof VillageGuard || entity instanceof AbstractVillager || entity instanceof IronGolem;
    }

    @Override
    public boolean isAlliedTo(Entity entity)
    {
        if (this.isVillageDefender(entity instanceof LivingEntity living ? living : null))
        {
            return true;
        }
        if (entity instanceof TamableAnimal tamable && tamable.isTame())
        {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        if (source.getEntity() instanceof VillageGuard)
        {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor)
    {
        ItemStack weapon = this.getMainHandItem();
        ItemStack projectile = this.getProjectile(weapon);
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, projectile, distanceFactor);
        if (weapon.getItem() instanceof BowItem bow)
        {
            arrow = bow.customArrow(arrow);
        }
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontal = Mth.sqrt((float) (dx * dx + dz * dz));
        arrow.shoot(dx, dy + horizontal * 0.2D, dz, 1.6F, 14 - this.level().getDifficulty().getId() * 4);
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }

    @Override
    public ItemStack getProjectile(ItemStack weapon)
    {
        return new ItemStack(Items.ARROW);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem weapon)
    {
        return weapon instanceof BowItem;
    }

    public boolean friendlyInLineOfSight()
    {
        Vec3 look = this.getViewVector(1.0F);
        AABB aabb = this.getBoundingBox().expandTowards(look.scale(6.0D)).inflate(1.0D);
        List<Entity> entities = this.level().getEntities(this, aabb);
        for (Entity entity : entities)
        {
            if (entity == this.getTarget() || !this.isVillageDefender(entity instanceof LivingEntity living ? living : null))
            {
                continue;
            }
            Vec3 toSelf = entity.position().vectorTo(this.position()).normalize();
            if (toSelf.dot(this.getLookAngle()) < 0.0D && this.hasLineOfSight(entity))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source)
    {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    protected float getSoundVolume()
    {
        return 0.8F;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer)
    {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player)
    {
        return false;
    }

    @Override
    public double getMyRidingOffset()
    {
        return -0.35D;
    }

    @Override
    public boolean canAttackType(EntityType<?> type)
    {
        return type != EntityType.CREEPER && type != ModEntities.VILLAGE_GUARD.get() && super.canAttackType(type);
    }
}
