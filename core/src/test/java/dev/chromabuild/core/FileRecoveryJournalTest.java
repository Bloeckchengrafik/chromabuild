package dev.chromabuild.core;

import dev.chromabuild.core.execution.FileRecoveryJournal;
import dev.chromabuild.core.execution.JournalEvent;
import dev.chromabuild.core.execution.JournalRecovery;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class FileRecoveryJournalTest {
    @TempDir Path dir;

    @Test void readsBackRecordsAndQuarantinesUnfinishedTransactions() throws Exception {
        var journal = new FileRecoveryJournal(dir.resolve("jobs.log"));
        UUID done = UUID.randomUUID(), uncertain = UUID.randomUUID();
        var start = new JournalEvent(done, 0, JournalEvent.Phase.BEGIN);
        journal.append(start);
        journal.append(new JournalEvent(done, 0, JournalEvent.Phase.COMPLETE));
        journal.append(new JournalEvent(uncertain, 0, JournalEvent.Phase.BEGIN));
        journal.append(new JournalEvent(uncertain, 0, JournalEvent.Phase.DEBIT_INTENT));
        assertEquals(4, journal.readAll().size());
        var unresolved = JournalRecovery.unresolved(journal.readAll());
        assertFalse(unresolved.containsKey(done));
        assertTrue(unresolved.containsKey(uncertain));
        assertEquals(JournalEvent.Phase.DEBIT_INTENT,
            unresolved.get(uncertain).getLast().phase());
    }

    @Test void rejectsTruncatedAndTamperedJournal() throws Exception {
        Path path = dir.resolve("broken.log");
        var journal = new FileRecoveryJournal(path);
        journal.append(new JournalEvent(UUID.randomUUID(), 0, JournalEvent.Phase.BEGIN));
        String original = Files.readString(path);
        Files.writeString(path, original.replace("BEGIN", "PLACE"));
        assertThrows(java.io.IOException.class, journal::readAll);
        Files.writeString(path, original.substring(0, original.length() - 1));
        assertThrows(java.io.IOException.class, journal::readAll);
    }

    @Test void emptyJournalIsSafe() throws Exception {
        assertEquals(List.of(), new FileRecoveryJournal(dir.resolve("missing.log")).readAll());
    }
}
