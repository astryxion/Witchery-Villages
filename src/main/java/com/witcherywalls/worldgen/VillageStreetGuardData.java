package com.witcherywalls.worldgen;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;

public class VillageStreetGuardData extends WorldSavedData
{
    private static final String DATA_NAME = "witcherywalls_street_guards";
    private static final int MATCH_DISTANCE_SQ = 48 * 48;

    private final Set<Long> processed = new HashSet<>();

    public VillageStreetGuardData(String name)
    {
        super(name);
    }

    public static VillageStreetGuardData get(World world)
    {
        MapStorage storage = world.getPerWorldStorage();
        VillageStreetGuardData data = (VillageStreetGuardData) storage.getOrLoadData(VillageStreetGuardData.class, DATA_NAME);
        if (data == null)
        {
            data = new VillageStreetGuardData(DATA_NAME);
            storage.setData(DATA_NAME, data);
        }
        return data;
    }

    public boolean isProcessed(BlockPos center)
    {
        for (long packed : processed)
        {
            BlockPos other = BlockPos.fromLong(packed);
            if (other.distanceSq(center) <= MATCH_DISTANCE_SQ)
            {
                return true;
            }
        }
        return false;
    }

    public void markProcessed(BlockPos center)
    {
        processed.add(center.toLong());
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        processed.clear();
        NBTTagList list = nbt.getTagList("Villages", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++)
        {
            processed.add(list.getCompoundTagAt(i).getLong("P"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList list = new NBTTagList();
        for (long packed : processed)
        {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("P", packed);
            list.appendTag(tag);
        }
        nbt.setTag("Villages", list);
        return nbt;
    }
}
