package com.witcherywalls.init;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.block.VillageWallGenBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, WitcheryWallsMod.MODID);

    public static final RegistryObject<BlockEntityType<VillageWallGenBlockEntity>> VILLAGE_WALL_GEN =
            BLOCK_ENTITIES.register("village_wall_gen",
                    () -> BlockEntityType.Builder.of(VillageWallGenBlockEntity::new, ModBlocks.VILLAGE_WALL_GEN.get())
                            .build(null));

    private ModBlockEntities()
    {
    }
}
