package pl.piotrek.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class IntroAdManagerPanel {

    private final List<Path> introVideos = new ArrayList<>();
    private final List<Path> adVideos = new ArrayList<>();

    public IntroAdManagerPanel() {
        adVideos.addAll(loadListFromFile("ad_videos.txt"));
        introVideos.addAll(loadListFromFile("intro_videos.txt"));
    }

    public List<Path> getIntroVideos() {
        return introVideos;
    }

    public List<Path> getAdVideos() {
        return adVideos;
    }

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("🎬 Zarządzaj Wstępami i Reklamami");
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        ListView<String> introList = new ListView<>();
        ListView<String> adList = new ListView<>();

        for (Path p : introVideos) introList.getItems().add(p.getFileName().toString());
        for (Path p : adVideos) adList.getItems().add(p.getFileName().toString());

        Button addIntro = new Button("➕ Dodaj Wstęp");
        Button addAd = new Button("➕ Dodaj Reklamę");
        Button removeIntro = new Button("🗑 Usuń Wstęp");
        Button removeAd = new Button("🗑 Usuń Reklamę");
        Button saveButton = new Button("💾 Zapisz");

        addIntro.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Wybierz plik wstępu");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Filmy", "*.mp4", "*.avi", "*.mkv"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                introVideos.add(file.toPath());
                introList.getItems().add(file.getName());
            }
        });

        addAd.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Wybierz plik reklamy");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Filmy", "*.mp4", "*.avi", "*.mkv"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                adVideos.add(file.toPath());
                adList.getItems().add(file.getName());
            }
        });

        removeIntro.setOnAction(e -> {
            int index = introList.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                introVideos.remove(index);
                introList.getItems().remove(index);
            }
        });

        removeAd.setOnAction(e -> {
            int index = adList.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                adVideos.remove(index);
                adList.getItems().remove(index);
            }
        });

        saveButton.setOnAction(e -> saveVideosToFiles());

        TitledPane introPane = new TitledPane("🎞 Wstępy", new VBox(5, introList, new HBox(5, addIntro, removeIntro)));
        TitledPane adPane = new TitledPane("📺 Reklamy", new VBox(5, adList, new HBox(5, addAd, removeAd)));

        introPane.setExpanded(true);
        adPane.setExpanded(true);

        root.getChildren().addAll(introPane, adPane, saveButton);

        stage.setScene(new Scene(root, 500, 400));
        stage.show();
    }

    private void saveVideosToFiles() {
        saveListToFile(adVideos, "ad_videos.txt");
        saveListToFile(introVideos, "intro_videos.txt");
    }

    private void saveListToFile(List<Path> list, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Path path : list) {
                writer.println(path.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Path> loadListFromFile(String filename) {
        List<Path> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(Paths.get(line));
            }
        } catch (IOException e) {
        }
        return list;
    }
}