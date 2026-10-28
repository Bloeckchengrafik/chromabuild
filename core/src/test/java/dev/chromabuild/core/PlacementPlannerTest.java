package dev.chromabuild.core;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PlacementPlannerTest {
    private static Palette palette() { return new Palette(List.of(
        new PaletteEntry("minecraft:stone",60,0,true,""),
        new PaletteEntry("minecraft:calcite",40,1,true,""))); }
    @Test void projectionClampsAndRejectsZeroLength() {
        var a=new GradientAxis(new BlockPosition(0,0,0),new BlockPosition(0,10,0));
        assertEquals(0,a.position(new BlockPosition(0,-9,0)));
        assertEquals(.5,a.position(new BlockPosition(0,5,0)));
        assertEquals(1,a.position(new BlockPosition(0,20,0)));
        assertThrows(IllegalArgumentException.class,()->new GradientAxis(new BlockPosition(0,0,0),new BlockPosition(0,0,0)));
    }
    @Test void selectionIndependentOfIterationOrder() {
        var points=new ArrayList<BlockPosition>();
        for(int x=0;x<16;x++)for(int z=0;z<16;z++)points.add(new BlockPosition(x,40,z));
        var s=new PlacementSettings(PlacementMode.WEIGHTED_RANDOM,42,0,4,0,0);
        var first=PlacementPlanner.generate(points,palette(),s,null).assignments();
        java.util.Collections.reverse(points);
        var second=PlacementPlanner.generate(points,palette(),s,null).assignments();
        var map=new java.util.HashMap<BlockPosition,String>();
        second.forEach(a->map.put(a.position(),a.blockStateId()));
        first.forEach(a->assertEquals(a.blockStateId(),map.get(a.position())));
    }
    @Test void weightedDistributionIsPlausible() {
        int stone=0;
        var s=new PlacementSettings(PlacementMode.WEIGHTED_RANDOM,99,0,4,0,0);
        for(int x=0;x<100;x++)for(int z=0;z<100;z++) {
            var one=PlacementPlanner.generate(List.of(new BlockPosition(x,0,z)),palette(),s,null);
            if(one.assignments().getFirst().blockStateId().equals("minecraft:stone"))stone++;
        }
        assertTrue(stone>5600 && stone<6400,"stone count: "+stone);
    }
    @Test void organicGenerationIsStable() {
        var p=List.of(new BlockPosition(4,10,5),new BlockPosition(5,10,5));
        var a=new GradientAxis(new BlockPosition(0,0,0),new BlockPosition(10,20,10));
        var s=new PlacementSettings(PlacementMode.ORGANIC_GRADIENT,123,0.35,5,0.2,0.4);
        assertEquals(PlacementPlanner.generate(p,palette(),s,a),PlacementPlanner.generate(p,palette(),s,a));
    }
    @Test void duplicateLocationsRejected() {
        var p=new BlockPosition(0,0,0);
        var s=new PlacementSettings(PlacementMode.WEIGHTED_RANDOM,0,0,1,0,0);
        assertThrows(IllegalArgumentException.class,()->PlacementPlanner.generate(List.of(p,p),palette(),s,null));
    }
}
