package dev.chromabuild.neoforge;

import dev.chromabuild.core.PlacementPlan;
import dev.chromabuild.core.execution.PlacementPreflight;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

/** Conservative one-block-only experimental gate. Not a Building Wands adapter. */
public final class PlacementAuthorization {
    private PlacementAuthorization() { }

    public static String denyReason(ServerPlayer player, PlacementPlan plan) {
        if (!player.hasPermissions(2)) return "operator permission required";
        if (player.isSpectator()) return "spectator";
        var preflight = PlacementPreflight.check(plan, 1);
        if (!preflight.approved()) return preflight.reason();
        var assignment = plan.assignments().getFirst();
        var p = assignment.position();
        var pos = new BlockPos(p.x(), p.y(), p.z());
        var level = player.serverLevel();
        if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos))
            return "outside world bounds";
        if (!level.hasChunkAt(pos)) return "chunk not loaded";
        if (player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 25.0)
            return "out of reach";
        if (!level.getBlockState(pos).isAir()) return "target is not air";
        if (!level.getEntities(player, new AABB(pos)).isEmpty())
            return "entity collision";
        return "claims NOT verified";
    }
}
