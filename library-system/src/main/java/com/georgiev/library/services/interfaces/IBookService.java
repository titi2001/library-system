package com.georgiev.library.services.interfaces;

import com.georgiev.library.pojo.AddBook;
import com.georgiev.library.entities.Book;
import com.georgiev.library.pojo.SearchQuery;
import java.sql.SQLException;
import java.util.List;

public interface IBookService {
    boolean createBook(AddBook book);
    boolean editBook(Book book);
    boolean deleteBook(int id);
    Book getBook(int id);
    Book findByTitle(String title);
    List<Book> getAllBooks();
    List<Book> searchBooks(SearchQuery searchQuery) throws SQLException;
}
