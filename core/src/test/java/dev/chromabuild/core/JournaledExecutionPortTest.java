package dev.chromabuild.core;

import dev.chromabuild.core.execution.*;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class JournaledExecutionPortTest {
    @TempDir Path dir;
    private static PlacementPlan plan() {
        return new PlacementPlan(List.of(new PlacementPlan.Assignment(
            new BlockPosition(0, 64, 0), "minecraft:stone")));
    }
    @Test void successfulStepProducesDurableHistory() throws Exception {
        var journal = new FileRecoveryJournal(dir.resolve("successful.log"));
        UUID id = UUID.randomUUID();
        ExecutionPort success = new ExecutionPort() {
            public boolean canPlace(PlacementPlan.Assignment a) { return true; }
            public boolean debit(PlacementPlan.Assignment a) { return true; }
            public boolean place(PlacementPlan.Assignment a) { return true; }
            public boolean refund(PlacementPlan.Assignment a) { return false; }
        };
        var port = new JournaledExecutionPort(success, journal, id, 1);
        var job = new ExecutionJob(plan(), port);
        job.tick(1);
        assertEquals(ExecutionJob.State.COMPLETED, job.state());
        port.markComplete();
        assertEquals(List.of(JournalEvent.Phase.BEGIN, JournalEvent.Phase.DEBIT_INTENT,
                JournalEvent.Phase.DEBIT_DONE, JournalEvent.Phase.PLACE_INTENT,
                JournalEvent.Phase.PLACE_DONE, JournalEvent.Phase.COMPLETE),
            journal.readAll().stream().map(JournalEvent::phase).toList());
        assertTrue(JournalRecovery.unresolved(journal.readAll()).isEmpty());
    }
    @Test void ambiguousWorldErrorIsLeftUnresolved() throws Exception {
        var journal = new FileRecoveryJournal(dir.resolve("uncertain.log"));
        ExecutionPort uncertain = new ExecutionPort() {
            public boolean canPlace(PlacementPlan.Assignment a) { return true; }
            public boolean debit(PlacementPlan.Assignment a) { return true; }
            public boolean place(PlacementPlan.Assignment a) { throw new IllegalStateException("after mutation"); }
            public boolean refund(PlacementPlan.Assignment a) { fail("must never guess refund"); return false; }
        };
        var port = new JournaledExecutionPort(uncertain, journal, UUID.randomUUID(), 1);
        var job = new ExecutionJob(plan(), port);
        job.tick(1);
        assertEquals(ExecutionJob.State.NEEDS_RECOVERY, job.state());
        assertThrows(IllegalStateException.class, port::markComplete);
        assertEquals(JournalEvent.Phase.PLACE_INTENT, journal.readAll().getLast().phase());
        assertEquals(1, JournalRecovery.unresolved(journal.readAll()).size());
    }
}
