package dev.chromabuild.neoforge;

import com.mojang.brigadier.Command;
import dev.chromabuild.core.execution.PlacementPreflight;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** Read-only Minecraft-side inspection of the currently approved preview. */
public final class WorldPreflight {
    private WorldPreflight() {}

    public static int report(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var level = player.serverLevel();
        var session = PreviewCommands.current(player.getUUID(), level.dimension().location().toString());
        if (session == null) {
            source.sendFailure(Component.literal("No active approved preview in this dimension."));
            return 0;
        }
        var gate = PlacementPreflight.check(session.plan(), 256);
        if (!gate.approved()) {
            source.sendFailure(Component.literal("Plan rejected: " + gate.reason()));
            return 0;
        }
        int unloaded = 0, outOfBounds = 0, blocked = 0, outOfReach = 0;
        for (var assignment : session.plan().assignments()) {
            var pos = assignment.position();
            var target = new BlockPos(pos.x(), pos.y(), pos.z());
            if (level.isOutsideBuildHeight(target) || !level.getWorldBorder().isWithinBounds(target)) {
                outOfBounds++;
            } else if (!level.hasChunkAt(target)) {
                unloaded++;
            } else if (player.distanceToSqr(target.getX() + .5, target.getY() + .5,
                                            target.getZ() + .5) > 36.0) {
                outOfReach++;
            } else if (!level.getBlockState(target).canBeReplaced()) {
                blocked++;
            }
        }
        int problems = unloaded + outOfBounds + outOfReach + blocked;
        String message = "World preflight for " + session.id() + ": " +
            session.plan().assignments().size() + " targets; " +
            outOfBounds + " outside bounds, " + unloaded + " unloaded, " +
            outOfReach + " beyond diagnostic reach, " + blocked + " nonreplaceable. " +
            (problems == 0 ? "Basic checks pass; NOT authorization to place." :
                "Not ready for placement.") +
            " Claims, entity collision and item costs NOT checked.";
        source.sendSuccess(() -> Component.literal(message), false);
        return problems == 0 ? Command.SINGLE_SUCCESS : 0;
    }
}
