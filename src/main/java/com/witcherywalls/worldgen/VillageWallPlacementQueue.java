package com.witcherywalls.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public final class VillageWallPlacementQueue
{
    private static final int BLOCKS_PER_TICK = 600;
    private static final int PLACE_FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final Map<ChunkKey, LinkedHashMap<Long, PendingBlock>> BY_CHUNK = new LinkedHashMap<>();

    private VillageWallPlacementQueue()
    {
    }

    public static void enqueue(Level level, BlockPos pos, BlockState state, boolean replaceSoftBlocks)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            placeNow(level, pos, state, replaceSoftBlocks);
            return;
        }

        ChunkKey key = new ChunkKey(serverLevel, ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        BY_CHUNK.computeIfAbsent(key, ignored -> new LinkedHashMap<>())
                .put(pos.asLong(), new PendingBlock(serverLevel, pos.immutable(), state, replaceSoftBlocks));
    }

    public static void tick()
    {
        if (BY_CHUNK.isEmpty())
        {
            return;
        }

        int placed = 0;
        Iterator<Map.Entry<ChunkKey, LinkedHashMap<Long, PendingBlock>>> chunks = BY_CHUNK.entrySet().iterator();

        while (chunks.hasNext() && placed < BLOCKS_PER_TICK)
        {
            Map.Entry<ChunkKey, LinkedHashMap<Long, PendingBlock>> entry = chunks.next();
            ChunkKey key = entry.getKey();
            int chunkX = ChunkPos.getX(key.chunkLong);
            int chunkZ = ChunkPos.getZ(key.chunkLong);
            if (!key.level.hasChunk(chunkX, chunkZ))
            {
                continue;
            }

            Iterator<PendingBlock> blocks = entry.getValue().values().iterator();
            while (blocks.hasNext() && placed < BLOCKS_PER_TICK)
            {
                PendingBlock pending = blocks.next();
                placeNow(pending.level, pending.pos, pending.state, pending.replaceSoftBlocks);
                blocks.remove();
                placed++;
            }

            if (entry.getValue().isEmpty())
            {
                chunks.remove();
            }
        }
    }

    public static BlockState getPlanned(Level level, BlockPos pos)
    {
        if (level instanceof ServerLevel serverLevel)
        {
            ChunkKey key = new ChunkKey(serverLevel, ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
            LinkedHashMap<Long, PendingBlock> chunk = BY_CHUNK.get(key);
            if (chunk != null)
            {
                PendingBlock pending = chunk.get(pos.asLong());
                if (pending != null)
                {
                    return pending.state;
                }
            }
        }
        if (level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4))
        {
            return level.getBlockState(pos);
        }
        return Blocks.AIR.defaultBlockState();
    }

    public static boolean hasPending(Level level, BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            return false;
        }
        ChunkKey key = new ChunkKey(serverLevel, ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        LinkedHashMap<Long, PendingBlock> chunk = BY_CHUNK.get(key);
        return chunk != null && chunk.containsKey(pos.asLong());
    }

    public static boolean isBusy()
    {
        for (Map.Entry<ChunkKey, LinkedHashMap<Long, PendingBlock>> entry : BY_CHUNK.entrySet())
        {
            ChunkKey key = entry.getKey();
            if (!entry.getValue().isEmpty()
                    && key.level.hasChunk(ChunkPos.getX(key.chunkLong), ChunkPos.getZ(key.chunkLong)))
            {
                return true;
            }
        }
        return false;
    }

    public static int size()
    {
        int total = 0;
        for (LinkedHashMap<Long, PendingBlock> chunk : BY_CHUNK.values())
        {
            total += chunk.size();
        }
        return total;
    }

    private static void placeNow(Level level, BlockPos pos, BlockState state, boolean replaceSoftBlocks)
    {
        BlockState existing = level.getBlockState(pos);
        if (VillagePathScanner.isProtectedFromWall(existing))
        {
            return;
        }

        if (replaceSoftBlocks && !VillageWallGenerator.canReplaceForWall(existing))
        {
            return;
        }

        level.setBlock(pos, state, PLACE_FLAGS);
    }

    private record ChunkKey(ServerLevel level, long chunkLong)
    {
    }

    private record PendingBlock(ServerLevel level, BlockPos pos, BlockState state, boolean replaceSoftBlocks)
    {
    }
}
