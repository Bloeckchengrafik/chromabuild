package dev.chromabuild.neoforge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
 * Operator-only interactive dry run. Never places blocks or withdraws resources.
 * Configurable dimensions are intentionally limited to 256 candidate positions.
 */
public final class PreviewCommands {
    private static final PlanSessions SESSIONS =
        new PlanSessions(Clock.systemUTC(), Duration.ofSeconds(90), 256);

    private PreviewCommands() {}

    /** The same session used by exact-plan confirmation; no fresh sample is generated. */
    public static dev.chromabuild.core.session.PlanSession current(
            UUID owner, String dimension) {
        return SESSIONS.current(owner, dimension);
    }

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("preview")
            .executes(ctx -> preview(ctx.getSource(), 4, 4, 42))
            .then(Commands.literal("rectangle")
                .then(Commands.argument("width", IntegerArgumentType.integer(1, 16))
                    .then(Commands.argument("height", IntegerArgumentType.integer(1, 16))
                        .executes(ctx -> preview(ctx.getSource(),
                            IntegerArgumentType.getInteger(ctx, "width"),
                            IntegerArgumentType.getInteger(ctx, "height"), 42))
                        .then(Commands.argument("seed", LongArgumentType.longArg())
                            .executes(ctx -> preview(ctx.getSource(),
                                IntegerArgumentType.getInteger(ctx, "width"),
                                IntegerArgumentType.getInteger(ctx, "height"),
                                LongArgumentType.getLong(ctx, "seed")))))))
            .then(Commands.literal("resources").executes(ctx -> resources(ctx.getSource())))
            .then(Commands.literal("checkworld").executes(ctx -> WorldPreflight.report(ctx.getSource())))
            .then(Commands.literal("readiness").executes(ctx -> LiveReadiness.report(ctx.getSource())))
            .then(Commands.literal("confirm")
                .then(Commands.argument("session", UuidArgument.uuid())
                    .then(Commands.argument("revision", LongArgumentType.longArg(1))
                        .then(Commands.argument("fingerprint", StringArgumentType.word())
                            .executes(ctx -> confirm(ctx.getSource(),
                                UuidArgument.getUuid(ctx, "session"),
                                LongArgumentType.getLong(ctx, "revision"),
                                StringArgumentType.getString(ctx, "fingerprint")))))));
    }

    private static int preview(CommandSourceStack source, int width, int height, long seed)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var origin = player.blockPosition();
        var plan = samplePlan(origin.getX(), origin.getY(), origin.getZ(), width, height, seed);
        var dimension = player.level().dimension().location().toString();
        var session = SESSIONS.publish(player.getUUID(), dimension, plan);
        long calcite = plan.assignments().stream()
            .filter(a -> a.blockStateId().equals("minecraft:calcite")).count();
        source.sendSuccess(() -> Component.literal(
            "Preview " + width + "x" + height + " (seed " + seed + "): " +
            calcite + " calcite, " + (plan.assignments().size() - calcite) +
            " stone. Revision " + session.revision() + ", session " + session.id() +
            ", fingerprint " + session.fingerprint() +
            ". Expires in 90s. No world changes."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int resources(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var session = current(player.getUUID(), player.level().dimension().location().toString());
        if (session == null) {
            source.sendFailure(Component.literal(
                "No active preview in this dimension. Run /chromabuild preview first."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(
            "Inventory quote for approved session " + session.id() +
            " revision " + session.revision() + " (not a reservation):"), false);
        return InventoryProbe.report(source, session.plan());
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

    private static PlacementPlan samplePlan(int px, int py, int pz, int width, int height, long seed) {
        if (width < 1 || height < 1 || (long) width * height > 256)
            throw new IllegalArgumentException("preview rectangle must contain 1..256 blocks");
        var positions = new ArrayList<BlockPosition>(width * height);
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                positions.add(new BlockPosition(px + x, py + y, pz));
        var palette = new Palette(List.of(
            new PaletteEntry("minecraft:stone", 1, 0, true, ""),
            new PaletteEntry("minecraft:calcite", 1, 1, true, "")
        ));
        // A single-block preview has no geometric extent; supply an arbitrary
        // nonzero axis rather than silently producing an invalid gradient.
        var start = positions.getFirst();
        var end = positions.size() == 1 ? new BlockPosition(px + 1, py, pz) : positions.getLast();
        return PlacementPlanner.generate(positions, palette,
            new PlacementSettings(PlacementMode.ORGANIC_GRADIENT, seed, 0.35, 5, 0, 0.4),
            new GradientAxis(start, end));
    }
}
