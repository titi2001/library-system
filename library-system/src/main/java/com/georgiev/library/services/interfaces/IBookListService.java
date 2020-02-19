package com.georgiev.library.services.interfaces;

import com.georgiev.library.pojo.AddBookList;
import com.georgiev.library.entities.BookList;
public interface IBookListService {
    boolean createBookList(AddBookList bookList);
    boolean editBookList(BookList bookList);
    boolean deleteBookList(int id);
    BookList getBookList(int id);
}
