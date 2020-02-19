package com.georgiev.library.pojo;
import java.util.List;

public class AddBookList {
    private String title;
    private int userId;
    private int bookId;

    public AddBookList(String title, int userId, int bookId) {
        this.title = title;
        this.userId = userId;
        this.bookId = bookId;
    }

    public AddBookList() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
}
