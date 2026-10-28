# Durable recovery groundwork (not enabled for Minecraft placement)

This PR adds an append-only, synchronously flushed record file and a conservative
startup scanner. Each entry carries transaction ID, step index, transition marker,
and CRC32 for accidental corruption detection.

The scanner classifies any transaction not explicitly completed as **unresolved**.
It neither places blocks nor automatically refunds resources.

Future production adapter requirements:
- Write `BEGIN` before extraction; write `DEBIT_INTENT` before an external debit.
- An interrupted intent is ambiguous even if no `DEBIT_DONE` record exists.
- Record `PLACE_INTENT` before changing the world, and `PLACE_DONE` afterward.
- Record refund intent and completion separately.
- Only write `COMPLETE` when all consequences are reconciled.
- Keep the file per-world in server saved data; never share one journal across worlds.
- Persist full assignment, item components/count, dimension, player identity,
  previous state, and recovery context before enabling live placement.
- On startup, quarantine unresolved operations and require operator recovery.
  No automatic refunds or replay that could dupe items.

This is deliberately a **record format and recovery detector only**, not a
production crash-safe placement transaction. CRC32 detects corruption but does
not authenticate hostile modifications. Journal truncation, rotation, disk-full
behavior and real server startup integration require further work.
