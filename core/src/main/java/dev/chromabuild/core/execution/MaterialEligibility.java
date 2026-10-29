package dev.chromabuild.core.execution;

import dev.chromabuild.core.PlacementPlan;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Explicit experimental support boundary. Unknown blocks are not presumed to
 * cost one item or to be safe to place. Not a production placement authorization.
 */
public final class MaterialEligibility {
    private static final Set<String> SIMPLE_BLOCKS =
        Set.of("minecraft:stone", "minecraft:calcite");
    private MaterialEligibility() {}

    public record Assessment(int supported, List<String> unsupported) {
        public Assessment {
            unsupported = List.copyOf(unsupported);
        }
        public boolean fullySupported() { return unsupported.isEmpty(); }
    }

    public static Assessment assess(PlacementPlan plan) {
        int supported = 0;
        var unsupported = new ArrayList<String>();
        for (var assignment : plan.assignments()) {
            if (SIMPLE_BLOCKS.contains(assignment.blockStateId())) {
                supported++;
            } else {
                unsupported.add(assignment.blockStateId());
            }
        }
        return new Assessment(supported, unsupported);
    }

    public static boolean supports(String blockStateId) {
        return SIMPLE_BLOCKS.contains(blockStateId);
    }
}
