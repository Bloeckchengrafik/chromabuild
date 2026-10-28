package dev.chromabuild.neoforge;

import com.mojang.brigadier.Command;
import dev.chromabuild.core.PlacementPlan;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Read-only comparison of plan assignments with ordinary player inventory.
 * This is NOT a survival cost resolver for block entities, components, slabs,
 * buckets, special placement rules, or remote storage.
 */
public final class InventoryProbe {
    private InventoryProbe() {}

    public static int report(CommandSourceStack source, PlacementPlan plan) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        Map<Item, Integer> required = new LinkedHashMap<>();
        int unsupported = 0;
        for (var assignment : plan.assignments()) {
            ResourceLocation id = ResourceLocation.tryParse(assignment.blockStateId());
            if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                unsupported++;
                continue;
            }
            Item item = BuiltInRegistries.BLOCK.get(id).asItem();
            if (item == Items.AIR) {
                unsupported++;
                continue;
            }
            required.merge(item, 1, Integer::sum);
        }
        int missing = 0;
        StringBuilder summary = new StringBuilder("ChromaBuild inventory quote (read-only): ");
        for (var requirement : required.entrySet()) {
            int count = 0;
            for (var stack : player.getInventory().items)
                if (stack.is(requirement.getKey())) count += stack.getCount();
            int deficit = Math.max(0, requirement.getValue() - count);
            missing += deficit;
            summary.append(BuiltInRegistries.ITEM.getKey(requirement.getKey()))
                .append(" ").append(count).append("/").append(requirement.getValue())
                .append(" (missing ").append(deficit).append("); ");
        }
        summary.append("unresolved placements: ").append(unsupported)
            .append("; missing items: ").append(missing)
            .append(". No items consumed or blocks placed.");
        source.sendSuccess(() -> Component.literal(summary.toString()), false);
        return Command.SINGLE_SUCCESS;
    }
}
