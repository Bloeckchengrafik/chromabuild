package dev.chromabuild.core.execution;

import dev.chromabuild.core.PlacementPlan;
import java.util.Objects;

/**
 * Single-threaded bounded state machine. A failed refund is not ignored:
 * the operation is quarantined for administrative recovery.
 *
 * This is a coordinator, not a durable journal or production Minecraft executor.
 */
public final class ExecutionJob {
    public enum State { RUNNING, COMPLETED, STOPPED, NEEDS_RECOVERY }

    private final PlacementPlan plan;
    private final ExecutionPort port;
    private int cursor;
    private int placed;
    private State state;
    private String reason = "";

    public ExecutionJob(PlacementPlan plan, ExecutionPort port) {
        this.plan = Objects.requireNonNull(plan);
        this.port = Objects.requireNonNull(port);
        this.state = plan.assignments().isEmpty() ? State.COMPLETED : State.RUNNING;
    }

    /**
     * Attempt at most budget assignments. Never automatically retry a failed step.
     * Caller is responsible for restricting invocations to the server thread.
     */
    public int tick(int budget) {
        if (budget < 1) throw new IllegalArgumentException("budget must be positive");
        if (state != State.RUNNING) return 0;
        int attempts = 0;
        while (attempts < budget && state == State.RUNNING) {
            var assignment = plan.assignments().get(cursor);
            attempts++;
            boolean allowed;
            try {
                allowed = port.canPlace(assignment);
            } catch (RuntimeException failure) {
                quarantine("validation threw: " + failure.getClass().getSimpleName());
                continue;
            }
            if (!allowed) {
                stop("validation rejected");
                continue;
            }

            boolean debited;
            try {
                debited = port.debit(assignment);
            } catch (RuntimeException failure) {
                // Debit may have extracted materials before throwing.
                // Never retry or guess that no material was withdrawn.
                quarantine("debit outcome unknown: " + failure.getClass().getSimpleName());
                continue;
            }
            if (!debited) {
                stop("material unavailable");
                continue;
            }

            final boolean placedSuccessfully;
            try {
                placedSuccessfully = port.place(assignment);
            } catch (RuntimeException failure) {
                // World edits may have happened before the exception. Refunding
                // could duplicate the item, so require explicit reconciliation.
                quarantine("placement outcome unknown: " + failure.getClass().getSimpleName());
                continue;
            }
            if (!placedSuccessfully) {
                recover(assignment, "placement rejected");
            } else {
                placed++;
                cursor++;
                if (cursor == plan.assignments().size()) state = State.COMPLETED;
            }
        }
        return attempts;
    }

    private void recover(PlacementPlan.Assignment assignment, String why) {
        try {
            if (port.refund(assignment)) stop(why);
            else quarantine(why + "; refund failed");
        } catch (RuntimeException failure) {
            quarantine(why + "; refund threw " + failure.getClass().getSimpleName());
        }
    }

    private void stop(String why) { state = State.STOPPED; reason = why; }
    private void quarantine(String why) { state = State.NEEDS_RECOVERY; reason = why; }

    public State state() { return state; }
    public int placed() { return placed; }
    public int attempted() { return cursor + (state == State.COMPLETED ? 0 : state == State.RUNNING ? 0 : 1); }
    public String reason() { return reason; }
}
