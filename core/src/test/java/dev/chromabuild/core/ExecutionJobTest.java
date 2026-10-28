package dev.chromabuild.core;

import dev.chromabuild.core.execution.ExecutionJob;
import dev.chromabuild.core.execution.ExecutionPort;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExecutionJobTest {
    static PlacementPlan plan(int count) {
        var entries = new ArrayList<PlacementPlan.Assignment>();
        for (int i = 0; i < count; i++)
            entries.add(new PlacementPlan.Assignment(new BlockPosition(i, 64, 0), "minecraft:stone"));
        return new PlacementPlan(entries);
    }

    static class Port implements ExecutionPort {
        int validated, debited, placed, refunds;
        int failAt = -1;
        boolean refundOk = true;
        @Override public boolean canPlace(PlacementPlan.Assignment a) { validated++; return true; }
        @Override public boolean debit(PlacementPlan.Assignment a) { debited++; return true; }
        @Override public boolean place(PlacementPlan.Assignment a) {
            if (a.position().x() == failAt) return false;
            placed++; return true;
        }
        @Override public boolean refund(PlacementPlan.Assignment a) { refunds++; return refundOk; }
    }

    @Test void respectsTickBudgetAndCompletes() {
        var port = new Port();
        var job = new ExecutionJob(plan(5), port);
        assertEquals(2, job.tick(2));
        assertEquals(2, job.placed());
        assertEquals(ExecutionJob.State.RUNNING, job.state());
        assertEquals(2, job.tick(2));
        assertEquals(1, job.tick(2));
        assertEquals(ExecutionJob.State.COMPLETED, job.state());
        assertEquals(5, port.debited);
        assertEquals(5, port.placed);
        assertEquals(0, job.tick(2));
    }

    @Test void failedPlacementRefundsAndNeverRetries() {
        var port = new Port(); port.failAt = 1;
        var job = new ExecutionJob(plan(3), port);
        assertEquals(2, job.tick(10));
        assertEquals(ExecutionJob.State.STOPPED, job.state());
        assertEquals(1, job.placed());
        assertEquals(2, port.debited);
        assertEquals(1, port.refunds);
        assertEquals(0, job.tick(10));
    }

    @Test void refundFailureQuarantines() {
        var port = new Port(); port.failAt = 0; port.refundOk = false;
        var job = new ExecutionJob(plan(2), port);
        assertEquals(1, job.tick(10));
        assertEquals(ExecutionJob.State.NEEDS_RECOVERY, job.state());
        assertEquals(0, job.placed());
        assertEquals(0, job.tick(10));
    }

    @Test void validationFailureNeverDebitsOrPlaces() {
        var port = new Port() {
            @Override public boolean canPlace(PlacementPlan.Assignment a) { return false; }
        };
        var job = new ExecutionJob(plan(2), port);
        assertEquals(1, job.tick(5));
        assertEquals(0, port.debited);
        assertEquals(0, port.placed);
        assertEquals(ExecutionJob.State.STOPPED, job.state());
    }

    @Test void rejectsInvalidBudget() {
        var job = new ExecutionJob(plan(1), new Port());
        assertThrows(IllegalArgumentException.class, () -> job.tick(0));
    }
}
