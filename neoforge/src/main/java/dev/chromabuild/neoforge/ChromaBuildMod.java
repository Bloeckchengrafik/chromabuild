package dev.chromabuild.neoforge;

import dev.chromabuild.core.BlockPosition;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoForge bootstrap. No blocks or resources are modified by this module.
 */
@Mod(ChromaBuildMod.MOD_ID)
public final class ChromaBuildMod {
    public static final String MOD_ID = "chromabuild";
    private static final Logger LOG = LoggerFactory.getLogger(ChromaBuildMod.class);

    public ChromaBuildMod() {
        NeoForge.EVENT_BUS.addListener(ChromaBuildCommands::register);
        BlockPosition origin = new BlockPosition(0, 0, 0);
        LOG.info("ChromaBuild loaded (core origin: {}, {}, {})", origin.x(), origin.y(), origin.z());
    }
}
