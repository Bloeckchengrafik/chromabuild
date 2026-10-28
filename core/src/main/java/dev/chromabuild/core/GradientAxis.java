package dev.chromabuild.core;

/** Clamped projection onto a nonzero world-space segment. */
public record GradientAxis(BlockPosition start, BlockPosition end) {
    public GradientAxis {
        if (start == null || end == null || start.equals(end))
            throw new IllegalArgumentException("gradient endpoints must be distinct");
    }
    public double position(BlockPosition point) {
        double dx = (double) end.x() - start.x();
        double dy = (double) end.y() - start.y();
        double dz = (double) end.z() - start.z();
        double dot = ((double)point.x()-start.x())*dx + ((double)point.y()-start.y())*dy + ((double)point.z()-start.z())*dz;
        return Math.clamp(dot/(dx*dx+dy*dy+dz*dz), 0, 1);
    }
}
