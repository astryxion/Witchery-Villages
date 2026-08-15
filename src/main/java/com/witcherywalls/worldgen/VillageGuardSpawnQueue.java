package com.witcherywalls.worldgen;

import com.witcherywalls.config.WitcheryWallsConfig;
import com.witcherywalls.entity.VillageGuard;
import com.witcherywalls.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Spawns guards after keep/tower/wall blocks actually exist so they do not fall through.
 * Also places Guard Villagers on those buildings when our own guards are turned off.
 */
public final class VillageGuardSpawnQueue
{
    public enum Site
    {
        KEEP, TOWER, WALL, STREET
    }

    private static final ResourceLocation GUARD_VILLAGERS_ID = new ResourceLocation("guardvillagers", "guard");
    private static final int GROUP_RADIUS = 96;
    private static final int READY_DELAY_TICKS = 20;
    private static final int PENDING_TIMEOUT_TICKS = 400;
    private static final int GROUP_TIMEOUT_TICKS = 800;
    private static final List<VillageGroup> GROUPS = new ArrayList<>();
    private static boolean placing;

    private VillageGuardSpawnQueue()
    {
    }

    public static boolean isPlacing()
    {
        return placing;
    }

    public static boolean isGuardVillagersGuard(Entity entity)
    {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return GUARD_VILLAGERS_ID.equals(key);
    }

    public static void schedule(ServerLevel level, BlockPos pos, Site site)
    {
        if (!shouldTrack(site))
        {
            return;
        }
        VillageGroup group = groupNear(level, pos);
        group.gotSpots = true;
        group.pending.add(new Pending(pos.immutable(), site, 0));
    }

    public static void steal(ServerLevel level, BlockPos pos)
    {
        groupNear(level, pos).stolen++;
    }

    public static void tick()
    {
        if (GROUPS.isEmpty())
        {
            return;
        }

        Iterator<VillageGroup> groups = GROUPS.iterator();
        while (groups.hasNext())
        {
            VillageGroup group = groups.next();
            group.age++;
            tickGroup(group);
            if (group.pending.isEmpty() && (group.gotSpots || group.age > GROUP_TIMEOUT_TICKS))
            {
                if (!group.gotSpots)
                {
                    spawnFallbackStolen(group);
                }
                groups.remove();
            }
        }
    }

    private static boolean shouldTrack(Site site)
    {
        boolean our = WitcheryWallsConfig.spawnVillageGuards();
        boolean gv = guardVillagersLoaded();
        if (site == Site.STREET)
        {
            if (our && gv)
            {
                return false;
            }
            return our || gv;
        }
        return our || gv;
    }

    private static boolean guardVillagersLoaded()
    {
        return ModList.get().isLoaded("guardvillagers");
    }

    private static VillageGroup groupNear(ServerLevel level, BlockPos pos)
    {
        for (VillageGroup group : GROUPS)
        {
            if (group.level == level && group.center.closerThan(pos, GROUP_RADIUS))
            {
                return group;
            }
        }
        VillageGroup created = new VillageGroup(level, pos.immutable());
        GROUPS.add(created);
        return created;
    }

    private static void tickGroup(VillageGroup group)
    {
        boolean our = WitcheryWallsConfig.spawnVillageGuards();
        boolean gv = guardVillagersLoaded();
        Iterator<Pending> iterator = group.pending.iterator();
        while (iterator.hasNext())
        {
            Pending pending = iterator.next();
            pending.age++;
            BlockPos stand = findStandable(group.level, pending.pos);
            boolean timeout = pending.age >= PENDING_TIMEOUT_TICKS;
            if ((stand == null || pending.age < READY_DELAY_TICKS) && !timeout)
            {
                continue;
            }
            if (stand == null)
            {
                stand = fallbackStandable(group.level, pending.pos);
            }

            if (our)
            {
                if (pending.site == Site.STREET && gv)
                {
                    iterator.remove();
                    continue;
                }
                spawnOur(group.level, stand);
                iterator.remove();
                continue;
            }

            if (gv)
            {
                spawnGuardVillagers(group.level, stand);
            }
            iterator.remove();
        }
    }

    private static void spawnFallbackStolen(VillageGroup group)
    {
        if (group.stolen <= 0 || !guardVillagersLoaded() || WitcheryWallsConfig.spawnVillageGuards())
        {
            return;
        }
        BlockPos stand = findStandable(group.level, group.center);
        if (stand == null)
        {
            stand = fallbackStandable(group.level, group.center);
        }
        for (int i = 0; i < group.stolen; i++)
        {
            spawnGuardVillagers(group.level, stand);
        }
    }

    private static BlockPos findStandable(ServerLevel level, BlockPos hint)
    {
        if (!level.hasChunk(hint.getX() >> 4, hint.getZ() >> 4))
        {
            return null;
        }
        if (VillageWallPlacementQueue.hasPending(level, hint) || VillageWallPlacementQueue.hasPending(level, hint.below()))
        {
            return null;
        }

        int x = hint.getX();
        int z = hint.getZ();
        for (int y = hint.getY() + 3; y >= hint.getY() - 10; y--)
        {
            BlockPos floor = new BlockPos(x, y, z);
            BlockPos feet = floor.above();
            if (!level.hasChunk(feet.getX() >> 4, feet.getZ() >> 4))
            {
                return null;
            }
            if (isSolidFloor(level, floor) && isFree(level, feet) && isFree(level, feet.above()))
            {
                return feet;
            }
        }
        return null;
    }

    private static BlockPos fallbackStandable(ServerLevel level, BlockPos hint)
    {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, hint.getX(), hint.getZ());
        return new BlockPos(hint.getX(), y, hint.getZ());
    }

    private static boolean isSolidFloor(ServerLevel level, BlockPos pos)
    {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && !state.getCollisionShape(level, pos).isEmpty();
    }

    private static boolean isFree(ServerLevel level, BlockPos pos)
    {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || !state.blocksMotion();
    }

    private static void spawnOur(ServerLevel level, BlockPos pos)
    {
        VillageGuard guard = ModEntities.VILLAGE_GUARD.get().create(level);
        if (guard == null)
        {
            return;
        }
        placing = true;
        try
        {
            guard.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
            guard.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            level.addFreshEntity(guard);
        }
        finally
        {
            placing = false;
        }
    }

    private static void spawnGuardVillagers(ServerLevel level, BlockPos pos)
    {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(GUARD_VILLAGERS_ID);
        if (type == null || !GUARD_VILLAGERS_ID.equals(ForgeRegistries.ENTITY_TYPES.getKey(type)))
        {
            return;
        }
        Entity entity = type.create(level);
        if (entity == null)
        {
            return;
        }
        placing = true;
        try
        {
            entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof Mob mob)
            {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
            }
            level.addFreshEntity(entity);
        }
        finally
        {
            placing = false;
        }
    }

    private static final class VillageGroup
    {
        private final ServerLevel level;
        private final BlockPos center;
        private final List<Pending> pending = new ArrayList<>();
        private int stolen;
        private boolean gotSpots;
        private int age;

        private VillageGroup(ServerLevel level, BlockPos center)
        {
            this.level = level;
            this.center = center;
        }
    }

    private static final class Pending
    {
        private final BlockPos pos;
        private final Site site;
        private int age;

        private Pending(BlockPos pos, Site site, int age)
        {
            this.pos = pos;
            this.site = site;
            this.age = age;
        }
    }
}
