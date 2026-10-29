package dev.chromabuild.core.session;

import dev.chromabuild.core.PlacementPlan;
import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Server-side ephemeral session store. Only an exact approved snapshot may be
 * consumed. Caller must still validate tool, reach, claims, world state and material costs.
 * This class assumes single-threaded access from the server main thread.
 */
public final class PlanSessions {
    private final Map<UUID, PlanSession> sessions = new HashMap<>();
    private final Map<UUID, Long> revisions = new HashMap<>();
    private final Clock clock;
    private final Duration ttl;
    private final int maxSessions;

    public PlanSessions(Clock clock, Duration ttl, int maxSessions) {
        this.clock = Objects.requireNonNull(clock);
        this.ttl = Objects.requireNonNull(ttl);
        if (ttl.isZero() || ttl.isNegative() || maxSessions < 1)
            throw new IllegalArgumentException("invalid session limits");
        this.maxSessions = maxSessions;
    }

    /** Replaces the owner's prior preview; revision increases monotonically. */
    public PlanSession publish(UUID owner, String dimension, PlacementPlan plan) {
        Objects.requireNonNull(owner); Objects.requireNonNull(plan);
        if (dimension == null || dimension.isBlank() || plan.assignments().isEmpty())
            throw new IllegalArgumentException("invalid preview");
        sessions.values().removeIf(s -> s.owner().equals(owner) || !clock.instant().isBefore(s.expiresAt()));
        if (sessions.size() >= maxSessions) throw new IllegalStateException("preview capacity reached");
        long revision = revisions.merge(owner, 1L, (oldValue, increment) -> Math.addExact(oldValue, increment));
        PlanSession session = new PlanSession(UUID.randomUUID(), owner, dimension, revision,
            clock.instant().plus(ttl), PlanFingerprint.of(plan), plan);
        sessions.put(session.id(), session);
        return session;
    }

    /**
     * Removes an approved plan so retries cannot execute it twice.
     * The calling server must first perform all dynamic authorization and resource checks.
     * A rejected confirmation leaves its valid preview intact.
     */
    public PlanSession consume(UUID requester, String dimension, UUID sessionId,
                               long revision, String fingerprint) {
        Objects.requireNonNull(requester); Objects.requireNonNull(sessionId);
        PlanSession session = sessions.get(sessionId);
        if (session == null || !session.owner().equals(requester) ||
            !session.dimension().equals(dimension) || session.revision() != revision ||
            !session.fingerprint().equals(fingerprint) ||
            !clock.instant().isBefore(session.expiresAt())) {
            throw new IllegalArgumentException("unknown, stale or unauthorized preview");
        }
        sessions.remove(sessionId);
        return session;
    }

    /** Read the current owner's approved preview without consuming or changing it. */
    public PlanSession current(UUID owner, String dimension) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(dimension);
        for (var session : sessions.values()) {
            if (session.owner().equals(owner) && session.dimension().equals(dimension)
                    && clock.instant().isBefore(session.expiresAt())) {
                return session;
            }
        }
        return null;
    }

    public void invalidate(UUID owner) {
        sessions.values().removeIf(s -> s.owner().equals(owner));
    }
}
