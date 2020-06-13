package com.georgiev.library.entities;

import javax.persistence.*;

@Entity
@Table(name = "quotes")
public class Quote {
    private int id;
    private String content;
    private Book book;
    private User user;

    public Quote() {}

    public Quote(int id, String content, Book book, User user) {
        this.id = id;
        this.content = content;
        this.book = book;
        this.user = user;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(length = 1024)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}