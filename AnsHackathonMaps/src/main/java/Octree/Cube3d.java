package Octree;

import ConvexHull.Point3d;

import java.util.ArrayList;
import java.util.List;

public class Cube3d {

    public double minX, minY, minZ, maxX, maxY, maxZ;
    private Integer mineX;
    private Integer mineY;
    private Integer mineZ;
    private List<Point3d> point3dsList = new ArrayList<Point3d>();
    private List<Integer> classificationList = new ArrayList<Integer>();

    private Integer mostClassification = 0;
    private Integer mostClassificationCount = 0;

    public Cube3d(double _minX, double _maxX, double _minY, double _maxY, double _minZ, double _maxZ) {
        this.minX = _minX;
        this.maxX = _maxX;
        this.minY = _minY;
        this.maxY = _maxY;
        this.minZ = _minZ;
        this.maxZ = _maxZ;
    }

    public boolean ifInCube(double x, double y, double z) {
        return (x <= maxX && x >= minX) && (y <= maxY && y >= minY) && (z <= maxZ && z >= minZ);
    }

    public void addPoint(Point3d point) {
        point3dsList.add(point);
    }
    public void addClassification(Double classification) {
        classificationList.add(classification.intValue());
    }

    public void removePoint(int i) {
        point3dsList.remove(i);
    }

    public Point3d getPoint(int i) {
        return  point3dsList.get(i);
    }

    public int getPointsSize(){
        return point3dsList.size();
    }

    public List<Point3d> getPoint3dsList() {
        return point3dsList;
    }

    public void setPoint3dsList(List<Point3d> point3dsList) {
        this.point3dsList = point3dsList;
    }

    public Integer getMineX() {
        return mineX;
    }

    public void setMineX(Integer mineX) {
        this.mineX = mineX;
    }

    public Integer getMineY() {
        return mineY;
    }

    public void setMineY(Integer mineY) {
        this.mineY = mineY;
    }

    public Integer getMineZ() {
        return mineZ;
    }

    public void setMineZ(Integer mineZ) {
        this.mineZ = mineZ;
    }

    public List<Integer> getClassificationList() {
        return classificationList;
    }

    public void setClassificationList(List<Integer> classificationList) {
        this.classificationList = classificationList;
    }

    public Integer getMostClassification() {
        return mostClassification;
    }

    public void setMostClassification(Integer mostClassification) {
        this.mostClassification = mostClassification;
    }

    public Integer getMostClassificationCount() {
        return mostClassificationCount;
    }

    public void setMostClassificationCount(Integer mostClassificationCount) {
        this.mostClassificationCount = mostClassificationCount;
    }
}
