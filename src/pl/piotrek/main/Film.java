package pl.piotrek.main;

import java.nio.file.Path;

public class Film {
    private String title;
    private Path filePath;

    public Film(String title, Path filePath) {
        this.title = title;
        this.filePath = filePath;
    }

    public String getTitle() {
        return title;
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return title;
    }
}