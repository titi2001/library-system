package com.georgiev.library.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.georgiev.library.entities.*;
import com.georgiev.library.pojo.*;
import com.georgiev.library.services.impl.AuthorServiceImpl;
import com.georgiev.library.services.impl.BookServiceImpl;
import com.georgiev.library.services.impl.UserServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import nl.siegmann.epublib.epub.EpubReader;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import twitter4j.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/book")
public class BookController {
    private BookServiceImpl bookService;
    private AuthorServiceImpl authorService;
    private UserServiceImpl userService;
    private static final Logger LOGGER = LoggerFactory.getLogger(BookController.class);
    private static String UPLOAD_DIR = System.getProperty("user.dir") + "/library-system/src/main/resources/static/bookFiles";
    public BookController(BookServiceImpl bookService, AuthorServiceImpl authorService, UserServiceImpl userService) {
        this.bookService = bookService;
        this.authorService = authorService;
        this.userService = userService;
    }

    public boolean isAuthenticated(String token) {
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        return userService.getUser(Integer.parseInt((String) jwt.getBody().get("id"))) != null;
    }

    public boolean isAdmin(String token) {
        List < String > admins = new ArrayList < > (Arrays.asList("AccountThesis"));
        Jws < Claims > jwt = Jwts.parser()
                .setSigningKey("")
                .parseClaimsJws(token);
        return admins.contains(jwt.getBody().get("username"));
    }

    @GetMapping("/")
    private List < Book > getAllBooks(@RequestParam("token") String token) throws SQLException {
        if (isAuthenticated(token)) {
            return bookService.getAllBooks();
        }
        return null;
    }

    @GetMapping("/{id}")
    public Book getBook(@PathVariable("id") String id, @RequestParam("token") String token) {
        if (isAuthenticated(token)) {
            return bookService.getBook(Integer.parseInt(id));
        }
        return null;
    }

    @PostMapping("/")
    public ResponseEntity < ? > addBook(@ModelAttribute AddBook book, @RequestParam("token") String token) throws Exception {
        if (isAdmin(token)) {
            if (book.getImageFile()[0] == null || book.getBookFile()[0] == null) {
                LOGGER.info("Files not found");
                return new ResponseEntity < > ("Error: File(s) not found", HttpStatus.BAD_REQUEST);
            }
            saveUploadedFile(book.getImageFile()[0], book.getTitle() + book.getAuthors().get(0) + book.getImageFile()[0].getOriginalFilename(), "/images");
            saveUploadedFile(book.getBookFile()[0], book.getTitle() + book.getAuthors().get(0) + book.getBookFile()[0].getOriginalFilename(), "/epubs");
            bookService.createBook(book);
            Book b = bookService.findByTitle(book.getTitle());
            linkAuthors(book, b);
            LOGGER.info("Book " + book.getTitle() + " created");
            return new ResponseEntity < String > ("Uploaded", HttpStatus.OK);
        }
        return new ResponseEntity < String > ("No", HttpStatus.BAD_REQUEST);
    }
    private String saveUploadedFile(MultipartFile file, String newName, String addition) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        uploadDir.mkdirs();
        StringBuilder sb = new StringBuilder();
        String uploadFilePath = UPLOAD_DIR + addition + "/" + newName;
        byte[] bytes = file.getBytes(); //getInputStream, TransferTo (1)
        Path path = Paths.get(uploadFilePath);
        Files.write(path, bytes);
        sb.append(uploadFilePath).append(", ");
        return sb.toString();
    }

    @PutMapping("/")
    public ResponseEntity < ? > editBook(@ModelAttribute EditBook book, @RequestParam("id") String id, @RequestParam("token") String token) throws IOException {
        if (isAdmin(token)) {
            Book b = bookService.getBook(Integer.parseInt(id));
            if (book.getTitle() != null) {
                b.setTitle(book.getTitle());
            }
            if (book.getDescription() != null) {
                b.setDescription(book.getDescription());
            }
            if (book.getGenre() != null) {
                b.setGenre(Genre.valueOf(book.getGenre().toUpperCase()));
            }
            if (book.getAuthors() != null && !new HashSet < > (book.getAuthors()).equals(b.getAuthors())) {
                AddBook temp = new AddBook(book.getTitle(), book.getDescription(), book.getAuthors(), book.getGenre(), new MultipartFile[1], new MultipartFile[1]);
                linkAuthors(temp, b);
            }
            if (bookService.editBook(b)) {
                LOGGER.info(b.getTitle() + "'s info edited");
                return new ResponseEntity < String > ("Edited", HttpStatus.OK);
            }
            LOGGER.info(b.getTitle() + "couldn't be edited");
            return new ResponseEntity < > ("Error", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity < > ("Error", HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/editPicture")
    public ResponseEntity < ? > editImage(@RequestBody MultipartFile image, @RequestParam("id") String id, @RequestParam("token") String token) throws IOException {
        if (isAdmin(token)) {
            Book book = bookService.getBook(Integer.parseInt(id));
            if (image != null) {
                File i = new File(UPLOAD_DIR + "/images/" + book.getImageName());
                i.delete();
                saveUploadedFile(image, book.getImageName(), "/images");
                LOGGER.info(book.getTitle() + "'s image edited");
                return new ResponseEntity < String > ("Edited", HttpStatus.OK);
            }
            LOGGER.info(book.getTitle() + "'s image couldn't be edited");
            return new ResponseEntity < > ("Error", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity < > ("Error", HttpStatus.BAD_REQUEST);

    }

    @PutMapping("/editFile")
    public ResponseEntity < ? > editFile(@ModelAttribute MultipartFile file, @RequestParam("id") String id, @RequestParam("token") String token) throws IOException {
        if (isAdmin(token)) {
            Book book = bookService.getBook(Integer.parseInt(id));
            if (file != null) {
                File bookFile = new File(UPLOAD_DIR + "/epubs/" + book.getFileName());
                bookFile.delete();
                saveUploadedFile(file, book.getFileName(), "/epubs");
                LOGGER.info(book.getTitle() + "'s file edited");
                return new ResponseEntity < String > ("Edited", HttpStatus.OK);
            }
            LOGGER.info(book.getTitle() + "'s file couldn't be edited");
            return new ResponseEntity < > ("Error", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity < > ("Error", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/search")
    private List < Book > searchBooks(@RequestParam("title") String title, @RequestParam("authorName") String authorName, @RequestParam("genre") String genre, @RequestParam("token") String token) throws SQLException {
        if (isAuthenticated(token)) {
            List < Book > books = bookService.searchBooks(title, authorName, genre);
            LOGGER.info("Search query: Title: '" + title + "', Author Name: '" + authorName + "' , Genre: '" + genre + "' completed");
            return books;
        }
        return null;
    }

    private List < String > getAuthorsAsList(List < Author > authors) {
        List < String > result = new ArrayList < > ();
        for (int i = 0; i < authors.size(); i++) {
            result.add(authors.get(i).getName());
        }
        return result;
    }

    @DeleteMapping("/")
    private boolean deleteBook(@RequestParam("id") String id, @RequestParam("token") String token) {
        if (isAdmin(token)) {
            Book b = bookService.getBook(Integer.parseInt(id));
            File image = new File(UPLOAD_DIR + "/images/" + b.getImageName());
            File bookFile = new File(UPLOAD_DIR + "/epubs/" + b.getFileName());
            image.delete();
            bookFile.delete();
            return bookService.deleteBook(Integer.parseInt(id));
        }
        return false;
    }
    @GetMapping("/getText")
    public List < String > getText(@RequestParam("id") String id, @RequestParam("token") String token) throws IOException {
        if (isAuthenticated(token)) {
            List < String > bookContent = new ArrayList < > ();
            Book b = bookService.getBook(Integer.parseInt(id));
            EpubReader epubReader = new EpubReader();
            nl.siegmann.epublib.domain.Book book = epubReader.readEpub(new FileInputStream(UPLOAD_DIR + "/epubs/" + b.getFileName()));
            for (int i = 0; i < book.getContents().size(); i++) {
                String entireContent = "";
                String textContent = "";
                InputStream inputStream = book.getContents().get(i).getInputStream(); // file .html
                try {
                    Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
                    entireContent = scanner.hasNext() ? scanner.next() : "";
                } finally {
                    inputStream.close();
                }
                org.jsoup.nodes.Document doc = Jsoup.parse(entireContent);
                textContent += doc.body().text();
                if (textContent.length() > 0) {
                    textContent += "\n\n";
                    bookContent.add(textContent);
                }
            }
            return bookContent;
        }
        return null;
    }

    @GetMapping("/download")
    public ResponseEntity < ByteArrayResource > downloadBook(@RequestParam("bookId") String bookId, @RequestParam("userId") String userId, @RequestParam("token") String token) throws IOException {
        if (isAuthenticated(token)) {
            User user = userService.getUser(Integer.parseInt(userId));
            if (user != null) {
                Book book = bookService.getBook(Integer.parseInt(bookId));
                File file = new File(UPLOAD_DIR + "/epubs/" + book.getFileName());
                Path path = Paths.get(file.getAbsolutePath());
                ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
                HttpHeaders header = new HttpHeaders();
                header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + book.getTitle() + ".epub");
                if (!user.getDownloadedBooks().contains(book)) {
                    user.getDownloadedBooks().add(book);
                    userService.editUser(user);
                }
                return ResponseEntity.ok()
                        .headers(header)
                        .contentLength(file.length())
                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(resource);
            }
        }
        return null;
    }

    private boolean linkAuthors(AddBook book, Book b) {
        b.setAuthors(new HashSet < > ());
        for (String a:
                book.getAuthors()) {
            AddAuthor author = new AddAuthor(a);
            if (authorService.getAuthorByName(a) == null) {
                authorService.createAuthor(author);
            }
            b.getAuthors().add(authorService.getAuthorByName(a));
            bookService.editBook(b);
        }
        return true;
    }

    @GetMapping("/wikipedia")
    public ModelAndView searchInWikipedia(@RequestParam("word") String word, @RequestParam("token") String token) throws JsonProcessingException {
        if (isAuthenticated(token)) {
            RestTemplate restTemplate = new RestTemplate();
            String fooResourceUrl = "https://en.wikipedia.org/w/api.php?action=opensearch&search=" + word + "&limit=1&namespace=0&callback=JSON_CALLBACK";
            ResponseEntity < String > response = restTemplate.getForEntity(fooResourceUrl + "/1", String.class);
            ObjectMapper mapper = new ObjectMapper();
            String str = response.getBody().toString();
            Pattern p = Pattern.compile("(https(.*?)\\\")");
            Matcher m = p.matcher(str);
            while (m.find()) {
                LOGGER.info(m.group(1).substring(0, m.group(1).length() - 1));
                return new ModelAndView("redirect:" + (m.group(1).substring(0, m.group(1).length() - 1)));

            }
        }
        return null;

    }
}