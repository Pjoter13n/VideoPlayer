package pl.piotrek.main;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {
	

    public FilmLibrary filmLibrary;
    private Label currentTimeLabel;
    private Label durationLabel;
    private Label nowPlayingLabel;
    private Slider timeSlider;
    private Slider volumeSlider;
    private TextField startTimeField;


    public ListView<Film> filmListView;
    IntroAdManagerPanel adManagerPanel = new IntroAdManagerPanel();
    @Override
    public void start(Stage primaryStage) {
    	System.setProperty("jna.library.path", "vlc");

        filmLibrary = new FilmLibrary();


        BorderPane root = new BorderPane();
        filmListView = new ListView<>();
        filmListView.setPrefWidth(400);
        filmListView.setPadding(new Insets(10));
        filmListView.setStyle("""
            -fx-background-color: white;
            -fx-border-color: #ccc;
            -fx-border-radius: 5px;
            -fx-background-radius: 5px;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.1, 0, 2);
        """);

        root.setStyle("-fx-background-color: #f9f9f9;"); 
        root.setCenter(filmListView);


        Button addButton = createStyledButton("➕ Dodaj film", "#4CAF50");
        Button playButton = createStyledButton("▶ Odtwórz", "#2196F3");
        Button pauseButton = createStyledButton("⏸ Pauza", "#FFC107");
        Button stopButton = createStyledButton("⏹ Stop", "#F44336");
        Button adjustButton = createStyledButton("🎛 Korekta obrazu", "#9C27B0");
//        Button equalizerButton = createStyledButton("🎵 Equalizer", "#3F51B5");
        Button adManagerButton = createStyledButton("📺 Zarządzaj reklamami", "#795548");
        Button helpButton = new Button("❓ Pomoc");

        
        
        
        SecondScreenPlayer screenPlayer = new SecondScreenPlayer();
        startTimeField = new TextField();
        startTimeField.setPromptText("Czas startu (mm:ss)");
        startTimeField.setPrefWidth(150);
        startTimeField.setStyle("-fx-font-size: 14px;");

        

        playButton.setOnAction(e -> {
            Film selected = filmListView.getSelectionModel().getSelectedItem();
            if (selected != null) {

                long startTimeMillis = parseTimeToMillis(startTimeField.getText());

                List<Path> allPreVideos = new ArrayList<>();
                allPreVideos.addAll(adManagerPanel.getAdVideos());
                allPreVideos.addAll(adManagerPanel.getIntroVideos());
                allPreVideos.add(selected.getFilePath());

                final int[] index = {0}; 

                Runnable playNext = new Runnable() {
                    @Override
                    public void run() {
                        if (index[0] < allPreVideos.size()) {
                            Path video = allPreVideos.get(index[0]);

                            // Reset UI przy starcie nowego video:
                            Platform.runLater(() -> {
                                timeSlider.setValue(0);
                                currentTimeLabel.setText("⏱ 00:00:00");
                                durationLabel.setText("⏲ --:--:--");
                                timeSlider.setDisable(true);
                            });

                            screenPlayer.playOnSecondScreen(video.toString());

                            if (index[0] == allPreVideos.size() - 1 && startTimeMillis > 0) {
                                new Thread(() -> {
                                    try {
                                        Thread.sleep(1000);
                                        screenPlayer.seek(startTimeMillis);
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }
                                }).start();
                            }

                            // Po jakimś czasie (np. 1.2 s) aktualizujemy max slider i duration
                            Timeline updateDuration = new Timeline(new KeyFrame(Duration.seconds(1.2), ev -> {
                                long totalMillis = screenPlayer.getMediaDuration();
                                if (totalMillis > 0) {
                                    durationLabel.setText("⏲ " + formatTime(totalMillis));
                                    timeSlider.setMax(totalMillis);
                                    timeSlider.setDisable(false);
                                }
                            }));
                            updateDuration.setCycleCount(1);
                            updateDuration.play();

                            index[0]++;
                        }
                    }
                };

                screenPlayer.setOnEndOfMedia(() -> {
                    playNext.run();
                });

                playNext.run();
                nowPlayingLabel.setText("🎞 Aktualnie: " + selected.getTitle());

                // Czekamy na załadowanie media i pobieramy czas tylko jeśli większy od 0
                Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                    long totalMillis = screenPlayer.getMediaDuration();
                    if (totalMillis > 0) {
                        durationLabel.setText("⏲ " + formatTime(totalMillis));
                        timeSlider.setMax(totalMillis);
                        timeSlider.setDisable(false);
                    }
                }));
                delay.setCycleCount(1);
                delay.play();

                // Aktualizacja czasu co 0.5 sekundy, uruchamiamy po opóźnieniu 1.5s, żeby mieć pewność, że media są załadowane
                Timeline currentTimeUpdater = new Timeline(new KeyFrame(Duration.seconds(0.5), ev -> {
                    long currentMillis = screenPlayer.getCurrentTime();
                    timeSlider.setValue(currentMillis);
                    currentTimeLabel.setText("⏱ " + formatTime(currentMillis));
                }));
                currentTimeUpdater.setCycleCount(Animation.INDEFINITE);
                currentTimeUpdater.play();

                new Timeline(new KeyFrame(Duration.seconds(1.5), ev -> currentTimeUpdater.play())).play();
            }
        });
        


        addButton.setOnAction(e -> addFilm(primaryStage));
        pauseButton.setOnAction(e -> screenPlayer.pause());
        stopButton.setOnAction(e -> screenPlayer.stop());
        HBox controls = new HBox(15, addButton, playButton, pauseButton, stopButton, adjustButton, adManagerButton,helpButton, 
        	    new Label("Czas startu:"), startTimeField);
        currentTimeLabel = new Label("⏱ 00:00");
        durationLabel = new Label("⏲ 00:00");
        timeSlider = new Slider(0, 100, 0); 
        timeSlider.setPrefWidth(400);
        timeSlider.setDisable(true); 
        volumeSlider = new Slider(0, 100, 50); 
        volumeSlider.setPrefWidth(150);

        Label volumeLabel = new Label("🔊 Głośność:");
        volumeLabel.setMinWidth(80);

        HBox volumeBox = new HBox(10, volumeLabel, volumeSlider);
        volumeBox.setAlignment(Pos.CENTER);
        HBox timeSliderBox = new HBox(10, new Label("⏩ Przewijanie:"), timeSlider);
        timeSliderBox.setAlignment(Pos.CENTER);

        HBox timeBox = new HBox(10, currentTimeLabel, new Label("/"), durationLabel);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.setPadding(new Insets(10));
        controls.setPadding(new Insets(15));
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-background-color: #eeeeee; -fx-border-color: #ddd;");
        nowPlayingLabel = new Label("🎞 Brak odtwarzanego filmu");
        nowPlayingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nowPlayingLabel.setTextFill(Color.web("#333"));
        nowPlayingLabel.setPadding(new Insets(5));
        nowPlayingLabel.setAlignment(Pos.CENTER);
        nowPlayingLabel.setMaxWidth(Double.MAX_VALUE);
        VBox bottomBox = new VBox(10, nowPlayingLabel, timeBox, timeSliderBox, volumeBox, controls);
        bottomBox.setPadding(new Insets(15));
        bottomBox.setStyle("-fx-background-color: #eeeeee; -fx-border-color: #ddd;");
        root.setBottom(bottomBox);
        timeSlider.setOnMouseReleased(e -> {
            long seekTime = (long) timeSlider.getValue();
            screenPlayer.seek(seekTime);
        });
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            screenPlayer.setVolume(newVal.intValue());
        });
        
        adjustButton.setOnAction(e -> {
            VideoAdjustmentPanel adjustmentPanel = new VideoAdjustmentPanel(screenPlayer);
            adjustmentPanel.show();
        });
//        equalizerButton.setOnAction(e -> {
//            EqualizerPanel eqPanel = new EqualizerPanel(screenPlayer);
//            eqPanel.show();
//        });
        helpButton.setOnAction(e -> HelpDialog.show());


        adManagerButton.setOnAction(e -> adManagerPanel.show());


        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("🎬 Kino Player - Projekt Piotr Nowak");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("🔴 Zamknięto panel – zatrzymuję film");
            if(screenPlayer.getMediaPlayer() !=null) {
            screenPlayer.stop();}
        });
    }
    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    

    private long parseTimeToMillis(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            return 0;
        }
        
        try {
            String[] parts = timeString.split(":");
            if (parts.length == 2) {
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return (minutes * 60L + seconds) * 1000L;
            } else if (parts.length == 1) {
                int seconds = Integer.parseInt(parts[0]);
                return seconds * 1000L;
            }
        } catch (NumberFormatException e) {
            System.err.println("Nieprawidłowy format czasu. Użyj mm:ss lub ss");
        }
        return 0;
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 10px 16px;" +
            "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: derive(" + color + ", 20%);" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 10px 16px;" +
            "-fx-cursor: hand;"
        ));

        button.setOnMouseExited(e -> button.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: white;" +
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 5px;" +
            "-fx-padding: 10px 16px;" +
            "-fx-cursor: hand;"
        ));

        return button;
    }
    
    private void addFilm(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz film");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki wideo", "*.mp4", "*.avi", "*.mkv"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            Film film = new Film(file.getName(), file.toPath());
            filmLibrary.addFilm(film);
            filmListView.getItems().add(film);
        }
    }
  
    


    public static void main(String[] args) {
    	    try {
    	        System.setProperty("vlcj.library.path", "libs/vlc");
    	        System.setProperty("vlcj.log", "DEBUG");

    	        launch(args);

    	    } catch (Throwable t) {
    	        t.printStackTrace();
    	    }
    	}
}