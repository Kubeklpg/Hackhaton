import java.io.*;
import java.util.*;

import ConvexHull.Point3d;
import ConvexHull.Point3dFixed;
import ConvexHull.ConvexHull3D;
import Octree.AABB;
import Octree.Cube3d;
import Octree.Octree;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import net.morbz.minecraft.blocks.DoorBlock;
import net.morbz.minecraft.blocks.Material;
import net.morbz.minecraft.blocks.SimpleBlock;
import net.morbz.minecraft.blocks.states.Facing4State;
import net.morbz.minecraft.level.FlatGenerator;
import net.morbz.minecraft.level.GameType;
import net.morbz.minecraft.level.IGenerator;
import net.morbz.minecraft.level.Level;
import net.morbz.minecraft.world.DefaultLayers;
import net.morbz.minecraft.world.World;

public class ExportService extends Service<Void> {

    private Boolean currentState;
    private ObjectProperty<String> currentWork = new SimpleObjectProperty<>();
    private List<File> files;
    private long pointsCount;
    private long pointsExportCount;

    private List<Point3d> points3dList, points3dList_v2, pointOfBuilding;
    private Map<String, String> dictionary;
    private List<Cube3d> cube3dList;
    private List<Cube3d> cube3dListNew;
    //    private List<Cube3d> cube3dListLocalTemp;
//    private List<Cube3d> cube3dListLocal;
    private Point3d[] vertices;
    private Octree octree;
    private int counter = 0;
    private long max = 0;

    private final Stage cubesStage = new Stage();

    Group root = new Group();
    XformBox cameraXform = new XformBox();
    XformBox allXForm = new XformBox();
    PhongMaterial redMaterial, greenMaterial, blueMaterial;

    PerspectiveCamera camera = new PerspectiveCamera(true);

    private static double CAMERA_INITIAL_DISTANCE = -450;
    private static double CAMERA_INITIAL_X_ANGLE = -10.0;
    private static double CAMERA_INITIAL_Y_ANGLE = 0.0;
    private static double CAMERA_NEAR_CLIP = 0.1;
    private static double CAMERA_FAR_CLIP = 10000.0;
    private static double AXIS_LENGTH = 1000.0;
    private static double MOUSE_SPEED = 0.1;
    private static double ROTATION_SPEED = 2.0;

    double mouseStartPosX, mouseStartPosY;
    double mousePosX, mousePosY;
    double mouseOldX, mouseOldY;
    double mouseDeltaX, mouseDeltaY;

    private void handleMouse(Scene scene) {

        scene.setOnScroll(me -> {
            camera.setTranslateZ(camera.getTranslateZ() + me.getDeltaY());
        });

        scene.setOnMousePressed(me -> {
            mouseStartPosX = me.getSceneX();
            mouseStartPosY = me.getSceneY();
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        scene.setOnMouseDragged(me -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            if (me.isPrimaryButtonDown()) {
                allXForm.addRotation(-mouseDeltaX * MOUSE_SPEED * ROTATION_SPEED, Rotate.Y_AXIS);
                allXForm.addRotation(mouseDeltaY * MOUSE_SPEED * ROTATION_SPEED, Rotate.X_AXIS);
            }
        });
    }

    private void handleKeyboard(Scene scene) {
        scene.setOnKeyPressed(event -> allXForm.reset());
    }

    PhongMaterial createMaterial(Color diffuseColor, Color specularColor) {
        PhongMaterial material = new PhongMaterial(diffuseColor);
        material.setSpecularColor(specularColor);
        return material;
    }

    class XformBox extends Group {
        XformBox() {
            super();
            getTransforms().add(new Affine());
        }

        public void addRotation(double angle, Point3D axis) {
            Rotate r = new Rotate(angle, axis);
            getTransforms().set(0, r.createConcatenation(getTransforms().get(0)));
        }

        public void reset() {
            getTransforms().set(0, new Affine());
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(
                        () -> {
                            setCurrentWork("Start");
                        }
                );

                points3dList = new ArrayList<Point3d>();
                points3dList_v2 = new ArrayList<Point3d>();
                pointOfBuilding = new ArrayList<Point3d>();
                dictionary  = new HashMap<String, String>();
                counter = 0;
                currentState = true;

                // ilosc operacji do progressu
                max = pointsCount;
                double minX = 0, minY = 0, minZ = 0;
                boolean isMin = false;

                try {

                    for (File file : files) {

                        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                        String row;

                        Platform.runLater(
                                () -> {
                                    setCurrentWork("1 z 6: Dekodowanie plikow...");
                                }
                        );

                        // pomijamy naglowek

                        bufferedReader.readLine();
                        while ((row = bufferedReader.readLine()) != null) {
                            if (!currentState) return null;

                            try {

                                String[] pointArray = new String[10];
                                pointArray = row.split(",");

                                Point3d point3d = new Point3d();
                                point3d.x = Double.parseDouble(pointArray[0]);  // x
                                point3d.y = Double.parseDouble(pointArray[1]);  // y
                                point3d.z = Double.parseDouble(pointArray[2]);  // z
                                point3d.r = Integer.parseInt(pointArray[3]);    // Red
                                point3d.g = Integer.parseInt(pointArray[4]);    // Green
                                point3d.b = Integer.parseInt(pointArray[5]);    // Blue
                                point3d.i = Double.parseDouble(pointArray[8]);
                                point3d.c = Double.parseDouble(pointArray[9]);  // classification
                                if(point3d.c != 7.0){
                                    if(!isMin){
                                        minX = point3d.x;
                                        minY = point3d.y;
                                        minZ = point3d.z;
                                        isMin = true;
                                    }
                                    minX = Math.min(minX, point3d.x);
                                    minY = Math.min(minY, point3d.y);
                                    minZ = Math.min(minZ, point3d.z);

                                    points3dList.add(point3d);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            counter++;
                            updateProgress(counter, max);
                        }

                    }
                    System.out.println("Prawidlowych pkt: " + points3dList.size());

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("2 z 6: Normalizacja danych...");
                            }
                    );

                    for(int i = 0; i < points3dList.size(); i++)
                    {
                        points3dList.get(i).x = points3dList.get(i).x - minX;
                        points3dList.get(i).y = points3dList.get(i).y - minY;
                        points3dList.get(i).z = points3dList.get(i).z - minZ;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("3 z 6: Sortowanie danych...");
                            }
                    );
                    Comparator<Point3d> comp = Comparator.comparing(Point3d::getDoubleX).thenComparingDouble(Point3d::getDoubleY).thenComparingDouble(Point3d::getDoubleZ);
                    Collections.sort(points3dList, comp);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("4 z 6: Usuwanie duplikatow...");
                            }
                    );
                    Point3d point = points3dList.get(0);
                    for(int i = 1; i < points3dList.size(); i++){
                        Point3d point2 = points3dList.get(i);
                        if((int)point.x != (int)point2.x) {
                            points3dList_v2.add(point);
                            point = point2;
                        }
                        else if ((int)point.y != (int)point2.y ) {
                            points3dList_v2.add(point);
                            point = point2;
                        }
                        else if ((int)point.z != (int)point2.z) {
                            points3dList_v2.add(point);
                            point = point2;
                        }
                        else {
                            point = point2;
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("5 z 6: Tworzenie listy budynkow...");
                            }
                    );
                    for(int i = 0; i < points3dList_v2.size(); i++){
                        Point3d point = points3dList_v2.get(i);
                        if(point.c == 6.0) pointOfBuilding.add(point);
                    }
                    System.out.println(pointOfBuilding);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    Platform.runLater(
                            () -> {
                                setCurrentWork("5 z 6: Tworzenie mapy...");
                            }
                    );

                    DefaultLayers layers = new DefaultLayers();
                    layers.setLayer(-64, Material.BEDROCK);
                    IGenerator generator = new FlatGenerator(layers);
                    Level level = new Level("HackathonMap", generator);
                    level.setGameType(GameType.CREATIVE);
                    level.setMapFeatures(false);

                    World world = new World(level, layers);

                    // Create a huge structure of glass that has an area of 100x100 blocks and is 50 blocks height.
                    // On top of the glass structure we have a layer of grass.
                    level.setSpawnPoint(0, (int) (minZ+100.0), 0);
                    /* for (int x = 0; x < 400; x++) {
                        for (int z = 0; z < 400; z++) {
                            // Set glass
                            for (int y = -63; y < -43; y++) {
                                world.setBlock(x, y, z, SimpleBlock.GLASS);
                            }
                            // Set grass
                            world.setBlock(x, -43, z, SimpleBlock.GLASS_PANE);
                        }
                    } */
                    double min=1000;

                    int[][] arr = Indentity(points3dList);
                    for (int i = 0; i < points3dList.size(); i++) {
                        Point3d point = points3dList_v2.get(i);
                        GenBlocks(point,world,arr);

                        if(point.z < min) min=point.z;
                        //world.setBlock((int) point.x, (int) point.z, (int) point.y * -1, SimpleBlock.GRASS);
                    }
                    for(int i = 0; i < pointOfBuilding.size(); i++){
                        Point3d point = pointOfBuilding.get(i);
                        for(int j = 0; j < 100; j++){
                            world.setBlock((int) point.x, (int) point.z - 1 - j , (int) point.y * -1, SimpleBlock.BRICK_BLOCK);
                        }
                    }


                    world.save();

                } catch (Throwable e) {
                    e.printStackTrace();
                }

                Platform.runLater(
                        () -> {
                            setCurrentWork(" READY :)");
                        }
                );
                return null;
            }

            public int[][] findMaxZ(List<Point3d> pointsList){
                int[][] maxZ = new int[5000000][3];

                Point3d point = pointsList.get(0);
                maxZ[0][0] = (int) point.x;
                maxZ[0][1] = (int) point.y;
                maxZ[0][2] = (int) point.z;

                for(int i = 1; i < pointsList.size(); i++){
                    point = pointsList.get(i);
                    for(int j = 0; j < pointsList.size(); j++){
                        if((int) point.x == maxZ[j][0] && (int) point.y == maxZ[j][1]){
                            maxZ[j][2] = Math.max(maxZ[j][2], (int) point.z);
                        }
                    }
                }

                return maxZ;
            }
        };

    }

    public static class Sortbyroll implements Comparator<Point3d>
    {
        // Used for sorting in ascending order of
        // roll number
        public int compare(Point3d a, Point3d b)
        {
            int aX, bX, aY, bY;
            aX = (int) a.x;
            bX = (int) b.x;
            aY = (int) a.y;
            bY = (int) b.y;

            if(aX != bX) return (int) (a.x - b.x);
            else if(aY != bY) return (int) (a.y - b.y);
            else return (int) (a.z - b.z);
        }
    }

    public Boolean getCurrentState() {
        return currentState;
    }

    public void setCurrentState(Boolean stateNew) {
        this.currentState = stateNew;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void setPointsCount(long pointsCount) {
        this.pointsCount = pointsCount;
    }


    public ObjectProperty<String> currentWorkProperty() {
        return currentWork;
    }

    public final String getCurrentWork() {
        return currentWorkProperty().get();
    }

    public final void setCurrentWork(String currentWork) {
        currentWorkProperty().set(currentWork);
    }

    public long getPointsExportCount() {
        return pointsExportCount;
    }

    public void setPointsExportCount(long pointsExportCount) {
        this.pointsExportCount = pointsExportCount;
    }


    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public void GenBlocks(Point3d point,World world,int[][] arr){
        int x = (int)point.x;
        int y = (int)point.y;
        int z = (int)point.z;

        int cat = (int)point.c;
        switch (cat){

            case 2:

                int diff = arr[2][1]/40;
                if(point.i > arr[2][0] && point.i < arr[2][0] +39* diff){

                    world.setBlock(x,z,y*-1,SimpleBlock.COBBLESTONE);

                }else world.setBlock(x,z,y*-1,SimpleBlock.GRASS);

                break;
            case 3:
                world.setBlock(x,z,y*-1,SimpleBlock.SAPLING);
                break;
            case 4:
                world.setBlock(x,z,y*-1,SimpleBlock.LOG);
                break;
            case 5:
                world.setBlock(x,z,y*-1,SimpleBlock.LEAVES);
                break;
            case 6:
                int differ = arr[6][1]/20;
                if(point.i > arr[6][0] && point.i < arr[6][0] + 18*differ) {
                    world.setBlock(x, z, y * -1, SimpleBlock.BRICK_BLOCK);
                }else world.setBlock(x, z, y * -1, SimpleBlock.STONE);
                break;
                /*

            case 9:
                world.setBlock(x,z,y*-1,SimpleBlock.WATER);
                break;

                 */


        }
    }

    public int[][] Indentity(List<Point3d> points3dList){
        int[][] arr = new int[9][2];
        for(int i = 0 ; i < arr.length ; i ++){
            arr[i][0]=1000000;
            arr[i][1]=0;
        }
        for(int i = 0 ; i < points3dList.size();i++) {
            Point3d point = points3dList.get(i);
            if(point.c == 2.0){
                arr[2][0] = Math.min(arr[2][0],(int)point.i);
                arr[2][1] = Math.max(arr[2][0],(int)point.i);
                double leng = arr[2][1]-arr[2][0];
                arr[2][1]= (int)leng;
            } else if (point.c == 6.0) {
                arr[6][0] = Math.min(arr[6][0],(int)point.i);
                arr[6][1] = Math.max(arr[6][0],(int)point.i);
                double leng = arr[6][1]-arr[6][0];
                arr[6][1]= (int)leng;
            }

        }

        return arr;
    }
}
