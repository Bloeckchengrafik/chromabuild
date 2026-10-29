# Placement preflight gate

This small pure-Java check runs on an exact approved assignment plan. It rejects
empty plans, excessive assignment counts, duplicate world positions and any
material outside the experimental stone/calcite allowlist.

It is **not** an authorization mechanism and cannot be used alone to enable
placement. The NeoForge executor still needs live checks for reach, claims,
chunk loading, existing block replaceability, player collisions, material
components, item withdrawals, per-tick budgets and durable crash recovery.

This gate exists so the next live integration cannot accidentally accept
arbitrary modded blocks or an oversized selection merely because the
underlying planner produced it.
