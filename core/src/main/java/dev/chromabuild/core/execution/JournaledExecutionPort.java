package dev.chromabuild.core.execution;

import dev.chromabuild.core.PlacementPlan;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.UUID;

/**
 * Durable intent markers around side effects. Only safe when the delegate's
 * place(false) contract means that no mutation occurred. Intended for a
 * server-thread adapter with per-step recovery metadata, not yet enabled.
 */
public final class JournaledExecutionPort implements ExecutionPort {
    private final ExecutionPort delegate;
    private final FileRecoveryJournal journal;
    private final UUID transactionId;
    private final int expected;
    private int step;
    private boolean finished;

    public JournaledExecutionPort(ExecutionPort delegate, FileRecoveryJournal journal,
                                  UUID transactionId, int expected) throws IOException {
        this.delegate = Objects.requireNonNull(delegate);
        this.journal = Objects.requireNonNull(journal);
        this.transactionId = Objects.requireNonNull(transactionId);
        if (expected < 1) throw new IllegalArgumentException("empty transaction");
        this.expected = expected;
        journal.append(new JournalEvent(transactionId, 0, JournalEvent.Phase.BEGIN));
    }

    private void record(JournalEvent.Phase phase) {
        try {
            journal.append(new JournalEvent(transactionId, step, phase));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override public boolean canPlace(PlacementPlan.Assignment assignment) {
        return delegate.canPlace(assignment);
    }

    @Override public boolean debit(PlacementPlan.Assignment assignment) {
        record(JournalEvent.Phase.DEBIT_INTENT);
        boolean result = delegate.debit(assignment);
        if (result) record(JournalEvent.Phase.DEBIT_DONE);
        return result;
    }

    @Override public boolean place(PlacementPlan.Assignment assignment) {
        record(JournalEvent.Phase.PLACE_INTENT);
        boolean result = delegate.place(assignment);
        if (result) {
            record(JournalEvent.Phase.PLACE_DONE);
            step++;
        }
        return result;
    }

    @Override public boolean refund(PlacementPlan.Assignment assignment) {
        record(JournalEvent.Phase.REFUND_INTENT);
        boolean result = delegate.refund(assignment);
        if (result) record(JournalEvent.Phase.REFUND_DONE);
        return result;
    }

    /** Only call after the entire job completed, never on a stopped job. */
    public void markComplete() {
        if (finished || step != expected) throw new IllegalStateException("transaction not fully placed");
        record(JournalEvent.Phase.COMPLETE);
        finished = true;
    }
}
