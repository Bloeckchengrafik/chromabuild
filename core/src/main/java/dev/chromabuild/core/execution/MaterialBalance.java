package dev.chromabuild.core.execution;

import dev.chromabuild.core.PlacementPlan;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Pure, read-only material balance for simple experimental block placements.
 * Unknown blocks fail closed; no inventory mutation is performed.
 */
public final class MaterialBalance {
    private MaterialBalance() {}

    public record Result(Map<String, Integer> required, Map<String, Integer> missing,
                         int unsupported) {
        public Result {
            required = Map.copyOf(required);
            missing = Map.copyOf(missing);
        }
        public boolean ready() { return unsupported == 0 && missing.isEmpty(); }
    }

    public static Result quote(PlacementPlan plan, Map<String, Integer> available) {
        Objects.requireNonNull(plan);
        Objects.requireNonNull(available);
        var required = new LinkedHashMap<String, Integer>();
        int unsupported = 0;
        for (var assignment : plan.assignments()) {
            if (!MaterialEligibility.supports(assignment.blockStateId())) {
                unsupported++;
            } else {
                required.merge(assignment.blockStateId(), 1, Math::addExact);
            }
        }
        var missing = new LinkedHashMap<String, Integer>();
        for (var entry : required.entrySet()) {
            int stock = available.getOrDefault(entry.getKey(), 0);
            if (stock < 0) throw new IllegalArgumentException("negative inventory count");
            if (stock < entry.getValue())
                missing.put(entry.getKey(), entry.getValue() - stock);
        }
        return new Result(required, missing, unsupported);
    }
}
