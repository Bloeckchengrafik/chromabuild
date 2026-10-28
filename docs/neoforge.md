# NeoForge development bootstrap

Targets Minecraft 1.21.1, Java 21 and NeoForge 21.1.265 with ModDevGradle 2.0.148.

## Commands

```bash
gradle :core:test
gradle :neoforge:build
gradle :neoforge:runClient
gradle :neoforge:runServer
```

The server run may terminate before full startup until its Minecraft EULA is accepted.
Do not disable the EULA for automation.

## Scope and caveats

- This module registers the mod with NeoForge and references the pure Java core.
- No procedural placement commands, client screens, custom network payloads, or material extraction exist.
- The mod declares no content registries and should not impose client registry synchronization requirements.
- **Mixed vanilla-client joining still requires an explicit integration test** before we claim compatibility.
- Building Wands, AE2 and FTB Chunks are not dependencies yet.
- The runtime mod JAR includes `:core` classes. Gson is supplied by the Minecraft runtime.
- The `neoforge:build` CI job checks packaging/compilation, *not* in-game startup.
- License placeholder must be chosen before public release.
