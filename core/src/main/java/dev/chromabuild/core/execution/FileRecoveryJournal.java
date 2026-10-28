package dev.chromabuild.core.execution;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.CRC32;

/**
 * Synchronous append-only local journal. Intentionally fail-closed on corruption:
 * never infer that a debit/refund succeeded solely from a missing record.
 */
public final class FileRecoveryJournal {
    private static final int MAX_BYTES = 1_048_576;
    private final Path path;

    public FileRecoveryJournal(Path path) { this.path = Objects.requireNonNull(path); }

    public synchronized void append(JournalEvent event) throws IOException {
        String body = event.transactionId() + "|" + event.step() + "|" + event.phase().name();
        String line = body + "|" + checksum(body) + "\n";
        byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 256) throw new IOException("oversized journal record");
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.SYNC)) {
            ByteBuffer data = ByteBuffer.wrap(bytes);
            while (data.hasRemaining()) channel.write(data);
            channel.force(true);
        }
    }

    public synchronized List<JournalEvent> readAll() throws IOException {
        if (!Files.exists(path)) return List.of();
        if (Files.size(path) > MAX_BYTES) throw new IOException("journal exceeds size cap");
        String data = Files.readString(path, StandardCharsets.UTF_8);
        if (!data.isEmpty() && !data.endsWith("\n"))
            throw new IOException("truncated journal record; manual recovery required");
        var events = new ArrayList<JournalEvent>();
        for (String line : data.split("\n")) {
            if (line.isEmpty()) continue;
            String[] fields = line.split("\\|", -1);
            if (fields.length != 4) throw new IOException("malformed journal record");
            String body = fields[0] + "|" + fields[1] + "|" + fields[2];
            if (!checksum(body).equals(fields[3])) throw new IOException("journal checksum mismatch");
            try {
                events.add(new JournalEvent(UUID.fromString(fields[0]),
                    Integer.parseInt(fields[1]), JournalEvent.Phase.valueOf(fields[2])));
            } catch (IllegalArgumentException ex) {
                throw new IOException("invalid journal record", ex);
            }
        }
        return List.copyOf(events);
    }

    private static String checksum(String data) {
        var crc = new CRC32();
        crc.update(data.getBytes(StandardCharsets.UTF_8));
        return Long.toHexString(crc.getValue());
    }
}
