package com.witcherywalls.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Resolves village layout from saved structure pieces — the 1.20 equivalent of
 * reading {@code StructureVillagePieces.Road} bounding boxes in 1.12.
 */
public final class VillageStructureLocator
{
    private static final int STREET_EXPANSION_X = 20;
    private static final int STREET_EXPANSION_Z = 7;
    private static final int BUILDING_EXPANSION_X = 10;
    private static final int BUILDING_EXPANSION_Z = 7;

    private VillageStructureLocator()
    {
    }

    public static LocatedVillage find(ServerLevel level, BlockPos center)
    {
        StructureStart start = findVillageStart(level, center);
        if (!start.isValid() || start.getPieces().isEmpty())
        {
            return null;
        }

        List<BoundingBox> streetBoxes = new ArrayList<>();
        List<BoundingBox> occupiedBoxes = new ArrayList<>();
        List<VillageWallGenerator.StructureBounds> streets = new ArrayList<>();
        List<VillageWallGenerator.StructureBounds> buildings = new ArrayList<>();
        long ySum = 0;
        int yCount = 0;

        for (StructurePiece piece : start.getPieces())
        {
            BoundingBox box = piece.getBoundingBox();
            occupiedBoxes.add(box);
            ySum += box.minY();
            yCount++;

            if (isStreetPiece(piece))
            {
                streetBoxes.add(box);
                streets.add(toBounds(box, STREET_EXPANSION_X, STREET_EXPANSION_Z));
            }
            else
            {
                buildings.add(toBounds(box, BUILDING_EXPANSION_X, BUILDING_EXPANSION_Z));
            }
        }

        List<VillageWallGenerator.StructureBounds> bounds = streets.isEmpty() ? buildings : streets;
        if (bounds.isEmpty())
        {
            BoundingBox box = start.getBoundingBox();
            bounds.add(toBounds(box, BUILDING_EXPANSION_X, BUILDING_EXPANSION_Z));
        }

        int groundY = yCount == 0 ? center.getY() : (int) (ySum / yCount);
        VillagePalette palette = VillagePalette.fromVillageStructure(level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .getKey(start.getStructure()));
        return new LocatedVillage(bounds, groundY, palette.type() == VillagePalette.Type.DESERT,
                start.getBoundingBox(), palette, streetBoxes, occupiedBoxes, isAbandonedVillage(start));
    }

    public static StructureStart findVillageStart(ServerLevel level, BlockPos center)
    {
        StructureStart withPiece = level.structureManager().getStructureWithPieceAt(center, StructureTags.VILLAGE);
        if (withPiece.isValid())
        {
            return withPiece;
        }

        Optional<HolderSet.Named<Structure>> villages = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .getTag(StructureTags.VILLAGE);
        if (villages.isEmpty())
        {
            return StructureStart.INVALID_START;
        }

        for (Holder<Structure> holder : villages.get())
        {
            StructureStart start = level.structureManager().getStructureAt(center, holder.value());
            if (start.isValid())
            {
                return start;
            }
        }

        return StructureStart.INVALID_START;
    }

    private static VillageWallGenerator.StructureBounds toBounds(BoundingBox box, int expansionX, int expansionZ)
    {
        return new VillageWallGenerator.StructureBounds(
                box.minX(), box.minY(), box.minZ(),
                box.maxX(), box.maxY(), box.maxZ(),
                expansionX, expansionZ);
    }

    private static boolean isAbandonedVillage(StructureStart start)
    {
        for (StructurePiece piece : start.getPieces())
        {
            if (isAbandonedPiece(piece))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isAbandonedPiece(StructurePiece piece)
    {
        String text = pieceText(piece);
        return text.contains("/zombie/") || text.contains("\\zombie\\");
    }

    private static boolean isStreetPiece(StructurePiece piece)
    {
        String text = pieceText(piece);
        return text.contains("/streets/")
                || text.contains("/terminators/")
                || text.contains("street");
    }

    private static String pieceText(StructurePiece piece)
    {
        String desc = piece instanceof PoolElementStructurePiece poolPiece
                ? poolPiece.getElement().toString()
                : piece.toString();
        return desc.toLowerCase(Locale.ROOT);
    }

    public record LocatedVillage(List<VillageWallGenerator.StructureBounds> bounds, int groundY, boolean desert,
                                BoundingBox villageBox, VillagePalette palette,
                                List<BoundingBox> streetBoxes, List<BoundingBox> occupiedBoxes,
                                boolean abandoned)
    {
    }
}
