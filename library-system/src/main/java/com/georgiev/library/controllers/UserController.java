package com.georgiev.library.controllers;

import com.georgiev.library.entities.Book;
import com.georgiev.library.entities.BookList;
import com.georgiev.library.entities.Quote;
import com.georgiev.library.entities.User;
import com.georgiev.library.services.impl.BookServiceImpl;
import com.georgiev.library.services.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;

@RestController
public class UserController {
    private Twitter twitter;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private UserServiceImpl userService;
    private BookServiceImpl bookService;
    public UserController(UserServiceImpl userService, BookServiceImpl bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/getUser")
    public User getUser() throws TwitterException {
        User u  = userService.getUser();
        if(u != null){

            if(u.getBookLists() != null){
                for (BookList bookList:
                     u.getBookLists()) {
                    bookList.setUser(null);
                    bookList.setBooks(new HashSet<>());
                }}
                if(u.getQuotes() != null){
                for (Quote quote:
                     u.getQuotes()) {
                    quote.setUser(null);
                    quote.getBook().setBookLists(null);
                    quote.getBook().setAuthors(null);
                    quote.getBook().setQuotes(null);
                }}
                if(u.getStartedBooks() != null){
                for (Book book:
                        u.getStartedBooks()) {
                    book.setQuotes(null);
                    book.setAuthors(null);
                    book.setBookLists(null);
                }}
                if(u.getFinishedBooks() != null){
                for (Book book:
                        u.getFinishedBooks()) {
                    book.setQuotes(null);
                    book.setAuthors(null);
                    book.setBookLists(null);
                }}
                if(u.getDownloadedBooks() != null){
                for (Book book:
                     u.getDownloadedBooks()) {
                    book.setQuotes(null);
                    book.setAuthors(null);
                    book.setBookLists(null);
                }}

        }
        return u;
    }

    @RequestMapping("/getToken")
    public RedirectView getToken(HttpServletRequest request, Model model) {
        //destination
        String twitterUrl = "";

        try {
            twitter = getTwitter();
            String callbackUrl = "http://localhost:8007/twitterCallback";
            RequestToken requestToken = twitter.getOAuthRequestToken(callbackUrl);
            request.getSession().setAttribute("requestToken", requestToken);
            request.getSession().setAttribute("twitter", twitter);
            twitterUrl = requestToken.getAuthorizationURL();
            LOGGER.info("Authorization url is " + twitterUrl);
        } catch (Exception e) {
            LOGGER.error("Problem logging in with Twitter!", e);
        }
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(twitterUrl);
        return redirectView;
    }


    public Twitter getTwitter() {
        Twitter twitter = null;
        String consumerKey = "nWgNOIVgIVEBL911MIhHuzVGi";
        String consumerSecret = "3BWI48tKyWz89PjYgmqLd5Nq0F32qW1YiBcsURr8qMpGTAEizi";
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(consumerKey);
        builder.setOAuthConsumerSecret(consumerSecret);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
        return twitter;
    }

    @RequestMapping("/twitterCallback")
    public RedirectView twitterCallback(@RequestParam(value="oauth_verifier", required=false) String oauthVerifier,
                                        @RequestParam(value="denied", required=false) String denied,
                                        HttpServletRequest request, HttpServletResponse response, Model model) {
        twitter = (Twitter) request.getSession().getAttribute("twitter");
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        try {
            AccessToken token = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
            request.getSession().removeAttribute("requestToken");
            if(!userService.existsByUsername(twitter.getScreenName())){
                userService.createUser(twitter.getScreenName());
            }
            userService.setUser(userService.getUserByUsername(twitter.getScreenName()));
            User m = userService.getUserByUsername(twitter.getScreenName());
        } catch (Exception e) {
            LOGGER.error("Problem getting token!",e);
        }
        return new RedirectView("http://localhost:8007");
    }

    @PutMapping("/toggleShareActivity")
    public void toggleShareActivity(){
        User u = userService.getUser();
        u.setShareActivity(!u.getShareActivity());
        userService.editUser(u);
        userService.setUser(u);
        LOGGER.info(userService.getUser().getUsername() + "'s share activity set to " + userService.getUser().getShareActivity());
    }
    @PutMapping("/startBook/{id}")
    public void startBook(@PathVariable("id") String id) throws TwitterException {
        Book book = bookService.getBook(Integer.parseInt(id));
        User user = userService.getUser();
        if(!user.getStartedBooks().contains(book) ){
            user.getStartedBooks().add(book);
            LOGGER.info(book.getTitle() + " added to " + user.getUsername() + "'s started books");
            userService.editUser(user);
            LOGGER.info("User edited!");
            if(user.getShareActivity()){
                twitter.updateStatus("I just started reading " + book.getTitle());
                LOGGER.info("Tweeted about beginning reading " + book.getTitle());
            }
        }
    }
    @PutMapping("/finishBook/{id}")
    public void finish(@PathVariable("id") String id) throws TwitterException {
        Book book = bookService.getBook(Integer.parseInt(id));
        User user = userService.getUser();
        if(!user.getFinishedBooks().contains(book)){
            if(user.getShareActivity()){
                twitter.updateStatus("I just finished reading " + book.getTitle());
                LOGGER.info("Tweeted about finishing reading " + book.getTitle());
            }
            user.getFinishedBooks().add(book);
            LOGGER.info(book.getTitle() + " added to " + user.getUsername() + "'s finished books");
            userService.editUser(user);
            userService.setUser(user);
        }
    }
}
