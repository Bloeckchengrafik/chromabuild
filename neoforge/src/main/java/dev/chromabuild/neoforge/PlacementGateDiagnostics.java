package dev.chromabuild.neoforge;

import com.mojang.brigadier.Command;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/**
 * Explain the actual experimental authorization gate for the approved plan.
 * No inventory mutation, world edit, session consumption, or claim bypass.
 */
public final class PlacementGateDiagnostics {
    private PlacementGateDiagnostics() { }

    public static int report(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var level = player.serverLevel();
        var session = PreviewCommands.current(player.getUUID(),
            level.dimension().location().toString());
        if (session == null) {
            source.sendFailure(Component.literal("No approved preview. Run /chromabuild preview rectangle 1 1 first."));
            return 0;
        }
        String denied = PlacementAuthorization.denyReason(player, session.plan());
        source.sendFailure(Component.literal(
            "Experimental placement gate for session " + session.id() +
            ": DENIED (" + denied + "). No blocks or items changed."));
        return 0;
    }
}
