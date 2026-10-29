# Durable intent logging around side effects

The new `JournaledExecutionPort` wraps a future Minecraft adapter.
It writes and synchronously flushes intent records **before** item debit,
world mutation and refunds, then completion markers after a confirmed result.
An exception after a side effect leaves the journal unresolved and the
execution job quarantined. Explicit completion requires every assignment
to have returned successful placement.

This is not enabled in Minecraft. The journal currently lacks sufficient
persistent assignment/item/previous-world-state metadata for automatic
reconciliation. Transactions still require server-specific ID ownership,
per-world journal storage, startup scanning and manual recovery workflows
before a production adapter can safely consume resources.
