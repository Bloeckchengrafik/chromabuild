package dev.chromabuild.core;

import java.util.List;

/** Immutable computed assignments, not an authorization to change the world. */
public record PlacementPlan(List<Assignment> assignments) {
    public PlacementPlan { assignments = List.copyOf(assignments); }
    public record Assignment(BlockPosition position, String blockStateId) { }
}
