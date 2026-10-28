package dev.chromabuild.neoforge;

import dev.chromabuild.core.BlockPosition;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoForge bootstrap only. Placement is intentionally disabled until the
 * server-authoritative execution and resource-accounting pipeline is implemented.
 */
@Mod(ChromaBuildMod.MOD_ID)
public final class ChromaBuildMod {
    public static final String MOD_ID = "chromabuild";
    private static final Logger LOG = LoggerFactory.getLogger(ChromaBuildMod.class);

    public ChromaBuildMod() {
        // This reference exercises the core dependency while remaining safe on dedicated servers.
        BlockPosition origin = new BlockPosition(0, 0, 0);
        LOG.info("ChromaBuild loaded (core origin: {}, {}, {})", origin.x(), origin.y(), origin.z());
    }
}
