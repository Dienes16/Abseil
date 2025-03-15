package me.dienes.abseil;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class AbseilItems {
    public static final ClimbingRopeItem CLIMBING_ROPE_ITEM = (ClimbingRopeItem) registerItem(
            "climbing_rope", ClimbingRopeItem::new, new Item.Settings()
                    .maxCount(16)
    );

    public static Item registerItem(String name, Function<Item.Settings, Item> itemFactory, Item.Settings settings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Abseil.MOD_ID, name));

        Item item = itemFactory.apply(settings.registryKey(itemKey));

        Registry.register(Registries.ITEM, itemKey, item);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> content.add(CLIMBING_ROPE_ITEM));

        return item;
    }

    public static void initialize() {
        // Empty
    }
}
