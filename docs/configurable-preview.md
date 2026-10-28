# Configurable preview smoke test

Operator commands:

- `/chromabuild preview` — original 4x4 sample.
- `/chromabuild preview rectangle <width> <height> [seed]` — sample a world-space vertical rectangle anchored at the player's current block position; width and height are 1–16 blocks each, with at most 256 assignments.
- `/chromabuild preview confirm <session-uuid> <revision> <fingerprint>` — acknowledge an exact previously approved session, once.

The preview command reports selected stone/calcite quantities as well as the
session revision and fingerprint. A different seed or rectangle produces a new
canonical preview and invalidates the previous session.

**This is a diagnostic, not a building tool.** It does not check whether the
target is buildable or protected. It does not show ghost blocks, inspect wand
geometry, charge inventory, or modify the world.

A single-block rectangle uses a synthetic nonzero axis to avoid a degenerate
gradient; in-game visual effects and production state are not yet supported.
