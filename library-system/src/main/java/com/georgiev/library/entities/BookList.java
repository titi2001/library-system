package com.georgiev.library.entities;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "bookLists")
public class BookList {
    private int id;
    private String title;
    private User user;

    private Set < Book > books;

    public BookList() {}
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToMany(cascade = {
            CascadeType.MERGE
    }, fetch = FetchType.EAGER)
    @JoinTable(name = "bookList_book",
            joinColumns = {
                    @JoinColumn(name = "bookList_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "book_id")
            })
    public Set < Book > getBooks() {
        return books;
    }

    public void setBooks(Set < Book > books) {
        this.books = books;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null || obj.getClass() != this.getClass())
            return false;

        BookList bookList = (BookList) obj;
        return bookList.id == this.id && bookList.title.equals(this.title) && this.user.getId() == bookList.getUser().getId();
    }

    @Override
    public int hashCode() {
        return this.id * 173;
    }
}