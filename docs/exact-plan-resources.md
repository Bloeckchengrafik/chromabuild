# Exact-plan resource quote (diagnostic)

Run:
1. `/chromabuild preview rectangle 8 8 12345` — create a server-approved plan.
2. `/chromabuild visualize` — render the very same plan with transient particles.
3. `/chromabuild preview resources` — compare the current main inventory against the very same stored plan.
4. `/chromabuild preview confirm <uuid> <revision> <fingerprint>` — consume the approved session, without building.

Resource reporting uses the plan's immutable material assignments, not a new sample generated near the player's current position. Repeating the resource command after moving the player quotes the **same plan**; after expiration, replacement or confirmation, it fails closed.

The quote is only an estimate: inventory may change, complex item components and modded placement costs are not resolved, and no materials are reserved or withdrawn. Unsupported blocks are explicitly reported and never treated as free. No world edits occur.
