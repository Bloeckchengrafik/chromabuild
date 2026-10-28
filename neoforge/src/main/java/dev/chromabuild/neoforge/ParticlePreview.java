package dev.chromabuild.neoforge;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import dev.chromabuild.core.BlockPosition;
import dev.chromabuild.core.GradientAxis;
import dev.chromabuild.core.Palette;
import dev.chromabuild.core.PaletteEntry;
import dev.chromabuild.core.PlacementMode;
import dev.chromabuild.core.PlacementPlanner;
import dev.chromabuild.core.PlacementSettings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerLevel;

/**
 * Ephemeral in-world preview using vanilla block particles.
 * This intentionally sends particles only; it never mutates the world.
 * Limited to a modest sample so operators cannot create large particle floods.
 */
public final class ParticlePreview {
    private ParticlePreview() {}

    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("visualize")
            .executes(ctx -> show(ctx.getSource(), 4, 4))
            .then(Commands.argument("width", IntegerArgumentType.integer(1, 8))
                .then(Commands.argument("height", IntegerArgumentType.integer(1, 8))
                    .executes(ctx -> show(ctx.getSource(),
                        IntegerArgumentType.getInteger(ctx, "width"),
                        IntegerArgumentType.getInteger(ctx, "height")))));
    }

    private static int show(CommandSourceStack source, int width, int height)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        var origin = player.blockPosition();
        var positions = new ArrayList<BlockPosition>(width * height);
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                positions.add(new BlockPosition(origin.getX() + x, origin.getY() + y, origin.getZ()));
        var palette = new Palette(List.of(
            new PaletteEntry("minecraft:stone", 1, 0, true, ""),
            new PaletteEntry("minecraft:calcite", 1, 1, true, "")
        ));
        var first = positions.getFirst();
        var last = positions.size() == 1
            ? new BlockPosition(first.x() + 1, first.y(), first.z())
            : positions.getLast();
        var plan = PlacementPlanner.generate(positions, palette,
            new PlacementSettings(PlacementMode.ORGANIC_GRADIENT, 42, .35, 5, 0, .4),
            new GradientAxis(first, last));
        for (var assignment : plan.assignments()) {
            var p = assignment.position();
            var state = assignment.blockStateId().equals("minecraft:calcite")
                ? Blocks.CALCITE.defaultBlockState() : Blocks.STONE.defaultBlockState();
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                p.x() + .5, p.y() + .5, p.z() + .5,
                4, .12, .12, .12, 0);
        }
        source.sendSuccess(() -> Component.literal(
            "Visualized " + width + "x" + height + " assignments as temporary block particles. " +
            "No blocks changed; particles are not a persistent ghost preview."), false);
        return Command.SINGLE_SUCCESS;
    }
}
