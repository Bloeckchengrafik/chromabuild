package dev.chromabuild.core;

import dev.chromabuild.core.execution.ExecutionJob;
import dev.chromabuild.core.execution.ExecutionPort;
import dev.chromabuild.core.execution.MaterialBalance;
import dev.chromabuild.core.execution.PlacementPreflight;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end fake-adapter scenarios. Does not assert Minecraft-side atomicity
 * or crash recovery, which require a real server transaction implementation.
 */
class ExecutionScenarioTest {
    private static PlacementPlan plan(String... states) {
        var list = new ArrayList<PlacementPlan.Assignment>();
        for (int i = 0; i < states.length; i++)
            list.add(new PlacementPlan.Assignment(new BlockPosition(i, 64, 0), states[i]));
        return new PlacementPlan(list);
    }

    private static final class StockPort implements ExecutionPort {
        final Map<String, Integer> stock;
        final List<BlockPosition> placed = new ArrayList<>();
        final int rejectAt;
        final boolean refundWorks;
        StockPort(Map<String, Integer> stock, int rejectAt, boolean refundWorks) {
            this.stock = new HashMap<>(stock);
            this.rejectAt = rejectAt;
            this.refundWorks = refundWorks;
        }
        @Override public boolean canPlace(PlacementPlan.Assignment a) {
            return true;
        }
        @Override public boolean debit(PlacementPlan.Assignment a) {
            var item = a.blockStateId();
            if (stock.getOrDefault(item, 0) < 1) return false;
            stock.merge(item, -1, Integer::sum);
            return true;
        }
        @Override public boolean place(PlacementPlan.Assignment a) {
            if (a.position().x() == rejectAt) return false;
            placed.add(a.position());
            return true;
        }
        @Override public boolean refund(PlacementPlan.Assignment a) {
            if (!refundWorks) return false;
            stock.merge(a.blockStateId(), 1, Integer::sum);
            return true;
        }
    }

    @Test void successfulQuoteAndBoundedExecutionConsumeExactStock() {
        var plan = plan("minecraft:stone", "minecraft:calcite", "minecraft:stone");
        var available = Map.of("minecraft:stone", 2, "minecraft:calcite", 1);
        assertTrue(PlacementPreflight.check(plan, 3).approved());
        assertTrue(MaterialBalance.quote(plan, available).ready());
        var port = new StockPort(available, -1, true);
        var job = new ExecutionJob(plan, port);
        assertEquals(2, job.tick(2));
        assertEquals(ExecutionJob.State.RUNNING, job.state());
        assertEquals(1, job.tick(2));
        assertEquals(ExecutionJob.State.COMPLETED, job.state());
        assertEquals(3, port.placed.size());
        assertEquals(0, port.stock.get("minecraft:stone"));
        assertEquals(0, port.stock.get("minecraft:calcite"));
    }

    @Test void shortagePreventsUnpaidPlacement() {
        var plan = plan("minecraft:stone", "minecraft:stone");
        assertFalse(MaterialBalance.quote(plan, Map.of("minecraft:stone", 1)).ready());
        var port = new StockPort(Map.of("minecraft:stone", 1), -1, true);
        var job = new ExecutionJob(plan, port);
        assertEquals(2, job.tick(5));
        assertEquals(ExecutionJob.State.STOPPED, job.state());
        assertEquals(1, job.placed());
        assertEquals(1, port.placed.size());
        assertEquals(0, port.stock.get("minecraft:stone"));
    }

    @Test void failedPlacementReturnsStagedMaterialAndStops() {
        var port = new StockPort(Map.of("minecraft:stone", 2), 0, true);
        var job = new ExecutionJob(plan("minecraft:stone", "minecraft:stone"), port);
        assertEquals(1, job.tick(8));
        assertEquals(ExecutionJob.State.STOPPED, job.state());
        assertEquals(2, port.stock.get("minecraft:stone"));
        assertTrue(port.placed.isEmpty());
    }

    @Test void failedRefundQuarantinesBeforeNextPlacement() {
        var port = new StockPort(Map.of("minecraft:stone", 3), 0, false);
        var job = new ExecutionJob(plan("minecraft:stone", "minecraft:stone"), port);
        assertEquals(1, job.tick(8));
        assertEquals(ExecutionJob.State.NEEDS_RECOVERY, job.state());
        assertEquals(2, port.stock.get("minecraft:stone")); // one staged, two still available
        assertTrue(port.placed.isEmpty());
        assertEquals(0, job.tick(8));
    }

    @Test void unsupportedMaterialsFailPreflightEvenWhenStockClaimsEnough() {
        var plan = plan("modded:machine");
        assertFalse(PlacementPreflight.check(plan, 10).approved());
        assertFalse(MaterialBalance.quote(plan, Map.of("modded:machine", 10)).ready());
    }
}
