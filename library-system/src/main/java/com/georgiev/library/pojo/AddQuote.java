package com.georgiev.library.pojo;
public class AddQuote {
    private String content;
    private int bookId;
    private int userId;

    public AddQuote(String content, int bookId, int userId) {
        this.content = content;
        this.bookId = bookId;
        this.userId = userId;
    }

    public AddQuote() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}