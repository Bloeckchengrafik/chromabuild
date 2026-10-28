# Implementation slices

This repository is organized around reviewable vertical slices.

1. **Integration investigation:** pin Minecraft 1.21.1 / NeoForge / Building Wands / Chroma Studio / AE2 / FTB Chunks versions and verify API hooks.
2. **Deterministic core:** ordered palettes, gradient projection, weighted random and smooth organic noise; versioned serialization and tests.
3. **Server-authoritative MVP:** canonical immutable plans, approved revisions, inventory staging, per-position claim checks and bounded placement.
4. **Building Wands integration:** preserve wand limits, durability, selection modes and undo behavior.
5. **Client experience:** palette UI, Chroma Studio clipboard import, world-space anchors and exact server-approved previews.
6. **AE2 and resilience:** wireless access validation, recovery journal, undo, depletion/multiplayer testing.

## Safety invariants

- `PlacementPlan` is not authorization to change the world. The server must validate each position.
- A resource quote is not a reservation; no free blocks, duplicated refunds or silent substitutions.
- Preserve connections from clients without the optional client addon.
- The core has no Minecraft dependency and samples using absolute world coordinates.
- Prefer supported Building Wands integration hooks over Mixins.

## Verification

Java 21 and Gradle 8.14.3; run `gradle :core:test :core:build`.
The GitHub Actions workflow performs this on pull requests and pushes to main.
