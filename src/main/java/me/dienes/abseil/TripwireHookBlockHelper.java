package me.dienes.abseil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.minecraft.block.TripwireHookBlock.FACING;

public class TripwireHookBlockHelper {
    public static void setPowered(World world, BlockPos pos, boolean powered) {
        BlockState currentState = world.getBlockState(pos);

        if (!currentState.isOf(Blocks.TRIPWIRE_HOOK))
            return;

        BlockState newState = currentState.with(TripwireHookBlock.POWERED, powered);

        world.setBlockState(pos, newState, Block.NOTIFY_ALL);

        world.updateNeighborsAlways(pos, newState.getBlock(), null);

        // Second update necessary for strong redstone power
        world.updateNeighborsAlways(pos.offset(newState.get(FACING).getOpposite()), newState.getBlock(), null);
    }

    public static void playAttachSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ENTITY_LEASH_KNOT_PLACE, SoundCategory.BLOCKS, 0.4f, 0.6f);
    }

    public static void playDetachSound(World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ENTITY_LEASH_KNOT_BREAK, SoundCategory.BLOCKS, 0.4f, 0.5f);
    }
}
