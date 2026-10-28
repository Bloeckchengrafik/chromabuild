package dev.chromabuild.core;

import dev.chromabuild.core.session.PlanSessions;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlanSessionsTest {
    private static PlacementPlan plan(String state) {
        return new PlacementPlan(List.of(new PlacementPlan.Assignment(new BlockPosition(1, 64, 2), state)));
    }
    private static final class AdjustableClock extends Clock {
        private Instant instant = Instant.parse("2026-10-28T12:00:00Z");
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
        void advance(Duration by) { instant = instant.plus(by); }
    }

    @Test void exactSnapshotIsSingleUseAndOwnerBound() {
        var owner = UUID.randomUUID();
        var other = UUID.randomUUID();
        var store = new PlanSessions(new AdjustableClock(), Duration.ofSeconds(20), 10);
        var first = store.publish(owner, "minecraft:overworld", plan("minecraft:stone"));
        assertThrows(IllegalArgumentException.class, () -> store.consume(other,
            first.dimension(), first.id(), first.revision(), first.fingerprint()));
        assertThrows(IllegalArgumentException.class, () -> store.consume(owner,
            "minecraft:the_nether", first.id(), first.revision(), first.fingerprint()));
        assertThrows(IllegalArgumentException.class, () -> store.consume(owner,
            first.dimension(), first.id(), first.revision(), "forged"));
        assertEquals(first, store.consume(owner, first.dimension(), first.id(),
            first.revision(), first.fingerprint()));
        assertThrows(IllegalArgumentException.class, () -> store.consume(owner,
            first.dimension(), first.id(), first.revision(), first.fingerprint()));
    }

    @Test void refreshedPreviewInvalidatesPreviousRevision() {
        var owner = UUID.randomUUID();
        var store = new PlanSessions(new AdjustableClock(), Duration.ofMinutes(1), 4);
        var old = store.publish(owner, "minecraft:overworld", plan("minecraft:stone"));
        var current = store.publish(owner, "minecraft:overworld", plan("minecraft:calcite"));
        assertEquals(old.revision() + 1, current.revision());
        assertNotEquals(old.fingerprint(), current.fingerprint());
        assertThrows(IllegalArgumentException.class, () -> store.consume(owner,
            old.dimension(), old.id(), old.revision(), old.fingerprint()));
    }

    @Test void expiryRejectsConfirmationAndSnapshotsAreImmutable() {
        var owner = UUID.randomUUID();
        var clock = new AdjustableClock();
        var store = new PlanSessions(clock, Duration.ofSeconds(10), 2);
        var session = store.publish(owner, "minecraft:overworld", plan("minecraft:stone"));
        assertThrows(UnsupportedOperationException.class, () -> session.plan().assignments().clear());
        clock.advance(Duration.ofSeconds(11));
        assertThrows(IllegalArgumentException.class, () -> store.consume(owner,
            session.dimension(), session.id(), session.revision(), session.fingerprint()));
    }
}
