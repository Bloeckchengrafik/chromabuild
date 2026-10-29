# Experimental placement eligibility boundary

The diagnostic plan resource quote now distinguishes **known ordinary block
materials** from unsupported assignments. Only the exact block-state IDs
`minecraft:stone` and `minecraft:calcite` are recognized by the experimental
eligibility check. Every other state, including unfamiliar block properties,
tile entities, and modded blocks, is **unsupported by default**.

This is intentionally a conservative allowlist for upcoming integration tests,
not a universal Minecraft cost model. It avoids assuming that any arbitrary
block can be paid for using one matching block item.

The inventory quote still does not reserve items or check protected regions,
block replacement rules, or placement side effects. Do not enable actual world
edits based on this allowlist alone. A full material-cost resolver and server
placement/claim checks must follow.
