package me.dienes.abseil;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class AbseilBlocks {
    public static final ClimbingRopeBlock CLIMBING_ROPE_BLOCK = (ClimbingRopeBlock) registerBlock(
        "climbing_rope", ClimbingRopeBlock::new, AbstractBlock.Settings.create()
            .nonOpaque()
            .noCollision()
            .burnable()
            .sounds(AbseilBlockSoundGroups.CLIMBING_ROPE_BLOCK)
    );

    public static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings settings) {
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Abseil.MOD_ID, name));

        Block block = blockFactory.apply(settings.registryKey(blockKey));

        return Registry.register(Registries.BLOCK, blockKey, block);
    }

    public static void initialize() {
        FlammableBlockRegistry.getDefaultInstance().add(CLIMBING_ROPE_BLOCK, 30, 60); // Values taken from wool
    }
}
