package com.witcherywalls.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.StructureStart;
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
        VillageWallDeferred.notifyChunkLoaded(level, chunk.getPos());

        for (StructureStart start : chunk.getAllStarts().values())
        {
            if (!start.isValid())
            {
                continue;
            }

            var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            boolean village = registry.getResourceKey(start.getStructure())
                    .flatMap(registry::getHolder)
                    .map(holder -> holder.is(StructureTags.VILLAGE))
                    .orElse(false);
            if (village)
            {
                BlockPos origin = start.getPieces().isEmpty()
                        ? start.getBoundingBox().getCenter()
                        : start.getPieces().get(0).getBoundingBox().getCenter();
                VillageWallScheduler.schedule(level, origin);
                return;
            }
        }

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
            return;
        }
    }
}
