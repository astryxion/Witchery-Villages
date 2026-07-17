package com.witcherywalls.init;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.block.VillageWallGenBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, WitcheryWallsMod.MODID);

    public static final RegistryObject<VillageWallGenBlock> VILLAGE_WALL_GEN =
            BLOCKS.register("village_wall_gen", VillageWallGenBlock::new);

    private ModBlocks()
    {
    }
}
