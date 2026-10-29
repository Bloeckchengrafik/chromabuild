# Exact-plan visual diagnostic

The operator workflow is now:
1. `/chromabuild preview rectangle 8 8 12345` — creates the canonical server-owned preview.
2. `/chromabuild visualize` — sends transient vanilla block particles for **that same plan**, using its exact stored assignments and world positions.
3. `/chromabuild preview confirm <session> <revision> <fingerprint>` — consumes the same plan in a dry run.

Visualization checks the preview's owner, dimension and expiration without consuming it.
If a new preview supersedes the old one, visualization automatically uses the latest
revision. After confirmation, there is no active preview to visualize.

This is still **not** a persistent client-side ghost renderer. Particles are
visible only temporarily and represent supported sample block states (stone
and calcite). Other states are counted as unsupported instead of rendered
incorrectly. Only the owning player receives particles. This command does
not check placement permissions or material availability and never mutates
the world.

Manual in-game and mixed-client testing is required in addition to CI.
