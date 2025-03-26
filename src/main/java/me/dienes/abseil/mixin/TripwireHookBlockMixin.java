package me.dienes.abseil.mixin;

import me.dienes.abseil.AbseilBlocks;
import me.dienes.abseil.ModifiableDropPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TripwireHookBlock.class)
public abstract class TripwireHookBlockMixin extends Block implements ModifiableDropPos {
    public TripwireHookBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public BlockPos getDropPos(BlockState state, World world, BlockPos originalPos, Entity entity) {
        // If the player breaks a tripwire hook with a climbing rope attached, try to drop the hook one block above to
        // help the player collect it, if there is air.
        if (world.getBlockState(originalPos.down()).isOf(AbseilBlocks.CLIMBING_ROPE_BLOCK)) {
            if (entity != null && entity.isPlayer()) {
                BlockPos newPos = originalPos.up();

                if (world.isAir(newPos))
                    return newPos;
            }
        }

        return originalPos;
    }
}
