# Building Wands integration investigation

Research target: [nicguzzo/wands](https://github.com/nicguzzo/wands), inspected upstream main at
`1cfc884a1d0b65d845d64319961ca6641c5a189c`.
**This is a source investigation, not a verified runtime API contract for a pinned 1.21.1 release.**
The project uses multi-version conditional preprocessing. Verify the exact 1.21.1 artifact before implementing hooks.

## Observed flow

- `wand/Interaction.java` sends clicked positions and palette seed via `ClientNetworking.SendPosPacket(...)`.
- `wand/WandMode.java` declares `place_in_buffer(Wand wand)` as its shape-generation contract.
- `wand/Wand.java` invokes `modes[m].place_in_buffer(this)` around line 584.
- Geometry and intended block states live in `utils/BlockBuffer.java`: coordinate arrays,
  `state[]`, `item[]`, and a bounded `length`.
- `Wand.java` around lines 603–748 handles mode-dependent limits, accounting,
  `inventory.check_inventory(this)`, per-block `place_block(...)`, and finally
  `inventory.remove_from_inventory(this, placed)`.
- `Wand.place_block(...)` around line 1204 calls `ClaimDispatch.claimCanInteract(...)`
  before checking the desired state, denylist and other placement rules.
- `InventoryManager.java` handles item consumption and storage-specific behavior;
  its accounting keys are `Item`, not arbitrary desired `BlockState`.
- `UndoManager.java` is the upstream transaction/undo subsystem and must not be duplicated
  by a ChromaBuild hook without an explicit ownership contract.

## Conclusions

**Most promising upstream seam:** a supported server-side material-assignment callback
after `place_in_buffer` and before inventory accounting, receiving candidate positions
and emitting a desired block state *plus associated survival item cost*. Merely editing
`BlockBuffer.state[]` is unsafe because `BlockBuffer.item[]` and `block_accounting` must
remain consistent.

**Do not hook only at `place_block`:** inventory permission/count decisions happen
before this call, while consumption happens afterward. A late material substitution
would risk unpaid placements, incorrect quantities, and inconsistent undo history.

**Do not use client-side geometry directly as authorization:** the server must independently
validate selection, wand limits, reach, relevant claim checks and actual block state.

## Follow-up work

1. Pin a released NeoForge 1.21.1 Building Wands JAR and exact source revision.
2. Trace `Networking.PosPacket` server handler into `Wand` processing and validate where
   server-side position regeneration happens.
3. Check for a stable public extension API or event; if none, propose a narrow upstream
   callback for per-position material selection and accounting.
4. Check the upstream undo and durability code against mixed-material placement.
5. Test with survival inventory shortages, magic bags, FTB Chunks and conflicting edits.

**Current implementation decision:** leave ChromaBuild independent of Building Wands binary
classes until a version-pinned adapter can be tested. The new `/chromabuild probe` command
exercises planning only and never changes the world.
