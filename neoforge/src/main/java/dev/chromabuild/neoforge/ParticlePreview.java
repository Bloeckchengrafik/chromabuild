package dev.chromabuild.neoforge;

import com.mojang.brigadier.Command;
import dev.chromabuild.core.session.PlanSession;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;

/**
 * Non-mutating transient preview of the EXACT approved server-owned plan.
 * Only current owner/dimension sessions may be rendered; the session is not consumed.
 */
public final class ParticlePreview {
    private ParticlePreview() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("visualize").executes(ctx -> show(ctx.getSource()));
    }

    private static int show(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        String dimension = level.dimension().location().toString();
        PlanSession session = PreviewCommands.current(player.getUUID(), dimension);
        if (session == null) {
            source.sendFailure(Component.literal(
                "No active preview in this dimension. Run /chromabuild preview first."));
            return 0;
        }
        int shown = 0;
        int unsupported = 0;
        for (var assignment : session.plan().assignments()) {
            var p = assignment.position();
            // This diagnostic palette supports only the two explicitly generated
            // sample states; never pretend unknown states are stone.
            var state = switch (assignment.blockStateId()) {
                case "minecraft:calcite" -> Blocks.CALCITE.defaultBlockState();
                case "minecraft:stone" -> Blocks.STONE.defaultBlockState();
                default -> null;
            };
            if (state == null) {
                unsupported++;
                continue;
            }
            level.sendParticles(player,
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                false, p.x() + .5, p.y() + .5, p.z() + .5,
                4, .12, .12, .12, 0);
            shown++;
        }
        int rendered = shown, skipped = unsupported;
        source.sendSuccess(() -> Component.literal(
            "Visualized approved preview " + session.id() + " revision " +
            session.revision() + " (" + rendered + " positions, " + skipped +
            " unsupported). Temporary particles; no world changes."), false);
        return Command.SINGLE_SUCCESS;
    }
}
