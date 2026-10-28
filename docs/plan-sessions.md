# Canonical preview sessions

The procedural engine returns assignments, not authority. The **server** creates
the canonical plan and issues an opaque session ID, revision, expiration and SHA-256
fingerprint over its ordered assignments.

`PlanSessions.publish` replaces the previous preview for that player.
`consume` checks owner, dimension, revision, digest and expiry, then invalidates
the session. Invalid confirmations cannot advance or reuse plans.

**Not implemented here:** networking, persistence, resource withdrawal, world
validation or placement. The session store is intentionally in-memory and requires
single-threaded server access. A process restart invalidates all uncommitted plans.
The resource transaction journal is a separate concern.

## Required server execution order

1. Authenticate player and resolve server-side tool geometry.
2. Generate canonical assignments, imposing selection and size limits.
3. Publish the preview with a stable revision and content hash.
4. On confirmation, validate current tool, dimension, reach, permissions and
   every target block; check or stage appropriate item-component requirements.
5. Consume the exact session once; execute in bounded batches with a durable
   transaction record and recovery policy.
6. If world/resource state no longer matches, reject or explicitly re-preview.
   Never silently substitute blocks after the client approves a preview.

The fingerprint is an integrity identifier **not a signature** and not permission.
A successful consume is not proof that materials or claims remain valid.
