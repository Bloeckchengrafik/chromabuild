# Single-block placement gate smoke test

Operators can run:
1. `/chromabuild preview rectangle 1 1`
2. `/chromabuild preview placementgate`

The command runs the real server-side experimental gate against the exact
approved preview and reports its first denial reason. This is intentionally
**not** a placement command. A local world that passes the basic checks is
still rejected as `claims NOT verified`.

There is no claim of full FTB Chunks integration. Production construction
requires a verified claim adapter, complete material transaction metadata,
per-world journal storage and explicit recovery before world mutation can
be enabled.

This diagnostic is an acceptance checkpoint: it demonstrates whether the
single-block path reaches the claim-authorization barrier while preserving
client-optional operation and zero world/inventory changes.
