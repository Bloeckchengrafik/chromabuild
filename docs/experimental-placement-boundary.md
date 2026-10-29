# Experimental single-block placement boundary

The next milestone is a **single ordinary stone/calcite block**, not arbitrary
Building Wands geometry. The provisional authorization check requires an
operator, non-spectator, exactly one allowlisted assignment, loaded in-bounds
target, five-block reach, empty air and no entity collision.

Critically, this check **always returns a non-authorizing result**
(`claims NOT verified`) after its local checks. Real placement must remain
disabled until FTB Chunks claim authorization and a safe transaction adapter
are implemented and validated against the pinned Minecraft version.

This is intentionally narrower than the full design and cannot be used to
bypass claims, wand restrictions or inventory costs.
