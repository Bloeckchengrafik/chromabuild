# Temporary visual preview experiment

Operator command:

`/chromabuild visualize [width height]`

Spawns vanilla block particles at the positions of a deterministic 4×4 (default)
stone/calcite plan next to the player. Optional width and height are each
limited to 1–8, for at most 64 sample positions. The particles are transient,
visible to nearby players, and are not a persistent ghost-block renderer.

**Safe by construction:** no block-state writes, no inventory operations,
no preview confirmations, and no client-only dependencies. Vanilla clients
can display these ordinary particles.

**Important limitation:** The command generates an independent sample plan;
it does not render the exact currently approved preview session. An accurate
client-side ghost preview still needs client networking, approved session
geometry, and immutable plan-to-render bindings.

Smoke-test in a NeoForge 1.21.1 client and dedicated server before treating
visual behavior as verified.
