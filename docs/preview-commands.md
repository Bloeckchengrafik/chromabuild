# Preview session smoke test

Operator-only diagnostics:

- `/chromabuild preview` — generates a fixed 4x4 stone/calcite sample near the player's current position and prints an immutable session ID, revision and fingerprint.
- `/chromabuild preview confirm <session-uuid> <revision> <fingerprint>` — confirms the **exact** approved plan once, checking player ownership, dimension, revision and expiry.

Sessions expire after 90 seconds. Generating a new preview invalidates the previous one.
Confirming twice, changing dimensions, or confirming another player's session is rejected.

**Important:** These commands are dry-run diagnostics. Confirmation never mutates blocks,
checks resource costs, consumes materials, or grants building permission.
The command layer has no renderer or client networking. This feature proves the
server-session handshake only.

The current static in-memory session store is process-scoped; world teardown lifecycle,
client networking, canonical geometry validation, Wand selection and resource transactions
must be implemented before production placement is enabled.
