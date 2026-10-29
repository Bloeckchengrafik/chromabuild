package dev.chromabuild.core;

import dev.chromabuild.core.execution.MaterialEligibility;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaterialEligibilityTest {
    @Test void onlyExplicitExperimentalBlocksAreAccepted() {
        var plan = new PlacementPlan(List.of(
            new PlacementPlan.Assignment(new BlockPosition(0, 0, 0), "minecraft:stone"),
            new PlacementPlan.Assignment(new BlockPosition(1, 0, 0), "minecraft:calcite"),
            new PlacementPlan.Assignment(new BlockPosition(2, 0, 0), "minecraft:chest"),
            new PlacementPlan.Assignment(new BlockPosition(3, 0, 0), "modded:machine")
        ));
        var assessment = MaterialEligibility.assess(plan);
        assertEquals(2, assessment.supported());
        assertEquals(List.of("minecraft:chest", "modded:machine"), assessment.unsupported());
        assertFalse(assessment.fullySupported());
        assertTrue(MaterialEligibility.supports("minecraft:stone"));
        assertFalse(MaterialEligibility.supports("minecraft:stone[axis=y]"));
    }

    @Test void supportedSampleIsFullySupported() {
        var plan = new PlacementPlan(List.of(
            new PlacementPlan.Assignment(new BlockPosition(0, 0, 0), "minecraft:stone")
        ));
        assertTrue(MaterialEligibility.assess(plan).fullySupported());
    }
}
