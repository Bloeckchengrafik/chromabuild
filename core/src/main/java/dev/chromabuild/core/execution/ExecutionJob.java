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
            if (!port.canPlace(assignment)) {
                stop("validation rejected");
            } else if (!port.debit(assignment)) {
                stop("material unavailable");
            } else {
                try {
                    if (!port.place(assignment)) {
                        recover(assignment, "placement rejected");
                    } else {
                        placed++;
                        cursor++;
                        if (cursor == plan.assignments().size()) state = State.COMPLETED;
                    }
                } catch (RuntimeException failure) {
                    recover(assignment, "placement threw: " + failure.getClass().getSimpleName());
                }
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
