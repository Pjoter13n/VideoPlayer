package pl.piotrek.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VideoAdjustmentPanel {

    private final SecondScreenPlayer screenPlayer;

    public VideoAdjustmentPanel(SecondScreenPlayer screenPlayer) {
        this.screenPlayer = screenPlayer;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("🎛 Korekta obrazu");

        Slider brightnessSlider = createSlider(-100, 100, 100);
        Slider contrastSlider = createSlider(-100, 100, 100);
        Slider saturationSlider = createSlider(-100, 100, 100);

        Label brightnessLabel = new Label("🌞 Jasność:");
        Label contrastLabel = new Label("🌓 Kontrast:");
        Label saturationLabel = new Label("🎨 Nasycenie:");
//        Button resetButton = new Button("🔁 Reset");
//        resetButton.setStyle(
//            "-fx-background-color: #607D8B;" +
//            "-fx-text-fill: white;" +
//            "-fx-font-weight: bold;" +
//            "-fx-background-radius: 5px;" +
//            "-fx-padding: 8px 16px;" +
//            "-fx-cursor: hand;"
//        );

        VBox layout = new VBox(15,
            new HBox(10, brightnessLabel, brightnessSlider),
            new HBox(10, contrastLabel, contrastSlider),
            new HBox(10, saturationLabel, saturationSlider)
        );
        
//        resetButton.setOnAction(e -> {
//            brightnessSlider.setValue(100);
//            contrastSlider.setValue(100);
//            saturationSlider.setValue(100);
//            screenPlayer.resetAdjustments();
//        });

        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
//        HBox resetBox = new HBox(resetButton);
//        resetBox.setAlignment(Pos.CENTER);
//        layout.getChildren().add(resetBox);

        Scene scene = new Scene(layout, 400, 250);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();

        brightnessSlider.valueProperty().addListener((obs, o, n) -> screenPlayer.setBrightness(n.doubleValue()));
        contrastSlider.valueProperty().addListener((obs, o, n) -> screenPlayer.setContrast(n.doubleValue()));
        saturationSlider.valueProperty().addListener((obs, o, n) -> screenPlayer.setSaturation(n.doubleValue()));
    }

    private Slider createSlider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setPrefWidth(250);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(4);
        return slider;
    }
}