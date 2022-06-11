package ConvexHull;

public class Point3d extends Vector3d {

    public Point3d() {
    }

    public Point3d(
            double x,
            double y,
            double z,
            Integer r,
            Integer g,
            Integer b,
            double i,
            double c
    ) {
        set(
                x,
                y,
                z,
                r,
                g,
                b,
                i,
                c
        );
    }

    private void set(double x, double y, double z, Integer r, Integer g, Integer b, double i, double c) {
    }
    public static double getDoubleX(Point3d p) {
        return p.x;
    }
    public static double getDoubleY(Point3d p) {
        return p.y;
    }
    public static double getDoubleZ(Point3d p) {
        return p.z;
    }
    public static double getIntensity(Point3d p) {
        return p.i;
    }
}
