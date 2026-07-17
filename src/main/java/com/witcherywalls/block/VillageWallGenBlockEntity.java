package com.witcherywalls.block;

import com.witcherywalls.init.ModBlockEntities;
import com.witcherywalls.worldgen.VillageWallScheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class VillageWallGenBlockEntity extends BlockEntity
{
    private static final long TIMEOUT_TICKS = 1000L;

    private long ticks;

    public VillageWallGenBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.VILLAGE_WALL_GEN.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VillageWallGenBlockEntity blockEntity)
    {
        blockEntity.tick(level);
    }

    private void tick(Level level)
    {
        ticks++;

        if (ticks == 1L && level instanceof ServerLevel serverLevel)
        {
            VillageWallScheduler.schedule(serverLevel, worldPosition);
            level.removeBlock(worldPosition, false);
        }
        else if (ticks > TIMEOUT_TICKS)
        {
            level.removeBlock(worldPosition, false);
        }
    }
}
