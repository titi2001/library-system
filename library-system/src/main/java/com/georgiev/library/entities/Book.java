package com.georgiev.library.entities;

import com.georgiev.library.pojo.Genre;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {
    private int id;
    private String title;
    private String description;
    private Set < Author > authors;
    @Enumerated(EnumType.STRING)
    private Genre genre;
    private String imageName;
    private String fileName;
    public Book() {}

    public Book(int id, String title, String description, Set < Author > authors, Genre genre, String imageName, String fileName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.genre = genre;
        this.imageName = imageName;
        this.fileName = fileName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(length = 1024)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToMany(cascade = {
            CascadeType.MERGE
    }, fetch = FetchType.EAGER)
    @JoinTable(name = "book_author",
            joinColumns = {
                    @JoinColumn(name = "book_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "author_id")
            })
    public Set < Author > getAuthors() {
        return authors;
    }

    public void setAuthors(Set < Author > authors) {
        this.authors = authors;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Book book = (Book) obj;
        return (book.id == this.id && book.getTitle().equals(this.title));
    }

    @Override
    public int hashCode() {
        return this.id * title.length();
    }
}