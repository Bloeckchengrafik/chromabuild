package dev.chromabuild.core;

import dev.chromabuild.core.execution.ExecutionJob;
import dev.chromabuild.core.execution.ExecutionPort;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AmbiguousExecutionFailureTest {
    private static PlacementPlan plan() {
        return new PlacementPlan(List.of(new PlacementPlan.Assignment(
            new BlockPosition(0, 64, 0), "minecraft:stone")));
    }
    @Test void unknownDebitOutcomeQuarantinesWithoutPlacing() {
        int[] placed = {0};
        var port = new ExecutionPort() {
            public boolean canPlace(PlacementPlan.Assignment a) { return true; }
            public boolean debit(PlacementPlan.Assignment a) { throw new IllegalStateException("withdrawn then failed"); }
            public boolean place(PlacementPlan.Assignment a) { placed[0]++; return true; }
            public boolean refund(PlacementPlan.Assignment a) { fail("must not refund uncertain debit"); return false; }
        };
        var job = new ExecutionJob(plan(), port);
        assertEquals(1, job.tick(2));
        assertEquals(ExecutionJob.State.NEEDS_RECOVERY, job.state());
        assertEquals(0, placed[0]);
        assertEquals(0, job.tick(2));
    }
    @Test void unknownPlaceOutcomeNeverRefunds() {
        int[] refunds = {0};
        var port = new ExecutionPort() {
            public boolean canPlace(PlacementPlan.Assignment a) { return true; }
            public boolean debit(PlacementPlan.Assignment a) { return true; }
            public boolean place(PlacementPlan.Assignment a) { throw new IllegalStateException("world changed then failed"); }
            public boolean refund(PlacementPlan.Assignment a) { refunds[0]++; return true; }
        };
        var job = new ExecutionJob(plan(), port);
        assertEquals(1, job.tick(3));
        assertEquals(ExecutionJob.State.NEEDS_RECOVERY, job.state());
        assertEquals(0, refunds[0]);
        assertEquals(0, job.tick(3));
    }
    @Test void validationExceptionQuarantinesWithoutDebit() {
        var port = new ExecutionPort() {
            public boolean canPlace(PlacementPlan.Assignment a) { throw new IllegalArgumentException(); }
            public boolean debit(PlacementPlan.Assignment a) { fail("must not debit"); return false; }
            public boolean place(PlacementPlan.Assignment a) { fail("must not place"); return false; }
            public boolean refund(PlacementPlan.Assignment a) { fail("must not refund"); return false; }
        };
        var job = new ExecutionJob(plan(), port);
        job.tick(1);
        assertEquals(ExecutionJob.State.NEEDS_RECOVERY, job.state());
    }
}
