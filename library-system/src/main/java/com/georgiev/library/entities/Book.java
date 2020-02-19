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
    private Set<Author> authors;
    @Enumerated(EnumType.STRING)
    private Genre genre;
    private String imageName;
    private String fileName;
    private Set<BookList> bookLists;
    private Set<Quote> quotes;
    public Book() {
    }

    public Book(int id, String title, String description, Set<Author> authors, Genre genre, String imageName, String fileName, Set<BookList> bookLists, Set<Quote> quotes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.genre = genre;
        this.imageName = imageName;
        this.fileName = fileName;
        this.bookLists = bookLists;
        this.quotes = quotes;
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

    @ManyToMany(cascade = { CascadeType.MERGE }, fetch = FetchType.EAGER)
    @JoinTable(
            name = "Book_Author",
            joinColumns = { @JoinColumn(name = "book_id") },
            inverseJoinColumns = { @JoinColumn(name = "author_id") }
    )
    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
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

    @ManyToMany(cascade = { CascadeType.MERGE },  fetch = FetchType.EAGER)
    @JoinTable(
            name = "Book_BookLists",
            joinColumns = { @JoinColumn(name = "book_id") },
            inverseJoinColumns = { @JoinColumn(name = "bookList_id") }
    )
    public Set<BookList> getBookLists() {
        return bookLists;
    }

    public void setBookLists(Set<BookList> bookLists) {
        this.bookLists = bookLists;
    }

    @OneToMany(mappedBy="book",  fetch = FetchType.EAGER)
    public Set<Quote> getQuotes() {
        return quotes;
    }

    public void setQuotes(Set<Quote> quotes) {
        this.quotes = quotes;
    }

    @Override
    public boolean equals(Object obj)
    {

        if(this == obj)
            return true;

        if(obj == null || obj.getClass()!= this.getClass())
            return false;

        Book book = (Book) obj;
        return (book.id == this.id && book.getTitle().equals(this.title));
    }

    @Override
    public int hashCode()
    {
        return this.id * title.length();
    }
}

