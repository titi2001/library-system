package com.georgiev.library.controllers;

import com.georgiev.library.pojo.AddQuote;
import com.georgiev.library.entities.Quote;
import com.georgiev.library.services.impl.BookServiceImpl;
import com.georgiev.library.services.impl.QuoteServiceImpl;
import com.georgiev.library.services.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quote")
public class QuoteController {
    private QuoteServiceImpl quoteService;
    private BookServiceImpl bookService;
    private UserServiceImpl userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteController.class);

    public QuoteController(QuoteServiceImpl quoteService, BookServiceImpl bookService, UserServiceImpl userService) {
        this.quoteService = quoteService;
        this.bookService = bookService;
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addQuote(@RequestBody AddQuote quote) throws Exception {
        if(quoteService.createQuote(quote)){
            LOGGER.info("Quote added!");
            userService.setUser(userService.getUser(userService.getUser().getId()));
            return new ResponseEntity<String>("Uploaded", HttpStatus.OK);}
        LOGGER.info("Quote not added!");
        return new ResponseEntity<String>("Error", HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/get/{id}")
    public Quote getQuote(@PathVariable("id") String id){
        Quote quote = quoteService.getQuote(Integer.parseInt(id));
        /*if(quote != null){
            quote.getUser().setBookLists(null);
            quote.getUser().setQuotes(null);
            quote.getUser().setDownloadedBooks(null);
            quote.getUser().setFinishedBooks(null);
            quote.getUser().setStartedBooks(null);
            for (Author author:
                 quote.getBook().getAuthors()) {
                author.setBooks(null);
            }
            quote.getBook().setBookLists(null);
        }*/
        return quote;
    }
}
