package dev.chromabuild.core;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CoreInvariantTest {
    @Test void paletteDefensivelyCopiesAndPreservesOrder() {
        var source = new ArrayList<PaletteEntry>();
        source.add(new PaletteEntry("minecraft:calcite", 2, 1, true, ""));
        source.add(new PaletteEntry("minecraft:stone", 3, 0, true, ""));
        var palette = new Palette(source);
        source.clear();
        assertEquals(2, palette.entries().size());
        assertEquals("minecraft:calcite", palette.entries().getFirst().stateId());
        assertEquals("minecraft:stone", palette.gradientStops().getFirst().stateId());
        assertThrows(UnsupportedOperationException.class, () -> palette.entries().clear());
    }

    @Test void rejectsOverflowingWeightTotalAndInvalidSettings() {
        assertThrows(IllegalArgumentException.class, () -> new Palette(List.of(
            new PaletteEntry("minecraft:stone", Double.MAX_VALUE, 0, true, ""),
            new PaletteEntry("minecraft:calcite", Double.MAX_VALUE, 1, true, ""))));
        assertThrows(IllegalArgumentException.class, () -> new PlacementSettings(
            PlacementMode.ORGANIC_GRADIENT, 1, 0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new PaletteEntry(
            "minecraft:stone", Double.NaN, 0, true, ""));
    }

    @Test void diagonalProjectionAndNoiseContinuity() {
        var axis = new GradientAxis(new BlockPosition(0,0,0), new BlockPosition(10,10,10));
        assertEquals(0.5, axis.position(new BlockPosition(5,5,5)), 1e-12);
        assertEquals(0, axis.position(new BlockPosition(-4,-4,-4)));
        assertEquals(1, axis.position(new BlockPosition(20,20,20)));
        double origin = SpatialNoise.smooth3(123, 1.25, 2.5, -3.75);
        assertEquals(origin, SpatialNoise.smooth3(123, 1.25, 2.5, -3.75));
        assertTrue(Math.abs(origin - SpatialNoise.smooth3(123, 1.250001, 2.5, -3.75)) < 1e-4);
    }
}
