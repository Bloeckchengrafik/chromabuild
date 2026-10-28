# Palette JSON schema 1

The core supports JSON import/export independently from Minecraft.

```json
{
  "schema": 1,
  "name": "Weathered Castle",
  "entries": [
    {"block": "minecraft:deepslate_bricks", "weight": 30, "position": 0.0},
    {"block": "minecraft:stone_bricks", "weight": 25, "position": 0.7,
     "enabled": true, "variationGroup": "stone"}
  ]
}
```

The entry order is preserved. `enabled` defaults to true; `variationGroup` defaults to empty.
Weights must be nonnegative finite numbers and their enabled sum positive and finite.
Positions must lie in [0,1]. At most 128 entries are accepted.

**Important:** `block` is currently an opaque block-state identifier in the independent core.
Validation of actual Minecraft registry IDs, state properties and item costs belongs to the
NeoForge adapter. Importing a palette must never authorize world edits.
