package com.witcherywalls.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class VillageWallChunkHandler
{
    private VillageWallChunkHandler()
    {
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event)
    {
        if (!(event.getLevel() instanceof ServerLevel level))
        {
            return;
        }

        LevelChunk chunk = (LevelChunk) event.getChunk();

        for (BlockPos pos : chunk.getBlockEntities().keySet())
        {
            if (!chunk.getBlockState(pos).is(Blocks.BELL))
            {
                continue;
            }

            if (VillageWallSavedData.get(level).isNearProcessed(pos))
            {
                continue;
            }

            VillageWallScheduler.schedule(level, pos);
        }
    }
}
