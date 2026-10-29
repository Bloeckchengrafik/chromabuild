package dev.chromabuild.core;

import dev.chromabuild.core.execution.MaterialBalance;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaterialBalanceTest {
    private static PlacementPlan plan(String... blocks) {
        var list = new ArrayList<PlacementPlan.Assignment>();
        for (int i = 0; i < blocks.length; i++)
            list.add(new PlacementPlan.Assignment(new BlockPosition(i, 64, 0), blocks[i]));
        return new PlacementPlan(list);
    }

    @Test void combinesIdenticalItemsAndTracksShortages() {
        var result = MaterialBalance.quote(plan("minecraft:stone", "minecraft:calcite",
            "minecraft:stone"), Map.of("minecraft:stone", 1, "minecraft:calcite", 3));
        assertEquals(2, result.required().get("minecraft:stone"));
        assertEquals(Map.of("minecraft:stone", 1), result.missing());
        assertFalse(result.ready());
    }

    @Test void unknownMaterialsCanNeverPassAsFree() {
        var result = MaterialBalance.quote(plan("minecraft:chest", "minecraft:stone"),
            Map.of("minecraft:stone", 1));
        assertEquals(1, result.unsupported());
        assertFalse(result.ready());
    }

    @Test void exactStockIsSufficientAndStockIsNotConsumed() {
        var stock = Map.of("minecraft:stone", 2);
        var result = MaterialBalance.quote(plan("minecraft:stone", "minecraft:stone"), stock);
        assertTrue(result.ready());
        assertEquals(2, stock.get("minecraft:stone"));
    }

    @Test void refusesNegativeStock() {
        assertThrows(IllegalArgumentException.class,
            () -> MaterialBalance.quote(plan("minecraft:stone"), Map.of("minecraft:stone", -1)));
    }
}
