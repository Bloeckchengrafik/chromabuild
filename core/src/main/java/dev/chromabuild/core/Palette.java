package dev.chromabuild.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Preserves caller order, sorting only a gradient view by explicit stop position. */
public record Palette(List<PaletteEntry> entries) {
    public Palette {
        entries = List.copyOf(entries);
        if (entries.isEmpty() || entries.size() > 128) throw new IllegalArgumentException("palette size must be 1..128");
        if (entries.stream().noneMatch(e -> e.enabled() && e.weight() > 0))
            throw new IllegalArgumentException("no active positive-weight entry");
        double total = 0;
        for (var entry : entries) if (entry.enabled()) total += entry.weight();
        if (!Double.isFinite(total) || total <= 0)
            throw new IllegalArgumentException("total active weight must be finite and positive");
    }
    public List<PaletteEntry> active() {
        return entries.stream().filter(e -> e.enabled() && e.weight() > 0).toList();
    }
    public List<PaletteEntry> gradientStops() {
        var stops = new ArrayList<>(active());
        stops.sort(Comparator.comparingDouble(PaletteEntry::gradientPosition));
        return List.copyOf(stops);
    }
}
