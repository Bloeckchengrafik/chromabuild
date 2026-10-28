package dev.chromabuild.core;

import dev.chromabuild.core.serialization.NamedPalette;
import dev.chromabuild.core.serialization.PaletteJson;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PaletteJsonTest {
    @Test void roundTripsOrderedPaletteAndDisabledEntries() {
        var original = new NamedPalette("Weathered Castle", new Palette(List.of(
            new PaletteEntry("minecraft:deepslate_bricks", 30, 0, true, "stone"),
            new PaletteEntry("minecraft:stone_bricks", 0, 0.5, false, ""),
            new PaletteEntry("minecraft:calcite", 15, 1, true, ""))));
        assertEquals(original, PaletteJson.read(PaletteJson.write(original)));
    }
    @Test void acceptsDocumentedMinimalFormat() {
        var decoded = PaletteJson.read("""
            {"schema":1,"name":"Simple","entries":[
              {"block":"minecraft:stone","weight":60,"position":0.0},
              {"block":"minecraft:calcite","weight":40,"position":1.0}
            ]}
            """);
        assertEquals(2, decoded.palette().entries().size());
        assertTrue(decoded.palette().entries().getFirst().enabled());
    }
    @Test void rejectsWrongSchemaAndBadInputs() {
        assertThrows(IllegalArgumentException.class,
            () -> PaletteJson.read("{\"schema\":2,\"name\":\"x\",\"entries\":[]}"));
        assertThrows(IllegalArgumentException.class,
            () -> PaletteJson.read("{\"schema\":1,\"name\":\"x\",\"entries\":[{\"block\":\"minecraft:stone\",\"weight\":-1,\"position\":0}]}"));
        assertThrows(IllegalArgumentException.class, () -> PaletteJson.read("[]"));
        assertThrows(IllegalArgumentException.class, () -> PaletteJson.read("null"));
    }
}
