# Ambiguous execution failures

A production block-operation API may modify world or inventory state and then
throw. Its exception is **not** proof that the action was rolled back.

The coordinator now quarantines the operation on a validation, debit or
placement exception. It never refunds a placement whose outcome is ambiguous.
Only an explicit `false` from `place`, contractually meaning no mutation,
permits the existing refund path.

This closes a possible item-duplication path in future adapters. It does not
replace durable transaction journaling or reconciliation after process crashes.
