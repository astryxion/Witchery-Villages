package com.witcherywalls.init;

import com.witcherywalls.WitcheryWallsMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems
{
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, WitcheryWallsMod.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> VILLAGE_GUARD_SPAWN_EGG = ITEMS.register(
            "village_guard_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.VILLAGE_GUARD, 0x222222, 0x513830, new Item.Properties()));

    private ModItems()
    {
    }
}
