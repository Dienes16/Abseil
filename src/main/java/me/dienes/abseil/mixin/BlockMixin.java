package me.dienes.abseil.mixin;

import me.dienes.abseil.ModifiableDropPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.minecraft.block.Block.dropStacks;

@Mixin(Block.class)
public abstract class BlockMixin implements ModifiableDropPos {
    @Override
    public BlockPos getDropPos(BlockState state, World world, BlockPos originalPos, Entity entity) {
        return originalPos;
    }

    @Redirect(method = "afterBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V"))
    private void modifyDropPos(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool) {
        BlockPos modifiedDropPos = getDropPos(state, world, pos, entity);
        dropStacks(state, world, modifiedDropPos, blockEntity, entity, tool);
    }
}
