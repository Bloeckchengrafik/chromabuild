package dev.chromabuild.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/** Pure computation only. Validation, inventory and world mutations belong to the server. */
public final class PlacementPlanner {
    private PlacementPlanner() { }
    public static PlacementPlan generate(List<BlockPosition> positions, Palette palette,
                                         PlacementSettings settings, GradientAxis axis) {
        if (positions.size()>256) throw new IllegalArgumentException("standard plan limit is 256");
        if (settings.mode()!=PlacementMode.WEIGHTED_RANDOM && axis==null)
            throw new IllegalArgumentException("gradient mode requires an axis");
        var seen=new HashSet<BlockPosition>();
        var assignments=new ArrayList<PlacementPlan.Assignment>(positions.size());
        for (var p:positions) {
            if (!seen.add(p)) throw new IllegalArgumentException("duplicate position: "+p);
            assignments.add(new PlacementPlan.Assignment(p,select(p,palette,settings,axis).stateId()));
        }
        return new PlacementPlan(assignments);
    }
    private static PaletteEntry select(BlockPosition p, Palette palette, PlacementSettings s, GradientAxis axis) {
        if (s.mode()==PlacementMode.WEIGHTED_RANDOM)
            return weighted(palette.active(),SpatialNoise.unit(s.seed(),p.x(),p.y(),p.z(),1));
        double t=axis.position(p);
        if (s.mode()==PlacementMode.ORGANIC_GRADIENT && s.blend()>0) {
            double noise=SpatialNoise.smooth3(s.seed(),p.x()/s.patchSize(),p.y()/s.patchSize(),p.z()/s.patchSize());
            t=Math.clamp(t+s.blend()*s.maxDisplacement()*noise,0,1);
        }
        var stops=palette.gradientStops();
        PaletteEntry primary=stops.getFirst();
        double distance=Math.abs(primary.gradientPosition()-t);
        for (var entry:stops) {
            double d=Math.abs(entry.gradientPosition()-t);
            if (d<distance) { primary=entry; distance=d; }
        }
        if (s.mode()!=PlacementMode.ORGANIC_GRADIENT || s.variation()==0 || primary.variationGroup().isEmpty())
            return primary;
        var alternatives=new ArrayList<PaletteEntry>();
        for (var e:stops) if (e.variationGroup().equals(primary.variationGroup())) alternatives.add(e);
        if (alternatives.size()<2 || SpatialNoise.unit(s.seed(),p.x(),p.y(),p.z(),2)>=s.variation()) return primary;
        return weighted(alternatives,SpatialNoise.unit(s.seed(),p.x(),p.y(),p.z(),3));
    }
    private static PaletteEntry weighted(List<PaletteEntry> entries,double roll) {
        double total=0;
        for (var entry:entries) total+=entry.weight();
        double target=roll*total;
        for (var entry:entries) { target-=entry.weight(); if (target<0) return entry; }
        return entries.getLast();
    }
}
