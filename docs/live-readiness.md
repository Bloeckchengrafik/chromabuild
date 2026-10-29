# Live material-readiness diagnostic

`/chromabuild preview readiness` checks the **same immutable session** shown
by `/chromabuild visualize`. It totals stone/calcite in the player's main
inventory and uses the pure-core material balance to compute shortages.

This never withdraws or reserves items; the result can become stale immediately.
Readiness does not imply placement authorization. Reach, claims, collision,
block state, item-component rules, and durable transactions must be validated
during real construction. The operator-only command is a smoke-test harness.
