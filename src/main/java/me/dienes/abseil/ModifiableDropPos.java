package me.dienes.abseil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ModifiableDropPos {
    /**
     * Allows blocks to modify the position at which they drop their items when broken either by an entity or by the world.
     * The default implementation returns the original position.
     *
     * The Mixins for {@link net.minecraft.block.Block} and {@link net.minecraft.world.World} call this method.
     *
     * @param state The state of the block that is being broken.
     * @param world The world in which the block is located.
     * @param originalPos The original position of the block that is being broken.
     * @param entity The entity that is breaking the block. This can be null if the block is being broken by the world.
     * @return The position at which the block should drop its items when broken.
     */
    BlockPos getDropPos(BlockState state, World world, BlockPos originalPos, Entity entity);
}
