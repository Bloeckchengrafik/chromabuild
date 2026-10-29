package dev.chromabuild.core.execution;

import dev.chromabuild.core.PlacementPlan;
import java.util.HashSet;
import java.util.Objects;

/**
 * Pure gate for a prospective placement plan. No world edits or withdrawal.
 * Minecraft-side adapter must separately verify reach, claims, replaceability,
 * inventory component identity and transaction safety.
 */
public final class PlacementPreflight {
    private PlacementPreflight() {}
    public record Result(boolean approved, String reason, int positions) {}

    public static Result check(PlacementPlan plan, int maxPositions) {
        Objects.requireNonNull(plan);
        if (maxPositions < 1) throw new IllegalArgumentException("invalid maxPositions");
        int size = plan.assignments().size();
        if (size == 0) return new Result(false, "empty plan", 0);
        if (size > maxPositions) return new Result(false, "selection exceeds configured cap", size);
        var seen = new HashSet<>();
        for (var assignment : plan.assignments()) {
            if (assignment == null || assignment.position() == null) return new Result(false, "invalid assignment", size);
            if (!seen.add(assignment.position())) return new Result(false, "duplicate target position", size);
            if (!MaterialEligibility.supports(assignment.blockStateId()))
                return new Result(false, "unsupported material: " + assignment.blockStateId(), size);
        }
        return new Result(true, "eligible for dynamic server checks (not permission to place)", size);
    }
}
