package com.witcherywalls.block;

import com.witcherywalls.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/**
 * Hidden technical block used once per walled village to expand paths into perimeter walls.
 */
public class VillageWallGenBlock extends BaseEntityBlock
{
    public VillageWallGenBlock()
    {
        super(Properties.of()
                .mapColor(MapColor.NONE)
                .strength(10000.0F, 6000000.0F)
                .noLootTable()
                .noOcclusion());
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
    {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new VillageWallGenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.VILLAGE_WALL_GEN.get(),
                VillageWallGenBlockEntity::serverTick);
    }
}
