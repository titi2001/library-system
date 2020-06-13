package com.georgiev.library.services.impl;

import com.georgiev.library.pojo.AddBookList;
import com.georgiev.library.entities.Book;
import com.georgiev.library.entities.BookList;
import com.georgiev.library.entities.User;
import com.georgiev.library.repositories.BookListRepository;
import com.georgiev.library.repositories.BookRepository;
import com.georgiev.library.repositories.UserRepository;
import com.georgiev.library.services.interfaces.IBookListService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class BookListServiceImpl implements IBookListService {
    private BookListRepository bookListRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;

    public BookListServiceImpl(BookListRepository bookListRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.bookListRepository = bookListRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean createBookList(AddBookList bookList) {
        BookList b = new BookList();
        b.setTitle(bookList.getTitle());
        b.setUser(userRepository.findById(bookList.getUserId()));
        b.setBooks(new HashSet < > ());
        if (bookRepository.findById(bookList.getBookId()) != null) {
            Book book = bookRepository.findById(bookList.getBookId());
            b.getBooks().add(book);
        }
        return bookListRepository.save(b) != null;
    }

    @Override
    public boolean editBookList(BookList bookList) {
        return bookListRepository.save(bookList) != null;
    }


    @Override
    public boolean deleteBookList(int id) {
        long test = bookListRepository.count();
        BookList bookList = bookListRepository.findById(id);
        User user = bookList.getUser();
        user.getBookLists().remove(bookList);
        userRepository.save(user);
        bookListRepository.delete(bookList);
        return bookListRepository.count() == test - 1;
    }

    @Override
    public BookList getBookList(int id) {
        return bookListRepository.findById(id);
    }

    @Override
    public List < BookList > getBookListsByBook(int bookId) {
        return bookListRepository.findBookListsByBook(bookId);
    }

}