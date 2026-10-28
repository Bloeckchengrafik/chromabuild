package dev.chromabuild.core;

public record PlacementSettings(PlacementMode mode, long seed, double blend, double patchSize,
                                double variation, double maxDisplacement) {
    public PlacementSettings {
        if (mode == null) throw new IllegalArgumentException("missing mode");
        if (!Double.isFinite(blend) || blend < 0 || blend > 1) throw new IllegalArgumentException("blend [0,1]");
        if (!Double.isFinite(patchSize) || patchSize < 1 || patchSize > 32) throw new IllegalArgumentException("patchSize [1,32]");
        if (!Double.isFinite(variation) || variation < 0 || variation > 1) throw new IllegalArgumentException("variation [0,1]");
        if (!Double.isFinite(maxDisplacement) || maxDisplacement < 0 || maxDisplacement > 1)
            throw new IllegalArgumentException("maxDisplacement [0,1]");
    }
}
