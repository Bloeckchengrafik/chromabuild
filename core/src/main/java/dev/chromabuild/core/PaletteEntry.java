package dev.chromabuild.core;

import java.util.Objects;

/** A block-state identifier and metadata. Item cost is resolved separately by the server adapter. */
public record PaletteEntry(String stateId, double weight, double gradientPosition, boolean enabled, String variationGroup) {
    public PaletteEntry {
        Objects.requireNonNull(stateId, "stateId");
        Objects.requireNonNull(variationGroup, "variationGroup");
        if (stateId.isBlank()) throw new IllegalArgumentException("blank block state");
        if (!Double.isFinite(weight) || weight < 0) throw new IllegalArgumentException("invalid weight");
        if (!Double.isFinite(gradientPosition) || gradientPosition < 0 || gradientPosition > 1)
            throw new IllegalArgumentException("gradient position must be in [0,1]");
    }
}
