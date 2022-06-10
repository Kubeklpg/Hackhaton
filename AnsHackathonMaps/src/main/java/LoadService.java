import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LoadService extends Service<Void> {

    private Boolean currentState;
    private List<File> files;
    private ObjectProperty<String> currentWork = new SimpleObjectProperty<>();
    private ObjectProperty<String> fileNames = new SimpleObjectProperty<>();
    private ObjectProperty<Long> pointsCount = new SimpleObjectProperty<>();



    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {

                Platform.runLater(
                        () -> {
                            setCurrentWork("Start");
                        }
                );

                try {

                    Platform.runLater(
                            () -> {
                                setPointsCount((long) 0);
                            }
                    );

                    StringBuilder fileNamesString = new StringBuilder();
                    for (File file : files) {

                        if (file != null) {
                            fileNamesString.append(file.getName() + ", ");

                            Platform.runLater(
                                    () -> {
                                        setFileNames(fileNamesString.toString());
                                    }
                            );

                            try {
                                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                                String row = bufferedReader.readLine();
                                String[] pointArray = new String[6];
                                pointArray = row.split(",");
                                if (pointArray.length == 10) {

                                    Path path = Paths.get(file.getPath());
                                    long pointsCountTemp = Files.lines(path).count();

                                    Platform.runLater(
                                            () -> {
                                                setPointsCount(getPointsCount() + pointsCountTemp);
                                            }
                                    );


                                } else if (pointArray.length == 8){
                                    Path path = Paths.get(file.getPath());
                                    long pointsCountTemp = Files.lines(path).count();

                                    Platform.runLater(
                                            () -> {
                                                setPointsCount(getPointsCount() + pointsCountTemp);
                                            }
                                    );

                                } else {
                                    Platform.runLater(
                                            () -> {
                                                setCurrentWork("Błędny format pliku: "+file.getName());
                                            }
                                    );
                                   this.cancel();
                                }

                            } catch (FileNotFoundException ex) {
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
                Platform.runLater(
                        () -> {
                            setCurrentWork("Gotowe");
                        }
                );


                return null;
            }
        };
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


    public ObjectProperty<String> currentWorkProperty() {
        return currentWork;
    }

    public final String getCurrentWork() {
        return currentWorkProperty().get();
    }

    public final void setCurrentWork(String currentWork) {
        currentWorkProperty().set(currentWork);
    }

    public ObjectProperty<String> currentFileNamesProperty() {
        return fileNames;
    }

    public final String getFileNames() {
        return currentFileNamesProperty().get();
    }

    public final void setFileNames(String fileNames) {
        currentFileNamesProperty().set(fileNames);
    }

    public ObjectProperty<Long> currentPointsCountProperty() {
        return pointsCount;
    }

    public final Long getPointsCount() {
        return currentPointsCountProperty().get();
    }

    public final void setPointsCount(Long pointsCount) {
        currentPointsCountProperty().set(pointsCount);
    }

}
