package com.georgiev.library.controllers;

import com.georgiev.library.entities.Book;
import com.georgiev.library.entities.BookList;
import com.georgiev.library.entities.Quote;
import com.georgiev.library.entities.User;
import com.georgiev.library.services.impl.BookServiceImpl;
import com.georgiev.library.services.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private UserServiceImpl userService;
    private BookServiceImpl bookService;

    public UserController(UserServiceImpl userService, BookServiceImpl bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }



    @GetMapping("/")
    public User getUser(@RequestParam("token") String token) throws TwitterException {
        if (!token.equals("undefined")) {
            Jws < Claims > jwt = Jwts.parser()
                    .setSigningKey("")
                    .parseClaimsJws(token);
            User u = userService.getUser(Integer.parseInt((String) jwt.getBody().get("id")));
            for (Quote quote:
                    u.getQuotes()) {
                quote.setUser(null);
            }
            for (BookList bookList:
                    u.getBookLists()) {
                bookList.setUser(null);
            }
            return u;
        }
        return null;
    }

    @PutMapping("/toggleShareActivity")
    public void toggleShareActivity(@RequestParam("token") String token) {
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        User u = userService.getUser(Integer.parseInt((String) jwt.getBody().get("id")));
        u.setShareActivity(!u.getShareActivity());
        userService.editUser(u);
        u = userService.getUser(Integer.parseInt((String) jwt.getBody().get("id")));
        LOGGER.info(u.getUsername() + "'s share activity set to " + u.getShareActivity());
    }
    @PutMapping("/startBook")
    public void startBook(@RequestParam("bookId") String bookId, @RequestParam("token") String token) throws TwitterException {
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        Book book = bookService.getBook(Integer.parseInt(bookId));
        User user = userService.getUser(Integer.parseInt((String) jwt.getBody().get("id")));
        if (!user.getStartedBooks().contains(book)) {
            user.getStartedBooks().add(book);
            LOGGER.info(book.getTitle() + " added to " + user.getUsername() + "'s started books");
            userService.editUser(user);
            LOGGER.info("User edited!");
            if (user.getShareActivity()) {
                AccessToken to = new AccessToken(user.getAccessToken(), user.getAccessTokenSecret());
                Twitter twitter = getTwitter();
                twitter.setOAuthAccessToken(to);
                twitter.updateStatus("I just started reading " + book.getTitle());
                LOGGER.info("Tweeted about beginning reading " + book.getTitle());
            }
        }
    }
    @PutMapping("/finishBook")
    public void finish(@RequestParam("bookId") String bookId, @RequestParam("token") String token) throws TwitterException {
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        Book book = bookService.getBook(Integer.parseInt(bookId));
        User user = userService.getUser(Integer.parseInt((String) jwt.getBody().get("id")));
        if (!user.getFinishedBooks().contains(book)) {
            user.getFinishedBooks().add(book);
            LOGGER.info(book.getTitle() + " added to " + user.getUsername() + "'s finished books");
            if (user.getShareActivity()) {
                AccessToken to = new AccessToken(user.getAccessToken(), user.getAccessTokenSecret());
                Twitter twitter = getTwitter();
                twitter.setOAuthAccessToken(to);
                twitter.updateStatus("I just finished reading " + book.getTitle());
                LOGGER.info("Tweeted about finishing reading " + book.getTitle());
            }
            userService.editUser(user);
            LOGGER.info("User edited!");
        }
    }
    @RequestMapping("/getToken")
    public RedirectView getToken(HttpServletRequest request, Model model) {
        //destination
        String twitterUrl = "";

        try {
            Twitter twitter = getTwitter();
            String callbackUrl = "http://localhost:8007/user/twitterCallback";
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
        Twitter t = null;
        String consumerKey = "";
        String consumerSecret = "";
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(consumerKey);
        builder.setOAuthConsumerSecret(consumerSecret);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        t = factory.getInstance();
        return t;
    }

    @RequestMapping("/twitterCallback")
    public ResponseEntity < ? > twitterCallback(@RequestParam(value = "oauth_verifier", required = false) String oauthVerifier,
                                                @RequestParam(value = "denied", required = false) String denied,
                                                HttpServletRequest request, HttpServletResponse response, Model model) throws TwitterException {
        Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
        AccessToken token = null;
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
        try {
            token = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
            LOGGER.info(String.valueOf(token));
            request.getSession().removeAttribute("requestToken");
            if (!userService.existsByUsername(twitter.getScreenName())) {
                userService.createUser(twitter.getScreenName());
            }
        } catch (Exception e) {
            LOGGER.error("Problem getting token!", e);
        }
        User u = userService.getUserByUsername(twitter.getScreenName());
        u.setAccessToken(token.getToken());
        u.setAccessTokenSecret(token.getTokenSecret());
        u.setProfilePictureUrl(twitter.showUser(twitter.getId()).get400x400ProfileImageURL());
        userService.editUser(u);
        String jws = Jwts.builder()
                .setIssuer("app")
                .setSubject("login")
                .claim("id", String.valueOf(u.getId()))
                .claim("username", u.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.ofEpochSecond(4622470422L)))
                .signWith(
                        SignatureAlgorithm.HS256,
                        TextCodec.BASE64.decode("")
                )
                .compact();
        Cookie cookie = new Cookie("uid", Integer.toString(u.getId()));
        Cookie cookie2 = new Cookie("token", jws);
        cookie2.setPath("/");
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(jws);
        LOGGER.info((String) jwt.getBody().get("username"));
        response.addCookie(cookie);
        response.addCookie(cookie2);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/");
        LOGGER.info(twitter.showUser(twitter.getId()).get400x400ProfileImageURL());
        return new ResponseEntity < String > (headers, HttpStatus.PERMANENT_REDIRECT);
    }

    @GetMapping("/confirm")
    public ResponseEntity < ? > isAdmin(@RequestParam("token") String token){
        List < String > admins = new ArrayList < > (Arrays.asList("AccountThesis"));
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        if(admins.contains(jwt.getBody().get("username"))){
            return new ResponseEntity < String > ("{}", HttpStatus.OK);
        }
        return new ResponseEntity <String > ("{}", HttpStatus.BAD_REQUEST);
    }
}