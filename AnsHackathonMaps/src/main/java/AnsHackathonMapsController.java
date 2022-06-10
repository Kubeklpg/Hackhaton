import com.jfoenix.controls.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;


public class AnsHackathonMapsController implements Initializable {

    @FXML
    private StackPane stackPane;
    @FXML
    private JFXButton pathButton;
    @FXML
    private JFXButton convertButton;
    @FXML
    private JFXButton openFolderButton;
    @FXML
    private JFXButton stopButton;
    @FXML
    private JFXTextArea fileNames;
    @FXML
    private Label pointsCountLabel;

    @FXML
    private Label percentLabel;
    @FXML
    private Label workLabel;
    @FXML
    private Label heapSizeLabel;
    @FXML
    private Label maxHeapSizeLabel;

    @FXML
    private JFXProgressBar progressBar;
    @FXML
    private JFXSpinner filesSpinner;
    @FXML
    private JFXSpinner taskSpinner;
    @FXML
    private JFXDialog infoDialog;
    @FXML
    private Label infoHeader;
    @FXML
    private Label infoBody;
    @FXML
    private JFXButton infoButtonAccept;

    private List<File> files;
    private Long pointsCount;
    private ExportService exportService;
    private LoadService loadService;
    private Boolean readyToExport = true;
    static Stage toastStage;

    @FXML
    protected void handlePathButtonAction(ActionEvent event) {

        files = new ArrayList<>();
        Window owner = pathButton.getScene().getWindow();
        toastStage = (Stage) owner.getScene().getWindow();

        FileChooser filesChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XYZ files (*.xyz)", "*.xyz");
        filesChooser.getExtensionFilters().add(extFilter);
        //file = filesChooser.showOpenDialog(owner);

        files = filesChooser.showOpenMultipleDialog(owner);

        if (files != null) {
            readyToExport = true;
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    filesSpinner.setVisible(true);
                }
            });
            loadService = new LoadService();
            loadService.setFiles(files);
            loadService.setOnSucceeded((WorkerStateEvent t) -> {
                SucssesToast("Load info", "Pliki wczytano poprawnie.");

                pointsCount = loadService.getPointsCount();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        filesSpinner.setVisible(false);

                    }
                });
                convertButton.setDisable(false);


            });
            loadService.setOnFailed((WorkerStateEvent t) -> {
                ErrorToast("Load error", "Wczytywanie przerwane.");
            });
            loadService.setOnCancelled((WorkerStateEvent t) -> {
                loadService.setCurrentState(false);
                NoticeToast("Load info", "Wczytywanie anulowane.");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        filesSpinner.setVisible(false);
                        readyToExport = false;
                        infoDialog.setDialogContainer(stackPane);
                        infoHeader.setText("UWAGA");
                        infoBody.setText("Napotkano niepoprawny format pliku.");
                        infoDialog.show();
                        infoButtonAccept.setOnAction(ex -> {
                            infoDialog.close();
                        });
                    }
                });

            });

            loadService.setOnRunning((WorkerStateEvent t) -> {
            });
            pointsCountLabel.textProperty().bind(loadService.currentPointsCountProperty().asString());
            fileNames.textProperty().bind(loadService.currentFileNamesProperty());
            loadService.start();

        } else {
            resetInterface();
        }
    }


    @FXML
    protected void handleStopButtonAction(ActionEvent actionEvent) {
        exportService.cancel();
        exportService.setCurrentState(false);
    }

    @FXML
    protected void handleConvertButtonAction(ActionEvent actionEvent) {
        if (readyToExport) {
                Window owner = convertButton.getScene().getWindow();
                toastStage = (Stage) owner.getScene().getWindow();
                openFolderButton.setVisible(false);
                progressBar.setVisible(true);
                workLabel.setVisible(true);
                pathButton.setDisable(true);
                convertButton.setDisable(true);

                exportService = new ExportService();
                exportService.setFiles(files);
                exportService.setPointsCount(pointsCount);
                exportService.setOnSucceeded((WorkerStateEvent t) -> {
                    stopButton.setVisible(false);
                    openFolderButton.setVisible(true);
                    convertButton.setDisable(false);
                    pathButton.setDisable(false);
                    taskSpinner.setVisible(false);
                    SucssesToast("Converter info", "Punkty wyeksportowano poprawnie.");
                });
                exportService.setOnFailed((WorkerStateEvent t) -> {
                    taskSpinner.setVisible(false);
                    stopButton.setVisible(false);
                    ErrorToast("Converter error", "Eksport przerwany.");
                });
                exportService.setOnCancelled((WorkerStateEvent t) -> {
                    taskSpinner.setVisible(false);
                    stopButton.setVisible(false);
                    convertButton.setDisable(false);
                    pathButton.setDisable(false);
                    taskSpinner.setVisible(false);
                    NoticeToast("Converter info", "Eksport anulowany.");
                });

                exportService.setOnRunning((WorkerStateEvent t) -> {
                    taskSpinner.setVisible(true);
                    stopButton.setVisible(true);
                });

                progressBar.progressProperty().bind(exportService.progressProperty());
                percentLabel.textProperty().bind(exportService.progressProperty().multiply(100).asString("%.2f %%"));
                workLabel.textProperty().bind(exportService.currentWorkProperty());
                exportService.start();
        } else {

        }
    }

    @FXML
    public void handleOpenFolderButtonAction(ActionEvent actionEvent) throws IOException {
        String myDocuments = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
        File dirMain = new File(myDocuments + "\\" + "ANS Hackhathon 2022 Map Generator");
        if (!dirMain.exists()) {
            dirMain.mkdir();
        }
        Desktop.getDesktop().open(dirMain);

    }




    private void resetInterface() {
        progressBar.setVisible(false);
        convertButton.setDisable(true);
        openFolderButton.setVisible(false);
        stopButton.setVisible(false);
        workLabel.setVisible(false);
        filesSpinner.setVisible(false);
        taskSpinner.setVisible(false);
        fileNames.textProperty().unbind();
        fileNames.setText("");
        pointsCountLabel.textProperty().unbind();
        pointsCountLabel.setText("0");
        percentLabel.textProperty().unbind();
        percentLabel.setText("");
        workLabel.textProperty().unbind();
        workLabel.setText("");
        pointsCount = (long) 0;

    }

    private void checkHeapSize() {
        Long heapsize = Runtime.getRuntime().totalMemory();

        Double bytes = (double) heapsize;
        Double bytesK = bytes / 1024.0;
        Double bytesM = (bytes / 1024.0) / 1024.0;
        Double bytesG = ((bytes / 1024.0) / 1024.0) / 1024.0;
        Double bytesT = (((bytes / 1024.0) / 1024.0) / 1024.0) / 1024.0;

        DecimalFormat dec = new DecimalFormat("0.00");

        Platform.runLater(() -> {
            if (bytesT > 1) {
                heapSizeLabel.setText(dec.format(bytesT).concat(" TB"));
            } else if (bytesG > 1) {
                heapSizeLabel.setText(dec.format(bytesG).concat(" GB"));
            } else if (bytesM > 1) {
                heapSizeLabel.setText(dec.format(bytesM).concat(" MB"));
            } else if (bytesK > 1) {
                heapSizeLabel.setText(dec.format(bytesK).concat(" KB"));
            } else {
                heapSizeLabel.setText(dec.format(bytes).concat(" Bytes"));
            }

        });

        Long heapMaxSize = Runtime.getRuntime().maxMemory();

        Double mbytes = (double) heapMaxSize;
        Double mbytesK = mbytes / 1024.0;
        Double mbytesM = (mbytes / 1024.0) / 1024.0;
        Double mbytesG = ((mbytes / 1024.0) / 1024.0) / 1024.0;
        Double mbytesT = (((mbytes / 1024.0) / 1024.0) / 1024.0) / 1024.0;

        DecimalFormat mdec = new DecimalFormat("0.00");

        Platform.runLater(() -> {
            if (mbytesT > 1) {
                maxHeapSizeLabel.setText(mdec.format(mbytesT).concat(" TB"));
            } else if (mbytesG > 1) {
                maxHeapSizeLabel.setText(mdec.format(mbytesG).concat(" GB"));
            } else if (mbytesM > 1) {
                maxHeapSizeLabel.setText(mdec.format(mbytesM).concat(" MB"));
            } else if (mbytesK > 1) {
                maxHeapSizeLabel.setText(mdec.format(mbytesK).concat(" KB"));
            } else {
                maxHeapSizeLabel.setText(mdec.format(mbytes).concat(" Bytes"));
            }

        });
    }

    static public Tooltip NoticeToast(String Title, String Txt) {
        return JToast(Title, Txt, "rgba(255,255,255,0.8)", "#222", Duration.seconds(7));
    }

    static public Tooltip SucssesToast(String Title, String Txt) {
        return JToast(Title, Txt, "rgba(15, 157, 88, 0.8)", "#222", Duration.seconds(7));
    }

    static public Tooltip ErrorToast(String Title, String Txt) {
        return JToast(Title, Txt, "rgba(255, 25, 36, 0.8)", "#fff", Duration.seconds(15));
    }

    public static Tooltip JToast(String Title, String Text, String BackgroundColor, String Color, Duration duration) {
        Tooltip S = new Tooltip();
        Label X = new Label("X");
        X.setId("Close");
        X.setOnMouseClicked(value -> {
            S.hide();
        });

        Label Content = new Label(Text);
        Content.setAlignment(Pos.TOP_RIGHT);
        Content.setTextAlignment(TextAlignment.RIGHT);
        Content.setWrapText(true);
        Content.setId("Content");

        double MaxWidth = 800;
        Content.setMaxWidth(MaxWidth);

        Label TitleLable = new Label(Title);
        TitleLable.setAlignment(Pos.TOP_RIGHT);
        Content.setStyle("-fx-text-fill:" + Color);
        TitleLable.setStyle("-fx-text-fill:" + Color);

        GridPane GB = new GridPane();
        double Width = 0;
        if (Content.getText() != null) {
            Width = 150 + Content.getText().length() / 2.5;
        }
        if (Width > MaxWidth) {
            Width = MaxWidth;
        }
        double height = Content.getPrefHeight();

        GB.getColumnConstraints().setAll(new ColumnConstraints(30, 30, 30, Priority.NEVER, HPos.LEFT, true), new ColumnConstraints(Width, Width, MaxWidth, Priority.ALWAYS, HPos.RIGHT, true));

        GB.getRowConstraints().setAll(new RowConstraints(30, 30, 30), new RowConstraints(height, height, 750, Priority.ALWAYS, VPos.CENTER, true));
        GB.setId("msgtipbox");
        GB.setStyle("-fx-background-color:" + BackgroundColor);
        GB.setVgap(10);
        GB.setHgap(10);
        GB.add(X, 0, 0);
        GB.add(TitleLable, 1, 0);
        GB.add(Content, 0, 1, 2, 1);
        GridPane.setVgrow(Content, Priority.ALWAYS);
        GridPane.setHgrow(Content, Priority.ALWAYS);
        S.setGraphic(GB);
        S.setId("msg-tip");
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double y = screenBounds.getMinY() + screenBounds.getHeight();
        S.show(toastStage, 0, y);

        ////// Show and Hide
        ////// Still showing in Hover Support
        SimpleBooleanProperty HoveProperty = new SimpleBooleanProperty(false);
        GB.setOnMouseEntered(v -> HoveProperty.set(true));
        GB.setOnMouseExited(v -> HoveProperty.set(false));
        PauseTransition wait = new PauseTransition(duration);
        wait.setOnFinished((e) -> {
            if (HoveProperty.get()) {
                wait.play();
            } else {
                S.hide();
            }
        });
        wait.play();

        HoveProperty.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            wait.playFromStart();
        });
        return S;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        checkHeapSize();

        toastStage = new Stage();
        resetInterface();

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            int newLength = change.getControlNewText().length();
            if (newLength > 19) {
                String tail = change.getControlNewText().substring(0, 19);
                change.setText(tail);
                // replace the range to complete text
                // valid coordinates for range is in terms of old text
                int oldLength = change.getControlText().length();
                change.setRange(0, oldLength);
            }
            String input = change.getText();
            if (input.matches("[0-9]*")) {
                return change;
            }
            return null;
        };


    }


}
