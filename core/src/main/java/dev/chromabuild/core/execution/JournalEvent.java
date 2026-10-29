package dev.chromabuild.core.execution;

import java.util.Objects;
import java.util.UUID;

/** Append-only transition marker. A recorded intent is NOT proof of a completed side effect. */
public record JournalEvent(UUID transactionId, int step, Phase phase) {
    public enum Phase { BEGIN, DEBIT_INTENT, DEBIT_DONE, PLACE_INTENT, PLACE_DONE,
                        REFUND_INTENT, REFUND_DONE, COMPLETE }
    public JournalEvent {
        Objects.requireNonNull(transactionId);
        Objects.requireNonNull(phase);
        if (step < 0) throw new IllegalArgumentException("negative step");
    }
}
