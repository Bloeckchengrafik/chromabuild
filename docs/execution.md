# Bounded execution coordinator (experimental)

The core now includes `ExecutionJob`, a deterministic per-tick coordinator
for an immutable server-approved assignment plan.

An adapter must implement four operations: validate, debit/stage, place and
refund. Each step is checked at execution time. Material is debited before
placing each block; failed placement attempts refund. If refund fails, the job
enters `NEEDS_RECOVERY` and cannot continue.

**No in-game block placement is enabled by this PR.** The current adapter is
a pure Java contract with fake implementations in tests. A production adapter
must journal debit/placement/refund transitions durably, including before a
potentially irreversible world modification; preserve a distinct recovery
identity for each step; account for side effects of modded blocks; implement
per-position FTB Chunks checks; respect wand reach and limits; and resolve
item components rather than inferring cost from block ID. It must also
ensure exceptions during debit cannot leave ambiguous staged materials.

The coordinator is deliberately not a substitute for a durable transaction
manager, and it cannot guarantee atomicity for arbitrary modded blocks.

Next integration gates:
1. Pin 1.21.1 Building Wands artifact and inspect a stable accounting hook.
2. Implement persistent transaction journal and fail-closed recovery.
3. Build a server-thread NeoForge adapter with real inventory, claim and
   placement semantics; test failure scenarios before enabling commands.
