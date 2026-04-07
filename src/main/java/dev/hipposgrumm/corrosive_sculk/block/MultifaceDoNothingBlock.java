package dev.hipposgrumm.corrosive_sculk.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

public class MultifaceDoNothingBlock extends MultifaceBlock implements SimpleWaterloggedBlock {
    //? if <1.21.2 {
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private final MultifaceSpreader spreader = new DoNothingSpreader();
    //?}

    public MultifaceDoNothingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        //? if <1.21.2
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false));
    }

    //? if >1.20.1 {
    /*public static final MapCodec<MultifaceDoNothingBlock> CODEC = simpleCodec(MultifaceDoNothingBlock::new);
    @Override protected MapCodec<? extends MultifaceBlock> codec() {
        return CODEC;
    }
    *///?}

    //? if <1.21.2 {
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> arg) {
        super.createBlockStateDefinition(arg.add(WATERLOGGED));
    }

    public BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, otherState, level, pos, otherPos);
    }

    public FluidState getFluidState(BlockState arg) {
        return arg.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(arg);
    }

    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().getItem() instanceof BlockItem block
                && block.getBlock() == this
                && super.canBeReplaced(state, context);
    }

    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }

    private static class DoNothingSpreader extends MultifaceSpreader {
        public DoNothingSpreader() {
            super((MultifaceBlock) null);
        }

        @Override
        public boolean canSpreadInAnyDirection(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
            return false;
        }

        @Override
        public Optional<SpreadPos> spreadFromRandomFaceTowardRandomDirection(BlockState state, LevelAccessor level, BlockPos pos, RandomSource random) {
            return Optional.empty();
        }

        @Override
        public long spreadAll(BlockState state, LevelAccessor level, BlockPos pos, boolean update) {
            return 0;
        }

        @Override
        public Optional<SpreadPos> spreadFromFaceTowardRandomDirection(BlockState state, LevelAccessor level, BlockPos pos, Direction curDirection, RandomSource random, boolean update) {
            return Optional.empty();
        }

        @Override
        public Optional<SpreadPos> spreadFromFaceTowardDirection(BlockState state, LevelAccessor level, BlockPos pos, Direction curDirection, Direction newDirection, boolean update) {
            return Optional.empty();
        }

        @Override
        public Optional<SpreadPos> getSpreadFromFaceTowardDirection(BlockState state, BlockGetter level, BlockPos pos, Direction curDirection, Direction newDirection, SpreadPredicate spreadPredicate) {
            return Optional.empty();
        }

        @Override
        public Optional<SpreadPos> spreadToFace(LevelAccessor level, SpreadPos proposedSpread, boolean update) {
            return Optional.empty();
        }
    }
    //?}
}
