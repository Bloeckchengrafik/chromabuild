# Live-world diagnostic preflight

Operator command: `/chromabuild preview checkworld`.

Checks the exact current server-owned preview, without loading chunks or editing
the world. Rejects outside-height/border targets, unloaded chunks, positions
more than six blocks away (diagnostic heuristic), and targets whose existing
block state cannot be replaced. Applies the existing strict core preflight.

**Not a placement permission check:** claim integrations (including FTB
Chunks), entity collisions, tool limits, survival item transactions and
Minecraft placement events remain mandatory before construction may be enabled.
Check results are a momentary observation, not cached approval. They must be
re-evaluated for each actual placement.
