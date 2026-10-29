# In-game inventory quote smoke test

Operator command: `/chromabuild resources`.

Builds the same 4x4 dry-run assignment sample as `/chromabuild probe`,
resolves ordinary registered blocks to their associated block items, counts
matching player main-inventory stacks, and reports shortages without changing
anything.

**Known limitations:** This is an instrumentation step, not a survival payment
adapter. It does not handle item components, custom placement items, multiple
items per placement, offhand, shulker contents, AE2, Magic Bag, claims, tool
durability or the final player-selected geometry. Unsupported identities are
reported as unresolved, never treated as free. The command is operator-only.

Completion condition: CI builds NeoForge module and an in-game smoke test
confirms the quote reflects ordinary main-inventory items.
