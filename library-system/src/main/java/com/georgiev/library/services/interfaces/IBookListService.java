package com.georgiev.library.services.interfaces;

import com.georgiev.library.entities.Book;
import com.georgiev.library.pojo.AddBookList;
import com.georgiev.library.entities.BookList;

import java.util.List;

public interface IBookListService {
    boolean createBookList(AddBookList bookList);
    boolean editBookList(BookList bookList);
    boolean deleteBookList(int id);
    BookList getBookList(int id);
    List<BookList> getBookListsByBook(int bookId);
}
