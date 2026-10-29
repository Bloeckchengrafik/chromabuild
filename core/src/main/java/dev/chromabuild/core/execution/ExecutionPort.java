package dev.chromabuild.core.execution;

import dev.chromabuild.core.PlacementPlan;

/**
 * Server-thread adapter. Implementations must supply persistent recovery and actual
 * Minecraft/claim validation. A successful debit means the material is staged and
 * exclusively owned by this operation, not merely quoted.
 */
public interface ExecutionPort {
    /** Check current world, tool, reach, claims and selected target for this exact assignment. */
    boolean canPlace(PlacementPlan.Assignment assignment);
    /** Extract exactly one appropriate material into transaction custody, before world mutation. */
    boolean debit(PlacementPlan.Assignment assignment);
    /** Perform placement, returning true only if the world modification succeeded. */
    boolean place(PlacementPlan.Assignment assignment);
    /** Recover a debited material after an unsuccessful placement. Must be idempotent per operation step. */
    boolean refund(PlacementPlan.Assignment assignment);
}
