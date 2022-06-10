package ConvexHull;

public class Point3dFixed extends Vector3d {
    public Point3dFixed(){

    }

    public Point3dFixed(Point3d point){
        this.x = (int) point.x;
        this.y = (int) point.y;
        this.z = (int) point.z;
        this.c = (int) point.c;
    }
}
