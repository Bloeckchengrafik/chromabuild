package dev.chromabuild.core.session;

import dev.chromabuild.core.PlacementPlan;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Server-owned immutable preview snapshot, not world-edit authorization. */
public record PlanSession(UUID id, UUID owner, String dimension, long revision,
                          Instant expiresAt, String fingerprint, PlacementPlan plan) {
    public PlanSession {
        Objects.requireNonNull(id); Objects.requireNonNull(owner);
        Objects.requireNonNull(dimension); Objects.requireNonNull(expiresAt);
        Objects.requireNonNull(fingerprint); Objects.requireNonNull(plan);
        if (dimension.isBlank() || revision < 1 || plan.assignments().isEmpty())
            throw new IllegalArgumentException("invalid plan session");
    }
}
