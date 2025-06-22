package pl.piotrek.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class EqualizerPanel {

    private final SecondScreenPlayer screenPlayer;
    private static final int BAND_COUNT = 9;
    private static final String[] BAND_LABELS = {
        "60Hz", "170Hz", "310Hz", "600Hz", "1KHz", "3KHz", "6KHz", "12KHz", "14KHz"
    };

    public EqualizerPanel(SecondScreenPlayer screenPlayer) {
        this.screenPlayer = screenPlayer;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("🎵 Equalizer");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);


        String[] labels = {"60Hz", "170Hz", "310Hz", "600Hz", "1KHz"};
        int[] bandIndices = {0, 1, 2, 3, 4};

        for (int i = 0; i < BAND_COUNT; i++) {
            final int bandIndex = i;
            Label label = new Label(BAND_LABELS[i]);

            Slider slider = new Slider(-20, 20, 0);
            styleSlider(slider);

            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                screenPlayer.setEqualizerBand(bandIndex, newVal.floatValue());
            });

            layout.getChildren().add(new HBox(10, label, slider));
        }
        Scene scene = new Scene(layout, 400, 300);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }
    private String getLabelForBand(int index) {
        String[] labels = {"60Hz", "170Hz", "310Hz", "600Hz", "1KHz", "3KHz", "6KHz", "12KHz", "14KHz", "16KHz"};
        return index >= 0 && index < labels.length ? labels[index] : (index * 100 + "Hz");
    }

    private void styleSlider(Slider slider) {
        slider.setPrefWidth(250);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(1);
        slider.setBlockIncrement(1);
    }
}