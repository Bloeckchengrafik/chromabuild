package dev.chromabuild.neoforge;

import com.mojang.brigadier.Command;
import dev.chromabuild.core.execution.MaterialBalance;
import dev.chromabuild.core.execution.PlacementPreflight;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

/** Snapshot inventory readiness for an exact session; intentionally no withdrawal. */
public final class LiveReadiness {
    private LiveReadiness() {}

    public static int report(CommandSourceStack source)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        var session = PreviewCommands.current(player.getUUID(),
            player.level().dimension().location().toString());
        if (session == null) {
            source.sendFailure(Component.literal("No active preview in this dimension."));
            return 0;
        }
        var gate = PlacementPreflight.check(session.plan(), 256);
        if (!gate.approved()) {
            source.sendFailure(Component.literal("Plan rejected: " + gate.reason()));
            return 0;
        }
        Map<String, Integer> available = new HashMap<>();
        for (var stack : player.getInventory().items) {
            if (stack.is(net.minecraft.world.item.Items.STONE))
                available.merge("minecraft:stone", stack.getCount(), Math::addExact);
            else if (stack.is(net.minecraft.world.item.Items.CALCITE))
                available.merge("minecraft:calcite", stack.getCount(), Math::addExact);
        }
        var quote = MaterialBalance.quote(session.plan(), available);
        String message = "Approved preview " + session.id() + " revision " + session.revision() +
            ": required " + quote.required() + ", missing " + quote.missing() +
            ", unsupported " + quote.unsupported() + ". " +
            (quote.ready() ? "Stock present (NOT reserved)." : "Insufficient or unsupported materials.") +
            " No blocks or inventory changed.";
        source.sendSuccess(() -> Component.literal(message), false);
        return quote.ready() ? Command.SINGLE_SUCCESS : 0;
    }
}
