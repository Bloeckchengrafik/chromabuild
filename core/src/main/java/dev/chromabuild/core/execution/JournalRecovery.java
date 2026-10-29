package dev.chromabuild.core.execution;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Conservative startup scan. No automatic item refunds or world rollback:
 * every transaction lacking an explicit COMPLETE marker requires investigation.
 */
public final class JournalRecovery {
    private JournalRecovery() { }

    public static Map<UUID, List<JournalEvent>> unresolved(List<JournalEvent> history) {
        var grouped = new LinkedHashMap<UUID, java.util.ArrayList<JournalEvent>>();
        for (var event : history)
            grouped.computeIfAbsent(event.transactionId(), ignored -> new java.util.ArrayList<>()).add(event);
        var result = new LinkedHashMap<UUID, List<JournalEvent>>();
        for (var entry : grouped.entrySet()) {
            var events = entry.getValue();
            if (events.getFirst().phase() != JournalEvent.Phase.BEGIN ||
                events.getLast().phase() != JournalEvent.Phase.COMPLETE)
                result.put(entry.getKey(), List.copyOf(events));
        }
        return Map.copyOf(result);
    }
}
