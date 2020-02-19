package com.georgiev.library.pojo;
import java.util.List;

public class EditBook {
    private String title;
    private String description;
    private List<String> authors;
    private String genre;

    public EditBook() {
    }

    public EditBook(String title, String description, List<String> authors, String genre) {
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.genre = genre;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
