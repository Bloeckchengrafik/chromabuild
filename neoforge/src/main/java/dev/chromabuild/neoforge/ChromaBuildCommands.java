package dev.chromabuild.neoforge;

import com.mojang.brigadier.Command;
import dev.chromabuild.core.BlockPosition;
import dev.chromabuild.core.GradientAxis;
import dev.chromabuild.core.Palette;
import dev.chromabuild.core.PaletteEntry;
import dev.chromabuild.core.PlacementMode;
import dev.chromabuild.core.PlacementPlan;
import dev.chromabuild.core.PlacementPlanner;
import dev.chromabuild.core.PlacementSettings;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import java.util.ArrayList;
import java.util.List;

/** Operator-only non-mutating diagnostic commands. */
public final class ChromaBuildCommands {
    private ChromaBuildCommands() { }

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("chromabuild")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("probe").executes(context -> {
                var player = context.getSource().getPlayerOrException();
                var pos = player.blockPosition();
                var plan = samplePlan(pos.getX(), pos.getY(), pos.getZ());
                long calcite = plan.assignments().stream()
                    .filter(a -> a.blockStateId().equals("minecraft:calcite")).count();
                context.getSource().sendSuccess(() -> Component.literal(
                    "ChromaBuild dry run: " + plan.assignments().size() +
                    " assignments, " + calcite + " calcite, " +
                    (plan.assignments().size() - calcite) + " stone; no world changes."), false);
                return Command.SINGLE_SUCCESS;
            }))
            .then(Commands.literal("resources").executes(context -> {
                var pos = context.getSource().getPlayerOrException().blockPosition();
                return InventoryProbe.report(context.getSource(),
                    samplePlan(pos.getX(), pos.getY(), pos.getZ()));
            }))
            .then(PreviewCommands.register()));
    }

    private static PlacementPlan samplePlan(int px, int py, int pz) {
        var points = new ArrayList<BlockPosition>(16);
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++)
                points.add(new BlockPosition(px + x, py + y, pz));
        var palette = new Palette(List.of(
            new PaletteEntry("minecraft:stone", 1, 0, true, ""),
            new PaletteEntry("minecraft:calcite", 1, 1, true, "")
        ));
        var settings = new PlacementSettings(PlacementMode.ORGANIC_GRADIENT,
            42L, 0.35, 5, 0, 0.4);
        return PlacementPlanner.generate(points, palette, settings,
            new GradientAxis(points.getFirst(), points.getLast()));
    }
}
