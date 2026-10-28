package dev.chromabuild.neoforge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.chromabuild.core.BlockPosition;
import dev.chromabuild.core.GradientAxis;
import dev.chromabuild.core.Palette;
import dev.chromabuild.core.PaletteEntry;
import dev.chromabuild.core.PlacementMode;
import dev.chromabuild.core.PlacementPlan;
import dev.chromabuild.core.PlacementPlanner;
import dev.chromabuild.core.PlacementSettings;
import dev.chromabuild.core.session.PlanSessions;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;

/**
 * Operator-only interactive dry-run of canonical plan sessions.
 * No block edits, permission grants, or inventory consumption occur here.
 */
public final class PreviewCommands {
    private static final PlanSessions SESSIONS =
        new PlanSessions(Clock.systemUTC(), Duration.ofSeconds(90), 256);

    private PreviewCommands() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("preview")
            .executes(ctx -> {
                var player = ctx.getSource().getPlayerOrException();
                var origin = player.blockPosition();
                var plan = samplePlan(origin.getX(), origin.getY(), origin.getZ());
                var dimension = player.level().dimension().location().toString();
                var session = SESSIONS.publish(player.getUUID(), dimension, plan);
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "Preview ready: " + plan.assignments().size() + " blocks, revision " +
                    session.revision() + ", session " + session.id() + ", fingerprint " +
                    session.fingerprint() + ". Expires in 90s. Dry-run confirmation only."), false);
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.literal("confirm")
                .then(Commands.argument("session", UuidArgument.uuid())
                    .then(Commands.argument("revision", LongArgumentType.longArg(1))
                        .then(Commands.argument("fingerprint", StringArgumentType.word())
                            .executes(ctx -> confirm(ctx.getSource(),
                                UuidArgument.getUuid(ctx, "session"),
                                LongArgumentType.getLong(ctx, "revision"),
                                StringArgumentType.getString(ctx, "fingerprint")))))));
    }

    private static int confirm(CommandSourceStack source, UUID id, long revision, String fingerprint)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        try {
            var session = SESSIONS.consume(player.getUUID(),
                player.level().dimension().location().toString(), id, revision, fingerprint);
            source.sendSuccess(() -> Component.literal(
                "Confirmed preview " + session.id() + " (" +
                session.plan().assignments().size() +
                " blocks). Dry run: no blocks placed and no materials consumed."), false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException ex) {
            source.sendFailure(Component.literal("Preview invalid, expired, already confirmed, or not yours."));
            return 0;
        }
    }

    private static PlacementPlan samplePlan(int px, int py, int pz) {
        var positions = new ArrayList<BlockPosition>(16);
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++)
                positions.add(new BlockPosition(px + x, py + y, pz));
        var palette = new Palette(List.of(
            new PaletteEntry("minecraft:stone", 1, 0, true, ""),
            new PaletteEntry("minecraft:calcite", 1, 1, true, "")
        ));
        return PlacementPlanner.generate(positions, palette,
            new PlacementSettings(PlacementMode.ORGANIC_GRADIENT, 42L, 0.35, 5, 0, 0.4),
            new GradientAxis(positions.getFirst(), positions.getLast()));
    }
}
