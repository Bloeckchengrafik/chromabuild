package dev.chromabuild.core;

/** Stateless world-space noise; sampling does not depend on iteration order. */
public final class SpatialNoise {
    private SpatialNoise() { }
    public static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xbf58476d1ce4e5b9L;
        z = (z ^ (z >>> 27)) * 0x94d049bb133111ebL;
        return z ^ (z >>> 31);
    }
    public static double unit(long seed, long x, long y, long z, long salt) {
        long h = mix(seed ^ salt ^ mix(x) ^ Long.rotateLeft(mix(y), 21) ^ Long.rotateLeft(mix(z), 42));
        return (h >>> 11) * 0x1.0p-53;
    }
    private static double fade(double t) { return t*t*(3-2*t); }
    private static double lerp(double a, double b, double t) { return a+(b-a)*t; }
    public static double smooth3(long seed, double x, double y, double z) {
        long ix=(long)Math.floor(x), iy=(long)Math.floor(y), iz=(long)Math.floor(z);
        double fx=fade(x-Math.floor(x)), fy=fade(y-Math.floor(y)), fz=fade(z-Math.floor(z));
        double[] v = new double[8];
        for (int dx=0;dx<2;dx++) for (int dy=0;dy<2;dy++) for (int dz=0;dz<2;dz++)
            v[(dx<<2)|(dy<<1)|dz]=unit(seed,ix+dx,iy+dy,iz+dz,0x6e6f697365L)*2-1;
        return lerp(lerp(lerp(v[0],v[4],fx),lerp(v[2],v[6],fx),fy),
                    lerp(lerp(v[1],v[5],fx),lerp(v[3],v[7],fx),fy),fz);
    }
}
