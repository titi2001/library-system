package com.georgiev.library.controllers;

import com.georgiev.library.pojo.AddQuote;
import com.georgiev.library.entities.Quote;
import com.georgiev.library.services.impl.BookServiceImpl;
import com.georgiev.library.services.impl.QuoteServiceImpl;
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
    public boolean isAuthenticated(String token) {
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        return userService.getUser(Integer.parseInt((String) jwt.getBody().get("id"))) != null;
    }
    @GetMapping("/")
    public Quote getQuote(@RequestParam("id") String id, @RequestParam("token") String token) {
        if (isAuthenticated(token)) {
            Quote quote = quoteService.getQuote(Integer.parseInt(id));
            return quote;
        }
        return null;
    }

    @PostMapping("/")
    public ResponseEntity < ? > addQuote(@RequestBody AddQuote quote, @RequestParam("token") String token) throws Exception {
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        if (userService.getUser(Integer.parseInt((String) jwt.getBody().get("id"))) != null) {
            quote.setUserId(Integer.parseInt((String) jwt.getBody().get("id")));
            if (quoteService.createQuote(quote)) {
                LOGGER.info("Quote added!");
                return new ResponseEntity < String > ("Uploaded", HttpStatus.OK);
            }
        }
        LOGGER.info("Quote not added!");
        return new ResponseEntity < String > ("Error", HttpStatus.BAD_REQUEST);
    }
}