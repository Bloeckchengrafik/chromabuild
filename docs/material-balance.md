# Read-only material balance

This pure-core component calculates grouped material requirements, shortages and
unsupported assignments from a canonical plan and an explicit stock snapshot.

Tests cover repeated materials, exact stock, shortages, unknown blocks and invalid
stock counts. The initial supported set remains stone/calcite only.

This does **not** withdraw or reserve materials. Real survival implementation
must identify components, extract atomically or journal per-step operations,
re-check world and claims on the server thread, and reconcile interrupted
transactions. The snapshot may become stale immediately.
