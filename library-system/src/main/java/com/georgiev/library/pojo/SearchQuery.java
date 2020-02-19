package com.georgiev.library.pojo;
public class SearchQuery {
    public String title;
    public String authorName;
    public String genre;

    public SearchQuery() {
    }

    public SearchQuery(String title, String authorName, String genre) {
        this.title = title;
        this.authorName = authorName;
        this.genre = genre;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
