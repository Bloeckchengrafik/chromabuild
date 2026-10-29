# Fake-adapter execution scenarios

Integration-style unit tests exercise the complete core path:
1. strict placement preflight;
2. canonical plan material quote;
3. bounded execution and mock staged-material debits;
4. stop/refund or recovery quarantine on placement failures.

These are **not** Minecraft integration tests and do not certify inventory
atomicity, FTB Chunks permissions, placement lifecycle, or crash recovery.
They are regression gates for future NeoForge adapter work.

No production server placement command is enabled by this PR.
