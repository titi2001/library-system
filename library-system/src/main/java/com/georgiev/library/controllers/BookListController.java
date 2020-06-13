package com.georgiev.library.controllers;

import com.georgiev.library.entities.BookList;
import com.georgiev.library.pojo.*;
import com.georgiev.library.services.impl.BookListServiceImpl;
import com.georgiev.library.services.impl.BookServiceImpl;
import com.georgiev.library.services.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
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

    public boolean isAuthenticated(String token) {
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        return userService.getUser(Integer.parseInt((String) jwt.getBody().get("id"))) != null;
    }

    @GetMapping("/{id}")
    public BookList getBookList(@PathVariable("id") String id, @RequestParam("token") String token) {
        if (isAuthenticated(token)) {
            BookList bookList = bookListService.getBookList(Integer.parseInt(id));
            bookList.setUser(null);
            return bookList;
        }
        return null;
    }

    @PostMapping("/")
    public ResponseEntity < ? > addBookList(@RequestBody AddBookList bookList, @RequestParam("token") String token) throws Exception {
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        if (userService.getUser(Integer.parseInt((String) jwt.getBody().get("id"))) != null) {
            bookList.setUserId(Integer.parseInt((String) jwt.getBody().get("id")));
            if (bookListService.createBookList(bookList)) {
                LOGGER.info(bookList.getTitle() + " (book list) created");
                return new ResponseEntity < String > ("Uploaded", HttpStatus.OK);
            }
        }
        return new ResponseEntity < String > ("Error", HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/")
    public ResponseEntity < ? > addBookToBookList(@RequestParam("bookListId") String bookListId, @RequestParam("bookId") String bookId, @RequestParam("token") String token) {
        if (isAuthenticated(token)) {
            BookList bookList = bookListService.getBookList(Integer.parseInt(bookListId));
            bookList.getBooks().add(bookService.getBook(Integer.parseInt(bookId)));
            if (bookListService.editBookList(bookList)) {
                LOGGER.info(bookService.getBook(Integer.parseInt(bookId)).getTitle() + " added to book list " + bookList.getTitle());
                return new ResponseEntity < String > ("Uploaded", HttpStatus.OK);
            }
            LOGGER.info(bookService.getBook(Integer.parseInt(bookId)).getTitle() + "not added to book list " + bookList.getTitle());
        }
        return new ResponseEntity < String > ("Error", HttpStatus.BAD_REQUEST);
    }


}