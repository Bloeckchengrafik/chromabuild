# Implementation slices

This repository is organized around reviewable vertical slices, with the source specification as the acceptance reference.

1. **Integration investigation:** pin Minecraft 1.21.1 / NeoForge / Building Wands / Chroma Studio / AE2 / FTB Chunks versions and verify supported API hooks.
2. **Deterministic core:** ordered palettes, gradient projection, weighted random and smooth organic noise. Finish serialization and stronger property tests next.
3. **Server-authoritative MVP:** canonical immutable plans, approved revisions, inventory staging, per-position claim checks and bounded placement.
4. **Building Wands integration:** preserve wand limits, durability, selection modes and undo behavior.
5. **Client experience:** palette UI, Chroma Studio clipboard import, world-space anchors and exact server-approved previews.
6. **AE2 and resilience:** wireless access validation, recovery journal, undo, depletion/multiplayer testing.

## Non-negotiable rules

- Never treat a `PlacementPlan` as a permission grant. The server validates each position.
- A quote is not a material reservation; no free blocks, duplicated refunds or silently substituted plans.
- Preserve connection of clients without the optional client addon.
- Pure core is independent of Minecraft classes and seeded by absolute world coordinates.
- Do not implement Building Wands integration with a Mixin until a supported hook or upstream change has been investigated.

## PR-001 scope

- Java 21 Gradle multi-project skeleton (`core`).
- Immutable palette and plan models.
- Weighted random and world-space linear/organic gradients.
- Determinism and basic distribution regression tests.

**Not included:** NeoForge runtime, registry validation, preview/UI, survival transactions, AE2 integration.
