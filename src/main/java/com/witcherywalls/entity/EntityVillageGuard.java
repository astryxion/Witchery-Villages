package com.witcherywalls.entity;

import com.witcherywalls.entity.ai.EntityAIDefendVillageGuard;
import com.witcherywalls.entity.ai.EntityAIGuardHurtByTarget;
import com.witcherywalls.entity.ai.EntityAIVillageGuardRanged;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.monster.AbstractIllager;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveThroughVillage;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.village.Village;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import java.util.List;

public class EntityVillageGuard extends EntityCreature implements IRangedAttackMob
{
    private static final DataParameter<Byte> GUARD_TYPE = EntityDataManager.createKey(EntityVillageGuard.class, DataSerializers.BYTE);

    private final EntityAIVillageGuardRanged aiArrowAttack = new EntityAIVillageGuardRanged(this, 1.0D, 20, 15.0F);
    private int homeCheckTimer;
    private Village villageObj;
    private boolean swingingArms;

    public EntityVillageGuard(World world)
    {
        super(world);
        setSize(0.6F, 1.8F);
        if (getNavigator() instanceof PathNavigateGround)
        {
            PathNavigateGround navigation = (PathNavigateGround) getNavigator();
            navigation.setBreakDoors(true);
            navigation.setEnterDoors(true);
        }

        tasks.addTask(1, new EntityAISwimming(this));
        tasks.addTask(3, aiArrowAttack);
        tasks.addTask(3, new EntityAIAttackMelee(this, 1.2D, true)
        {
            @Override
            public boolean shouldExecute()
            {
                ItemStack held = EntityVillageGuard.this.getHeldItemMainhand();
                return EntityVillageGuard.this.getAttackTarget() != null
                        && (held.isEmpty() || held.getItem() != Items.BOW)
                        && super.shouldExecute();
            }
        });
        tasks.addTask(4, new EntityAIOpenDoor(this, true));
        tasks.addTask(6, new EntityAIMoveThroughVillage(this, 0.6D, true));
        tasks.addTask(7, new EntityAIMoveTowardsRestriction(this, 1.0D));
        tasks.addTask(8, new EntityAIRestrictOpenDoor(this));
        tasks.addTask(10, new EntityAIWander(this, 0.6D));
        tasks.addTask(11, new EntityAIWatchClosest(this, EntityVillager.class, 8.0F));
        tasks.addTask(11, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        tasks.addTask(12, new EntityAILookIdle(this));

        targetTasks.addTask(2, new EntityAIGuardHurtByTarget(this));
        targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityWitch.class, true));
        targetTasks.addTask(4, new EntityAINearestAttackableTarget<>(this, AbstractIllager.class, true));
        targetTasks.addTask(5, new EntityAIDefendVillageGuard(this));
        targetTasks.addTask(5, new EntityAINearestAttackableTarget<>(this, EntityZombie.class, 10, true, true,
                zombie -> !(zombie instanceof EntityPigZombie)));
        targetTasks.addTask(6, new EntityAINearestAttackableTarget<>(this, EntityLiving.class, 5, true, true, this::isHostileTarget));

        if (world != null && !world.isRemote)
        {
            initEquipment();
        }

        experienceValue = 5;
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        dataManager.register(GUARD_TYPE, (byte) 0);
    }

    @Override
    public boolean canDespawn()
    {
        return false;
    }

    public boolean isHostileTarget(EntityLivingBase entity)
    {
        return entity instanceof IMob
                && !(entity instanceof EntityCreeper)
                && !(entity instanceof EntityPigZombie)
                && isValidGuardTarget(entity);
    }

    public boolean isValidGuardTarget(EntityLivingBase entity)
    {
        if (entity == null || entity == this || !entity.isEntityAlive())
        {
            return false;
        }
        if (isVillageDefender(entity) || entity instanceof EntityCreeper)
        {
            return false;
        }
        if (entity instanceof EntityTameable && ((EntityTameable) entity).isTamed())
        {
            return false;
        }
        if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;
            return !player.isSpectator() && !player.capabilities.isCreativeMode;
        }
        return true;
    }

    public Village getVillage()
    {
        return villageObj;
    }

    @Override
    protected void updateAITasks()
    {
        if (--homeCheckTimer <= 0)
        {
            homeCheckTimer = 70 + rand.nextInt(50);
            villageObj = world.getVillageCollection().getNearestVillage(new BlockPos(this), 32);

            if (villageObj == null)
            {
                detachHome();
            }
            else
            {
                BlockPos center = villageObj.getCenter();
                setHomePosAndDistance(center, (int) (villageObj.getVillageRadius() * 1.5F));

                if (getAttackTarget() == null)
                {
                    setAbsorptionAmount(1.0F);
                }
            }
        }

        super.updateAITasks();
    }

    @Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource)
    {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    @Override
    protected float getSoundVolume()
    {
        return 0.8F;
    }

    @Override
    public void onLivingUpdate()
    {
        updateArmSwingProgress();
        super.onLivingUpdate();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (source.getTrueSource() instanceof EntityVillageGuard)
        {
            return false;
        }

        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        Entity killer = cause.getTrueSource();
        if (killer instanceof EntityPlayer && villageObj != null)
        {
            villageObj.modifyPlayerReputation(((EntityPlayer) killer).getUniqueID(), -5);
        }

        super.onDeath(cause);
    }

    @Override
    protected Item getDropItem()
    {
        return Items.ARROW;
    }

    @Override
    protected void dropFewItems(boolean wasRecentlyHit, int looting)
    {
        entityDropItem(new ItemStack(Items.LEATHER_BOOTS), 0.0F);
    }

    public void initEquipment()
    {
        setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(
                rand.nextInt(5) == 0 ? Items.IRON_CHESTPLATE : Items.LEATHER_CHESTPLATE
        ));
        setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(
                rand.nextInt(5) == 0 ? Items.IRON_HELMET : Items.LEATHER_HELMET
        ));
        setDropChance(EntityEquipmentSlot.MAINHAND, 0.0F);
        setDropChance(EntityEquipmentSlot.FEET, 0.0F);
        setDropChance(EntityEquipmentSlot.LEGS, 0.0F);
        setDropChance(EntityEquipmentSlot.CHEST, 0.0F);
        setDropChance(EntityEquipmentSlot.HEAD, 0.0F);
    }

    /** Re-applies equipment after spawn so clients receive SPacketEntityEquipment updates. */
    public void syncEquipmentToClients()
    {
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
        {
            ItemStack stack = getItemStackFromSlot(slot);
            if (!stack.isEmpty())
            {
                setItemStackToSlot(slot, stack.copy());
            }
        }
    }

    @Override
    public String getName()
    {
        if (hasCustomName())
        {
            return getCustomNameTag();
        }
        return I18n.translateToLocal("entity.village_guard.name");
    }

    @Override
    public void onAddedToWorld()
    {
        super.onAddedToWorld();

        if (!world.isRemote)
        {
            if (getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty())
            {
                initEquipment();
            }
            syncEquipmentToClients();
        }
    }

    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata)
    {
        livingdata = super.onInitialSpawn(difficulty, livingdata);
        initEquipment();
        return livingdata;
    }

    public boolean isVillageDefender(Entity entity)
    {
        return entity instanceof EntityVillageGuard || entity instanceof EntityVillager || entity instanceof EntityIronGolem;
    }

    public boolean friendlyInLineOfSight()
    {
        Vec3d look = this.getLook(1.0F);
        AxisAlignedBB aabb = this.getEntityBoundingBox().expand(look.x * 6.0D, look.y * 6.0D, look.z * 6.0D).grow(1.0D);
        List<Entity> entities = this.world.getEntitiesWithinAABBExcludingEntity(this, aabb);
        for (Entity entity : entities)
        {
            if (entity == this.getAttackTarget() || !this.isVillageDefender(entity))
            {
                continue;
            }
            Vec3d toSelf = new Vec3d(this.posX - entity.posX, this.posY - entity.posY, this.posZ - entity.posZ).normalize();
            if (toSelf.dotProduct(this.getLookVec()) < 0.0D && this.canEntityBeSeen(entity))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setSwingingArms(boolean swingingArms)
    {
        this.swingingArms = swingingArms;
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float distanceFactor)
    {
        EntityTippedArrow arrow = new EntityTippedArrow(world, this);
        arrow.setPosition(posX, posY + getEyeHeight() - 0.1D, posZ);
        double d0 = target.posX - posX;
        double d1 = target.getEntityBoundingBox().minY + target.height / 3.0D - arrow.posY;
        double d2 = target.posZ - posZ;
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
        arrow.shoot(d0, d1 + d3 * 0.2D, d2, 1.6F, (float) (14 - world.getDifficulty().getDifficultyId() * 4));

        int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, getHeldItemMainhand());
        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, getHeldItemMainhand());
        arrow.setDamage(distanceFactor * 2.0D + rand.nextGaussian() * 0.25D + world.getDifficulty().getDifficultyId() * 0.11D);

        if (power > 0)
        {
            arrow.setDamage(arrow.getDamage() + power * 0.5D + 0.5D);
        }

        if (punch > 0)
        {
            arrow.setKnockbackStrength(punch);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, getHeldItemMainhand()) > 0 || getGuardType() == 1)
        {
            arrow.setFire(100);
        }

        playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (getRNG().nextFloat() * 0.4F + 0.8F));
        world.spawnEntity(arrow);
    }

    public int getGuardType()
    {
        return dataManager.get(GUARD_TYPE);
    }

    public void setGuardType(int type)
    {
        dataManager.set(GUARD_TYPE, (byte) type);
        isImmuneToFire = type == 1;

        if (type == 1)
        {
            setSize(0.72F, 2.34F);
        }
        else
        {
            setSize(0.6F, 1.8F);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound)
    {
        super.readEntityFromNBT(compound);

        if (compound.hasKey("GuardType"))
        {
            setGuardType(compound.getByte("GuardType"));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound)
    {
        super.writeEntityToNBT(compound);
        compound.setByte("GuardType", (byte) getGuardType());
    }

    @Override
    public double getYOffset()
    {
        return super.getYOffset() - 0.5D;
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> cls)
    {
        return cls != EntityCreeper.class
                && cls != EntityVillageGuard.class
                && cls != EntityVillager.class
                && cls != EntityIronGolem.class
                && super.canAttackClass(cls);
    }

    @Override
    public void setAttackTarget(EntityLivingBase target)
    {
        if (target != null && !isValidGuardTarget(target))
        {
            return;
        }
        super.setAttackTarget(target);
    }

    @Override
    public boolean isOnSameTeam(Entity entity)
    {
        if (isVillageDefender(entity))
        {
            return true;
        }
        if (entity instanceof EntityTameable && ((EntityTameable) entity).isTamed())
        {
            return true;
        }
        return super.isOnSameTeam(entity);
    }

    @Override
    protected void collideWithEntity(Entity entity)
    {
        if (entity instanceof EntityLiving)
        {
            EntityLiving living = (EntityLiving) entity;
            if (isVillageDefender(living.getAttackTarget()))
            {
                setAttackTarget(living);
            }
        }
        super.collideWithEntity(entity);
    }
}
