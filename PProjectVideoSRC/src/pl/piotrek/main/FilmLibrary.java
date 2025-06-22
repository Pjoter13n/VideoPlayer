package pl.piotrek.main;

import java.util.ArrayList;
import java.util.List;

public class FilmLibrary {
    private final List<Film> films;

    public FilmLibrary() {
        this.films = new ArrayList<>();
    }

    public void addFilm(Film film) {
        films.add(film);
    }

    public void removeFilm(Film film) {
        films.remove(film);
    }

    public List<Film> getFilms() {
        return films;
    }
}