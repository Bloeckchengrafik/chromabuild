package dev.chromabuild.core.serialization;

import dev.chromabuild.core.Palette;
import java.util.Objects;

/** A named, versioned palette payload; runtime block registration is validated by the NeoForge adapter. */
public record NamedPalette(String name, Palette palette) {
    public NamedPalette {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(palette, "palette");
        if (name.isBlank() || name.length() > 128)
            throw new IllegalArgumentException("palette name must be 1..128 characters");
    }
}
