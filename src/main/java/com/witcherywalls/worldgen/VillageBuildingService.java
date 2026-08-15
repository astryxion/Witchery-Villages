package com.witcherywalls.worldgen;

import com.witcherywalls.WitcheryWallsMod;
import com.witcherywalls.config.WitcheryWallsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Places keep and watch towers like 1.12 village pieces: flush to the dirt path,
 * door facing the road, without overlapping existing houses.
 */
public final class VillageBuildingService
{
    private static final int KEEP_WIDTH = 16;
    private static final int KEEP_HEIGHT = 26;
    private static final int KEEP_DEPTH = 16;
    private static final int TOWER_WIDTH = 8;
    private static final int TOWER_HEIGHT = 23;
    private static final int TOWER_DEPTH = 8;
    private static final int WALL_MARGIN = 4;
    private static final int MAX_WATCH_TOWERS = 3;
    private static final int VILLAGE_STREET_GUARDS = 6;
    private static final int STREET_GUARD_MIN_DISTANCE = 12;

    private VillageBuildingService()
    {
    }

    public static void place(ServerLevel level, BlockPos center, VillageStructureLocator.LocatedVillage located)
    {
        VillagePalette palette = located.palette();
        Random random = new Random(level.getSeed() ^ center.asLong() ^ 0x4B454550L);
        BoundingBox village = located.villageBox();
        List<BoundingBox> streets = located.streetBoxes();
        List<BoundingBox> occupied = buildingsOnly(located.occupiedBoxes(), streets);
        List<VillageWallGenerator.StructureBounds> wallInterior = located.bounds();

        BoundingBox keepBox = null;
        if (WitcheryWallsConfig.roll(random, WitcheryWallsConfig.KEEP_SPAWN_CHANCE))
        {
            keepBox = placeKeep(level, village, streets, occupied, wallInterior, palette, random);
            if (keepBox != null)
            {
                occupied.add(keepBox);
            }
        }

        int placedTowers = 0;
        if (WitcheryWallsConfig.roll(random, WitcheryWallsConfig.WATCH_TOWER_SPAWN_CHANCE))
        {
            int towers = 1 + random.nextInt(MAX_WATCH_TOWERS);
            placedTowers = placeWatchTowers(level, village, streets, occupied, wallInterior, palette, random, towers);
        }

        int streetGuards = spawnStreetGuards(level, village, streets, random, VILLAGE_STREET_GUARDS);

        WitcheryWallsMod.getLogger().info("Queued village keep ({}) and {} watch tower(s) along streets at {}, spawned {} street guards",
                keepBox != null, placedTowers, center, streetGuards);
    }

    private static BoundingBox placeKeep(ServerLevel level, BoundingBox village, List<BoundingBox> streets,
                                         List<BoundingBox> occupied, List<VillageWallGenerator.StructureBounds> wallInterior,
                                         VillagePalette palette, Random random)
    {
        List<Candidate> candidates = collectCandidates(level, village, streets, occupied, wallInterior,
                KEEP_WIDTH, KEEP_DEPTH, true);
        Candidate chosen = pickRandomGood(candidates, random);
        if (chosen == null)
        {
            WitcheryWallsMod.getLogger().debug("No path-side slot found for keep inside village {}", village);
            return null;
        }

        return generateAt(level, chosen, KEEP_WIDTH, KEEP_HEIGHT, KEEP_DEPTH, palette, random, true);
    }

    private static int placeWatchTowers(ServerLevel level, BoundingBox village, List<BoundingBox> streets,
                                        List<BoundingBox> occupied, List<VillageWallGenerator.StructureBounds> wallInterior,
                                        VillagePalette palette, Random random, int targetCount)
    {
        List<Candidate> candidates = collectCandidates(level, village, streets, occupied, wallInterior,
                TOWER_WIDTH, TOWER_DEPTH, false);
        candidates.sort(Comparator.comparingInt((Candidate c) -> c.score).reversed());

        int placed = 0;
        while (placed < targetCount && !candidates.isEmpty())
        {
            int window = Math.min(candidates.size(), Math.max(3, candidates.size() / 2));
            Candidate candidate = candidates.remove(random.nextInt(window));
            if (overlapAreaSum(candidate.box, occupied) > 0)
            {
                continue;
            }

            BoundingBox placedBox = generateAt(level, candidate, TOWER_WIDTH, TOWER_HEIGHT, TOWER_DEPTH, palette, random, false);
            if (placedBox != null)
            {
                occupied.add(placedBox);
                placed++;
            }
        }
        return placed;
    }

    private static List<Candidate> collectCandidates(ServerLevel level, BoundingBox village, List<BoundingBox> streets,
                                                     List<BoundingBox> occupied, List<VillageWallGenerator.StructureBounds> wallInterior,
                                                     int width, int depth, boolean keep)
    {
        List<BlockPos> paths = findPathBlocks(level, streets, village);
        int doorOffset = keep ? 8 : 4;
        List<Candidate> candidates = new ArrayList<>();

        for (int margin : new int[] {WALL_MARGIN, 3, 2})
        {
            Set<Attach> seen = new HashSet<>();
            for (BlockPos path : paths)
            {
                int px = path.getX();
                int pz = path.getZ();
                addPathCandidate(candidates, seen, level, village, occupied, wallInterior,
                        px - doorOffset, pz + 1, Direction.SOUTH, width, depth, margin, keep);
                addPathCandidate(candidates, seen, level, village, occupied, wallInterior,
                        px - doorOffset, pz - 1, Direction.NORTH, width, depth, margin, keep);
                addPathCandidate(candidates, seen, level, village, occupied, wallInterior,
                        px + 1, pz - doorOffset, Direction.EAST, width, depth, margin, keep);
                addPathCandidate(candidates, seen, level, village, occupied, wallInterior,
                        px - 1, pz - doorOffset, Direction.WEST, width, depth, margin, keep);
            }

            if (!candidates.isEmpty())
            {
                return candidates;
            }
        }

        return candidates;
    }

    private static void addPathCandidate(List<Candidate> candidates, Set<Attach> seen, ServerLevel level,
                                         BoundingBox village, List<BoundingBox> occupied,
                                         List<VillageWallGenerator.StructureBounds> wallInterior,
                                         int attachX, int attachZ, Direction facing,
                                         int width, int depth, int margin, boolean keep)
    {
        Attach key = new Attach(attachX, attachZ, facing);
        if (!seen.add(key))
        {
            return;
        }

        BoundingBox box = VillagePieceBuilder.boxFor(attachX, village.minY(), attachZ, 0, 0, 0, width, 8, depth, facing);
        if (!insideWallInterior(box, wallInterior, margin))
        {
            return;
        }
        if (overlapAreaSum(box, occupied) > 0)
        {
            return;
        }
        if (countFootprintPath(level, box) > 0)
        {
            return;
        }

        int pathContact = countFrontPath(level, box, facing);
        int minPath = keep ? 3 : 2;
        if (pathContact < minPath)
        {
            return;
        }
        if (keep && countDoorPath(level, box, facing) < 2)
        {
            return;
        }

        int score = pathContact * 25;
        if (keep)
        {
            score += countDoorPath(level, box, facing) * 40;
        }
        candidates.add(new Candidate(attachX, attachZ, facing, box, score));
    }

    private static Candidate pickRandomGood(List<Candidate> candidates, Random random)
    {
        if (candidates.isEmpty())
        {
            return null;
        }
        candidates.sort(Comparator.comparingInt((Candidate c) -> c.score).reversed());
        int take = Math.min(4, candidates.size());
        return candidates.get(random.nextInt(take));
    }

    private static BoundingBox generateAt(ServerLevel level, Candidate candidate, int width, int height, int depth,
                                          VillagePalette palette, Random random, boolean keep)
    {
        BoundingBox box = VillagePieceBuilder.boxFor(
                candidate.attachX, 0, candidate.attachZ, 0, 0, 0, width, height, depth, candidate.facing);
        int ground = VillagePieceBuilder.averageGroundY(level, box);
        if (ground <= level.getMinBuildHeight())
        {
            return null;
        }
        box = box.moved(0, ground - box.minY(), 0);
        VillagePieceBuilder builder = new VillagePieceBuilder(level, box, candidate.facing, palette);
        if (keep)
        {
            VillageKeepGenerator.generate(builder, random);
        }
        else
        {
            VillageWatchTowerGenerator.generate(builder, random);
        }
        return box;
    }

    private static int spawnStreetGuards(ServerLevel level, BoundingBox village, List<BoundingBox> streets,
                                         Random random, int count)
    {
        List<BlockPos> paths = findPathBlocks(level, streets, village);
        if (paths.isEmpty())
        {
            paths = sampleStreetColumns(level, streets, village);
        }
        if (paths.isEmpty())
        {
            return 0;
        }

        Collections.shuffle(paths, random);
        List<BlockPos> chosen = new ArrayList<>();
        int minDist = STREET_GUARD_MIN_DISTANCE;
        while (chosen.size() < count && minDist >= 4)
        {
            chosen.clear();
            for (BlockPos path : paths)
            {
                if (chosen.size() >= count)
                {
                    break;
                }

                int x = path.getX();
                int z = path.getZ();
                if (!level.hasChunk(x >> 4, z >> 4))
                {
                    continue;
                }

                BlockPos spawn = pathSpawnPos(level, x, z);
                if (spawn == null || tooClose(spawn, chosen, minDist))
                {
                    continue;
                }
                chosen.add(spawn);
            }
            minDist -= 4;
        }

        for (BlockPos pos : chosen)
        {
            VillageGuardSpawnQueue.schedule(level, pos, VillageGuardSpawnQueue.Site.STREET);
        }
        return chosen.size();
    }

    private static boolean tooClose(BlockPos pos, List<BlockPos> chosen, int minDist)
    {
        int minDistSq = minDist * minDist;
        for (BlockPos other : chosen)
        {
            int dx = pos.getX() - other.getX();
            int dz = pos.getZ() - other.getZ();
            if (dx * dx + dz * dz < minDistSq)
            {
                return true;
            }
        }
        return false;
    }

    private static List<BlockPos> findPathBlocks(ServerLevel level, List<BoundingBox> streets, BoundingBox village)
    {
        List<BoundingBox> scan = streets.isEmpty() ? List.of(village) : streets;
        List<BlockPos> paths = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (BoundingBox area : scan)
        {
            for (int x = area.minX() - 1; x <= area.maxX() + 1; x++)
            {
                for (int z = area.minZ() - 1; z <= area.maxZ() + 1; z++)
                {
                    BlockPos floor = findPathFloor(level, x, z);
                    if (floor == null)
                    {
                        continue;
                    }
                    if (seen.add(BlockPos.asLong(x, 0, z)))
                    {
                        paths.add(floor);
                    }
                }
            }
        }
        return paths;
    }

    private static List<BlockPos> sampleStreetColumns(ServerLevel level, List<BoundingBox> streets, BoundingBox village)
    {
        List<BoundingBox> scan = streets.isEmpty() ? List.of(village) : streets;
        List<BlockPos> columns = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (BoundingBox area : scan)
        {
            int step = 4;
            for (int x = area.minX(); x <= area.maxX(); x += step)
            {
                for (int z = area.minZ(); z <= area.maxZ(); z += step)
                {
                    if (!level.hasChunk(x >> 4, z >> 4))
                    {
                        continue;
                    }
                    if (seen.add(BlockPos.asLong(x, 0, z)))
                    {
                        columns.add(new BlockPos(x, 0, z));
                    }
                }
            }
        }
        return columns;
    }

    private static BlockPos pathSpawnPos(ServerLevel level, int x, int z)
    {
        BlockPos floor = findPathFloor(level, x, z);
        if (floor != null)
        {
            return floor.above();
        }
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }

    private static BlockPos findPathFloor(ServerLevel level, int x, int z)
    {
        if (!level.hasChunk(x >> 4, z >> 4))
        {
            return null;
        }
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        for (int y = top; y >= top - 12; y--)
        {
            BlockPos floor = new BlockPos(x, y, z);
            if (VillagePathScanner.isPathBlock(level, floor))
            {
                return floor;
            }
        }
        return null;
    }

    private static List<BoundingBox> buildingsOnly(List<BoundingBox> occupied, List<BoundingBox> streets)
    {
        List<BoundingBox> buildings = new ArrayList<>();
        for (BoundingBox box : occupied)
        {
            if (!isStreetBox(box, streets))
            {
                buildings.add(box);
            }
        }
        return buildings;
    }

    private static boolean isStreetBox(BoundingBox box, List<BoundingBox> streets)
    {
        for (BoundingBox street : streets)
        {
            if (box.minX() == street.minX() && box.maxX() == street.maxX()
                    && box.minZ() == street.minZ() && box.maxZ() == street.maxZ())
            {
                return true;
            }
        }
        return false;
    }

    private static int countFootprintPath(ServerLevel level, BoundingBox box)
    {
        int count = 0;
        for (int x = box.minX(); x <= box.maxX(); x++)
        {
            for (int z = box.minZ(); z <= box.maxZ(); z++)
            {
                if (isPath(level, x, z))
                {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countFrontPath(ServerLevel level, BoundingBox box, Direction facing)
    {
        int[] count = {0};
        forEachFrontAdjacent(box, facing, (x, z) ->
        {
            if (isPath(level, x, z))
            {
                count[0]++;
            }
        });
        return count[0];
    }

    private static int countDoorPath(ServerLevel level, BoundingBox box, Direction facing)
    {
        int[] count = {0};
        forEachDoorFront(box, facing, (x, z) ->
        {
            if (isPath(level, x, z))
            {
                count[0]++;
            }
        });
        return count[0];
    }

    private static void forEachFrontAdjacent(BoundingBox box, Direction facing, CoordConsumer consumer)
    {
        switch (facing)
        {
            case SOUTH ->
            {
                int z = box.minZ() - 1;
                for (int x = box.minX(); x <= box.maxX(); x++)
                {
                    consumer.accept(x, z);
                }
            }
            case NORTH ->
            {
                int z = box.maxZ() + 1;
                for (int x = box.minX(); x <= box.maxX(); x++)
                {
                    consumer.accept(x, z);
                }
            }
            case EAST ->
            {
                int x = box.minX() - 1;
                for (int z = box.minZ(); z <= box.maxZ(); z++)
                {
                    consumer.accept(x, z);
                }
            }
            default ->
            {
                int x = box.maxX() + 1;
                for (int z = box.minZ(); z <= box.maxZ(); z++)
                {
                    consumer.accept(x, z);
                }
            }
        }
    }

    private static void forEachDoorFront(BoundingBox box, Direction facing, CoordConsumer consumer)
    {
        switch (facing)
        {
            case SOUTH ->
            {
                int z = box.minZ() - 1;
                int mid = box.minX() + 8;
                for (int x = mid - 1; x <= mid + 1; x++)
                {
                    consumer.accept(x, z);
                }
            }
            case NORTH ->
            {
                int z = box.maxZ() + 1;
                int mid = box.minX() + 8;
                for (int x = mid - 1; x <= mid + 1; x++)
                {
                    consumer.accept(x, z);
                }
            }
            case EAST ->
            {
                int x = box.minX() - 1;
                int mid = box.minZ() + 8;
                for (int z = mid - 1; z <= mid + 1; z++)
                {
                    consumer.accept(x, z);
                }
            }
            default ->
            {
                int x = box.maxX() + 1;
                int mid = box.minZ() + 8;
                for (int z = mid - 1; z <= mid + 1; z++)
                {
                    consumer.accept(x, z);
                }
            }
        }
    }

    private static boolean isPath(ServerLevel level, int x, int z)
    {
        return findPathFloor(level, x, z) != null;
    }

    private static boolean insideWallInterior(BoundingBox box, List<VillageWallGenerator.StructureBounds> bounds, int margin)
    {
        if (bounds == null || bounds.isEmpty())
        {
            return false;
        }
        for (int x = box.minX(); x <= box.maxX(); x++)
        {
            for (int z = box.minZ(); z <= box.maxZ(); z++)
            {
                if (!coveredByWallInterior(x, z, bounds, margin))
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean coveredByWallInterior(int x, int z, List<VillageWallGenerator.StructureBounds> bounds, int margin)
    {
        for (VillageWallGenerator.StructureBounds bound : bounds)
        {
            if (x >= bound.minX + margin && x <= bound.maxX - margin
                    && z >= bound.minZ + margin && z <= bound.maxZ - margin)
            {
                return true;
            }
        }
        return false;
    }

    private static int overlapAreaSum(BoundingBox box, List<BoundingBox> occupied)
    {
        int overlap = 0;
        for (BoundingBox other : occupied)
        {
            overlap += overlapArea(box, other);
        }
        return overlap;
    }

    private static int overlapArea(BoundingBox a, BoundingBox b)
    {
        int minX = Math.max(a.minX(), b.minX());
        int maxX = Math.min(a.maxX(), b.maxX());
        int minZ = Math.max(a.minZ(), b.minZ());
        int maxZ = Math.min(a.maxZ(), b.maxZ());
        if (minX > maxX || minZ > maxZ)
        {
            return 0;
        }
        return (maxX - minX + 1) * (maxZ - minZ + 1);
    }

    private record Candidate(int attachX, int attachZ, Direction facing, BoundingBox box, int score)
    {
    }

    private record Attach(int attachX, int attachZ, Direction facing)
    {
    }

    @FunctionalInterface
    private interface CoordConsumer
    {
        void accept(int x, int z);
    }
}
