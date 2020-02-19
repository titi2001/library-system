package com.georgiev.library.pojo;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.List;

public class AddBook {
    private String title;
    private String description;
    private List<String> authors;
    private String genre;
    private MultipartFile[] imageFile;
    private MultipartFile[] bookFile;


    public AddBook(String title, String description, List<String> authors, String genre, MultipartFile[] imageFile, MultipartFile[] bookFile) {
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.genre = genre;
        if(imageFile.length == 0){this.imageFile = null;}
        else{
        this.imageFile = imageFile;}
        if(bookFile.length == 0){this.bookFile = null;}
        else{
        this.bookFile = bookFile;}
    }
    public AddBook(String title, String description, List<String> authors, String genre, String imageFile, String bookFile){
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.genre = genre;
        this.imageFile = null;
        this.bookFile = null;
    }

    public AddBook() {
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

    public MultipartFile[] getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile[] imageFile) {
        this.imageFile = imageFile;
    }

    public MultipartFile[] getBookFile() {
        return bookFile;
    }

    public void setBookFile(MultipartFile[] bookFile) {
        this.bookFile = bookFile;
    }
}
