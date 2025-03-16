package me.dienes.abseil;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;

public class ClimbingRopeBlock extends Block implements Waterloggable, ModifiableDropPos {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<ClimbingRopeSegment> CLIMBING_ROPE_SEGMENT = EnumProperty.of("climbing_rope_segment", ClimbingRopeSegment.class);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    protected static final VoxelShape NORTH_SHAPE_BASE = Block.createCuboidShape(7.4, 7.0, 7.9, 8.6, 16.0, 9.1);
    protected static final VoxelShape EAST_SHAPE_BASE = Block.createCuboidShape(6.9, 7.0, 7.4, 8.1, 16.0, 8.6);
    protected static final VoxelShape SOUTH_SHAPE_BASE = Block.createCuboidShape(7.4, 7.0, 6.9, 8.6, 16.0, 8.1);
    protected static final VoxelShape WEST_SHAPE_BASE = Block.createCuboidShape(7.9, 7.0, 7.4, 9.1, 16.0, 8.6);

    protected static final VoxelShape NORTH_SHAPE_TOP_EXTENSION = Block.createCuboidShape(7.4, 16.0, 7.9, 8.6, 23.0, 9.1);
    protected static final VoxelShape EAST_SHAPE_TOP_EXTENSION = Block.createCuboidShape(6.9, 16.0, 7.4, 8.1, 23.0, 8.6);
    protected static final VoxelShape SOUTH_SHAPE_TOP_EXTENSION = Block.createCuboidShape(7.4, 16.0, 6.9, 8.6, 23.0, 8.1);
    protected static final VoxelShape WEST_SHAPE_TOP_EXTENSION = Block.createCuboidShape(7.9, 16.0, 7.4, 9.1, 23.0, 8.6);

    protected static final VoxelShape NORTH_SHAPE_BOTTOM_EXTENSION = Block.createCuboidShape(7.4, 0.0, 7.9, 8.6, 9.0, 9.1);
    protected static final VoxelShape EAST_SHAPE_BOTTOM_EXTENSION = Block.createCuboidShape(6.9, 0.0, 7.4, 8.1, 9.0, 8.6);
    protected static final VoxelShape SOUTH_SHAPE_BOTTOM_EXTENSION = Block.createCuboidShape(7.4, 0.0, 6.9, 8.6, 9.0, 8.1);
    protected static final VoxelShape WEST_SHAPE_BOTTOM_EXTENSION = Block.createCuboidShape(7.9, 0.0, 7.4, 9.1, 9.0, 8.6);

    protected static final VoxelShape[] NORTH_SHAPES = ClimbingRopeBlock.composeShapes(NORTH_SHAPE_BASE, NORTH_SHAPE_TOP_EXTENSION, NORTH_SHAPE_BOTTOM_EXTENSION);
    protected static final VoxelShape[] EAST_SHAPES = ClimbingRopeBlock.composeShapes(EAST_SHAPE_BASE, EAST_SHAPE_TOP_EXTENSION, EAST_SHAPE_BOTTOM_EXTENSION);
    protected static final VoxelShape[] SOUTH_SHAPES = ClimbingRopeBlock.composeShapes(SOUTH_SHAPE_BASE, SOUTH_SHAPE_TOP_EXTENSION, SOUTH_SHAPE_BOTTOM_EXTENSION);
    protected static final VoxelShape[] WEST_SHAPES = ClimbingRopeBlock.composeShapes(WEST_SHAPE_BASE, WEST_SHAPE_TOP_EXTENSION, WEST_SHAPE_BOTTOM_EXTENSION);

    protected static VoxelShape[] composeShapes(VoxelShape base, VoxelShape topExtension, VoxelShape bottomExtension) {
        VoxelShape[] shapes = new VoxelShape[ClimbingRopeSegment.values().length];

        shapes[ClimbingRopeSegment.TOP.ordinal()] = VoxelShapes.union(base, topExtension, bottomExtension);
        shapes[ClimbingRopeSegment.MIDDLE.ordinal()] = VoxelShapes.union(base, bottomExtension);
        shapes[ClimbingRopeSegment.BOTTOM.ordinal()] = base;
        shapes[ClimbingRopeSegment.TOP_BOTTOM.ordinal()] = VoxelShapes.union(base, topExtension);

        return shapes;
    }

    public ClimbingRopeBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(CLIMBING_ROPE_SEGMENT, ClimbingRopeSegment.TOP)
                .with(WATERLOGGED, false)
        );
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        // Technically not needed I guess, as the player cannot place a rope in water
        return getDefaultState().with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (isTopSegment(state)) {
            if (!state.isOf(newState.getBlock())) {
                BlockPos blockPosAbove = pos.up();
                BlockState blockStateAbove = world.getBlockState(blockPosAbove);
                if (blockStateAbove.isOf(Blocks.TRIPWIRE_HOOK)) {
                    TripwireHookBlockHelper.setPowered(world, blockPosAbove, false);
                    TripwireHookBlockHelper.playDetachSound(world, blockPosAbove);
                    // TODO: Necessary to call world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, blockState))?
                }
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST -> EAST_SHAPES[state.get(CLIMBING_ROPE_SEGMENT).ordinal()];
            case WEST -> WEST_SHAPES[state.get(CLIMBING_ROPE_SEGMENT).ordinal()];
            case SOUTH -> SOUTH_SHAPES[state.get(CLIMBING_ROPE_SEGMENT).ordinal()];
            default -> NORTH_SHAPES[state.get(CLIMBING_ROPE_SEGMENT).ordinal()];
        };
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (isAttached(world, pos)) {
            unrollBeneath(state, world, pos);
        } else {
            world.breakBlock(pos, true);
        }
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Prevent calls of scheduledTick()
        //super.randomTick(state, world, pos, random);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        if (direction == Direction.UP) {
            if (!isAttached(world, pos)) {
                tickView.scheduleBlockTick(pos, this, 1);
            }
        } else if (direction == Direction.DOWN) {
            if (!neighborState.isOf(this)) {
                if (isTopSegment(state))
                    return state.with(CLIMBING_ROPE_SEGMENT, ClimbingRopeSegment.TOP_BOTTOM);
                else // MIDDLE or BOTTOM
                    return state.with(CLIMBING_ROPE_SEGMENT, ClimbingRopeSegment.BOTTOM);
            }
        }

        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(this)) {
            // New rope placed
            if (canUnrollBeneath(world, pos)) {
                /*
                 * When a rope is placed on a tripwire hook, it will unroll, meaning in the next scheduled tick, it will try to place
                 * another rope beneath itself if possible. The newly placed block will then again schedule a tick to unroll, and so on.
                 */
                world.scheduleBlockTick(pos, this, 2);
            }
        }

        super.onBlockAdded(state, world, pos, oldState, notify);
    }

    @Override
    public BlockPos getDropPos(BlockState state, World world, BlockPos originalPos, Entity entity) {
        // Try to drop the hook one block above to help the player collect it, if there is air.
        // Do this also if the rope is broken by the world, as that can happen when the player breaks the tripwire hook.
        if (isTopSegment(state)) {
            BlockPos newPos = originalPos.up().up();

            if (world.isAir(newPos))
                return newPos;
        }

        return originalPos;
    }

    protected boolean canUnrollBeneath(World world, BlockPos pos) {
        return world.isAir(pos.down());
    }

    protected boolean isTopSegment(BlockState state) {
        return (state.get(CLIMBING_ROPE_SEGMENT) == ClimbingRopeSegment.TOP || state.get(CLIMBING_ROPE_SEGMENT) == ClimbingRopeSegment.TOP_BOTTOM);
    }

    protected boolean isAttached(WorldView world, BlockPos pos) {
        BlockState blockStateAbove = world.getBlockState(pos.up());

        if (blockStateAbove.isOf(this))
            return true;

        return (blockStateAbove.isOf(Blocks.TRIPWIRE_HOOK) && blockStateAbove.get(TripwireHookBlock.POWERED));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, CLIMBING_ROPE_SEGMENT, WATERLOGGED);
    }

    protected void unrollBeneath(BlockState state, ServerWorld world, BlockPos pos) {
        Direction facing = state.getOrEmpty(Properties.HORIZONTAL_FACING).orElse(Direction.NORTH);

        BlockPos posBeneath = pos.down();

        if (!world.isAir(posBeneath))
            return;

        ClimbingRopeSegment currentSegment = state.get(CLIMBING_ROPE_SEGMENT);
        ClimbingRopeSegment nextSegment = ClimbingRopeSegment.BOTTOM;

        if (currentSegment == ClimbingRopeSegment.TOP_BOTTOM) {
            currentSegment = ClimbingRopeSegment.TOP;
        } else {
            currentSegment = ClimbingRopeSegment.MIDDLE;
        }

        world.setBlockState(pos, state.with(ClimbingRopeBlock.CLIMBING_ROPE_SEGMENT, currentSegment), Block.NOTIFY_ALL);

        world.setBlockState(posBeneath, state.with(ClimbingRopeBlock.FACING, facing).with(ClimbingRopeBlock.CLIMBING_ROPE_SEGMENT, nextSegment), Block.NOTIFY_ALL);
    }
}
