package dev.chromabuild.core;

/** Stateless world-space noise; sampling is independent of iteration order. */
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
    private static double sample(long seed, long x, long y, long z) {
        return 2*unit(seed,x,y,z,0x6e6f697365L)-1;
    }

    /** Trilinearly interpolated smooth value noise in [-1,1], with no per-sample arrays. */
    public static double smooth3(long seed, double x, double y, double z) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
            throw new IllegalArgumentException("coordinates must be finite");
        long ix=(long)Math.floor(x), iy=(long)Math.floor(y), iz=(long)Math.floor(z);
        double fx=fade(x-Math.floor(x)), fy=fade(y-Math.floor(y)), fz=fade(z-Math.floor(z));
        double low = lerp(
            lerp(lerp(sample(seed,ix,iy,iz),sample(seed,ix+1,iy,iz),fx),
                 lerp(sample(seed,ix,iy+1,iz),sample(seed,ix+1,iy+1,iz),fx),fy),
            lerp(lerp(sample(seed,ix,iy,iz+1),sample(seed,ix+1,iy,iz+1),fx),
                 lerp(sample(seed,ix,iy+1,iz+1),sample(seed,ix+1,iy+1,iz+1),fx),fy),fz);
        return low;
    }
}
