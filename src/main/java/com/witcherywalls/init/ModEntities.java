package com.witcherywalls.init;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.entity.VillageGuard;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, WitcheryWallsMod.MODID);

    public static final RegistryObject<EntityType<VillageGuard>> VILLAGE_GUARD = ENTITY_TYPES.register("village_guard",
            () -> EntityType.Builder.of(VillageGuard::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(80)
                    .updateInterval(3)
                    .build(WitcheryWallsMod.MODID + ":village_guard"));

    private ModEntities()
    {
    }
}
