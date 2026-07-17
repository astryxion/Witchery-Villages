package com.witcherywalls.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashSet;
import java.util.Set;

public class VillageWallSavedData extends SavedData
{
    private static final String DATA_NAME = "witcherywalls_processed_villages";
    private static final int VILLAGE_DEDUP_RADIUS = 80;

    private final Set<Long> processedCenters = new HashSet<>();

    public VillageWallSavedData()
    {
    }

    public VillageWallSavedData(CompoundTag tag)
    {
        ListTag list = tag.getList("Processed", ListTag.TAG_LONG);
        for (int i = 0; i < list.size(); i++)
        {
            processedCenters.add(((LongTag) list.get(i)).getAsLong());
        }
    }

    public static VillageWallSavedData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(
                VillageWallSavedData::load,
                VillageWallSavedData::new,
                DATA_NAME);
    }

    private static VillageWallSavedData load(CompoundTag tag)
    {
        return new VillageWallSavedData(tag);
    }

    public boolean isNearProcessed(BlockPos pos)
    {
        long radiusSquared = (long) VILLAGE_DEDUP_RADIUS * VILLAGE_DEDUP_RADIUS;

        for (long stored : processedCenters)
        {
            BlockPos center = BlockPos.of(stored);
            if (center.distSqr(pos) <= radiusSquared)
            {
                return true;
            }
        }

        return false;
    }

    public void markProcessed(BlockPos center)
    {
        processedCenters.add(center.asLong());
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag)
    {
        ListTag list = new ListTag();
        for (long value : processedCenters)
        {
            list.add(LongTag.valueOf(value));
        }
        tag.put("Processed", list);
        return tag;
    }
}
