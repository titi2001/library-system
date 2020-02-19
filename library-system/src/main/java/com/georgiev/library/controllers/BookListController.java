package com.georgiev.library.controllers;

import com.georgiev.library.entities.Author;
import com.georgiev.library.entities.Book;
import com.georgiev.library.entities.BookList;
import com.georgiev.library.pojo.*;
import com.georgiev.library.services.impl.BookListServiceImpl;
import com.georgiev.library.services.impl.BookServiceImpl;
import com.georgiev.library.services.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookList")
public class BookListController {
    private BookListServiceImpl bookListService;
    private UserServiceImpl userService;
    private BookServiceImpl bookService;
    private static final Logger LOGGER = LoggerFactory.getLogger(BookListController.class);

    public BookListController(BookListServiceImpl bookListService, UserServiceImpl userService, BookServiceImpl bookService) {
        this.bookListService = bookListService;
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/get/{id}")
    public BookList getBookList(@PathVariable("id") String id){
        BookList bookList = bookListService.getBookList(Integer.parseInt(id));
        bookList.setUser(null);
        for (Book book:
             bookList.getBooks()) {
            book.setQuotes(null);
            book.setBookLists(null);
            for (Author author:
                 book.getAuthors()) {
                author.setBooks(null);
            }
        }
        return bookList;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addBookList(@RequestBody AddBookList bookList) throws Exception {
        if(bookListService.createBookList(bookList)){
            LOGGER.info(bookList.getTitle() + " (book list) created");
            return new ResponseEntity<String>("Uploaded", HttpStatus.OK);
        }
        return new ResponseEntity<String>("Error", HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/addBookToBookList/{bookListId}/{bookId}")
    public ResponseEntity<?> addBookToBookList(@PathVariable("bookListId") String bookListId, @PathVariable("bookId") String bookId){
        BookList bookList = bookListService.getBookList(Integer.parseInt(bookListId));
        Book book = bookService.getBook(Integer.parseInt(bookId));
        bookList.getBooks().add(bookService.getBook(Integer.parseInt(bookId)));
        book.getBookLists().add(bookList);
        if(bookService.editBook(book)){
            LOGGER.info(bookService.getBook(Integer.parseInt(bookId)).getTitle() + " added to book list " + bookList.getTitle());
            return new ResponseEntity<String>("Uploaded", HttpStatus.OK);
        }
        LOGGER.info(bookService.getBook(Integer.parseInt(bookId)).getTitle() + "not added to book list " + bookList.getTitle());
        return new ResponseEntity<String>("Error", HttpStatus.BAD_REQUEST);
    }


}
