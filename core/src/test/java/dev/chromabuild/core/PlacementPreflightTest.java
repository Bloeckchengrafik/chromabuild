package dev.chromabuild.core;

import dev.chromabuild.core.execution.PlacementPreflight;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlacementPreflightTest {
    private static PlacementPlan plan(String... states) {
        var assignments = new java.util.ArrayList<PlacementPlan.Assignment>();
        for (int i = 0; i < states.length; i++)
            assignments.add(new PlacementPlan.Assignment(new BlockPosition(i, 64, 0), states[i]));
        return new PlacementPlan(assignments);
    }
    @Test void acceptsSimpleUniquePlanWithinCap() {
        assertTrue(PlacementPreflight.check(plan("minecraft:stone", "minecraft:calcite"), 2).approved());
    }
    @Test void rejectsUnsupportedAndOversized() {
        assertFalse(PlacementPreflight.check(plan("minecraft:chest"), 4).approved());
        assertFalse(PlacementPreflight.check(plan("minecraft:stone", "minecraft:stone"), 1).approved());
    }
    @Test void rejectsDuplicateTargets() {
        var position = new BlockPosition(0, 64, 0);
        var duplicate = new PlacementPlan(List.of(
            new PlacementPlan.Assignment(position, "minecraft:stone"),
            new PlacementPlan.Assignment(position, "minecraft:calcite")));
        assertEquals("duplicate target position", PlacementPreflight.check(duplicate, 10).reason());
    }
}
