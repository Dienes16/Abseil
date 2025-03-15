package me.dienes.abseil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ClimbingRopeItem extends Item {
    public ClimbingRopeItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.isOf(Blocks.TRIPWIRE_HOOK)) {
            // Can only use with a tripwire hook that is not otherwise in use (tripwire)
            if (blockState.get(TripwireHookBlock.ATTACHED) || blockState.get(TripwireHookBlock.POWERED))
                return ActionResult.FAIL;
        }
        else if (blockState.isOf(AbseilBlocks.CLIMBING_ROPE_BLOCK)) {
            // Can only use with a bottom segment of another rope
            ClimbingRopeSegment segment = blockState.get(ClimbingRopeBlock.CLIMBING_ROPE_SEGMENT);
            if (segment != ClimbingRopeSegment.TOP_BOTTOM && segment != ClimbingRopeSegment.BOTTOM)
                return ActionResult.FAIL;
        }
        else {
            return ActionResult.PASS;
        }

        if (!world.isAir(blockPos.down()))
            return ActionResult.FAIL;

        if (!world.isClient) {
            if (blockState.isOf(Blocks.TRIPWIRE_HOOK)) {
                // Important to first set the tripwire hook to be powered, before placing the rope.
                // Otherwise, the placed rope will destroy itself again because the hook above is not powered.
                TripwireHookBlockHelper.setPowered(world, blockPos, true);
                TripwireHookBlockHelper.playAttachSound(world, blockPos);
            }

            attachBelowBlock(world, blockPos);

            PlayerEntity playerEntity = context.getPlayer();
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, blockState));

            ItemStack itemStack = context.getStack();
            itemStack.decrement(1);
        }

        return ActionResult.SUCCESS;
    }

    private void attachBelowBlock(World world, BlockPos blockPos) {
        Direction facing = world.getBlockState(blockPos).getOrEmpty(Properties.HORIZONTAL_FACING).orElse(Direction.NORTH);

        world.setBlockState(blockPos.down(), AbseilBlocks.CLIMBING_ROPE_BLOCK.getDefaultState().with(ClimbingRopeBlock.FACING, facing).with(ClimbingRopeBlock.CLIMBING_ROPE_SEGMENT, ClimbingRopeSegment.TOP_BOTTOM), Block.NOTIFY_ALL);
    }
}
