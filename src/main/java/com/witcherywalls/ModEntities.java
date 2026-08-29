package com.witcherywalls;

import com.witcherywalls.config.WitcheryWallsConfig;
import com.witcherywalls.entity.EntityVillageGuard;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class ModEntities
{
    public static final ResourceLocation VILLAGE_GUARD_ID = new ResourceLocation(WitcheryWallsMod.MODID, "village_guard");

    private static final int VILLAGE_GUARD_ID_NUM = 0;
    private static final int VILLAGE_GUARD_EGG_PRIMARY = 0x222222;
    private static final int VILLAGE_GUARD_EGG_SECONDARY = 0x515A2A;

    private static EntityList.EntityEggInfo villageGuardEgg;

    private ModEntities()
    {
    }

    public static void register()
    {
        EntityRegistry.registerModEntity(
                VILLAGE_GUARD_ID,
                EntityVillageGuard.class,
                "village_guard",
                VILLAGE_GUARD_ID_NUM,
                WitcheryWallsMod.instance,
                80,
                3,
                true,
                VILLAGE_GUARD_EGG_PRIMARY,
                VILLAGE_GUARD_EGG_SECONDARY
        );
        updateSpawnEggVisibility();
    }

    public static void updateSpawnEggVisibility()
    {
        EntityEntry entry = ForgeRegistries.ENTITIES.getValue(VILLAGE_GUARD_ID);
        if (entry == null)
        {
            return;
        }
        if (villageGuardEgg == null && entry.getEgg() != null)
        {
            villageGuardEgg = entry.getEgg();
        }
        if (villageGuardEgg == null)
        {
            return;
        }

        if (WitcheryWallsConfig.spawnVillageGuards())
        {
            entry.setEgg(villageGuardEgg);
            EntityList.ENTITY_EGGS.put(VILLAGE_GUARD_ID, villageGuardEgg);
        }
        else
        {
            entry.setEgg(null);
            EntityList.ENTITY_EGGS.remove(VILLAGE_GUARD_ID);
        }
    }
}
