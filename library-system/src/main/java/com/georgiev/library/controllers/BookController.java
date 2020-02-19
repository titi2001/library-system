package com.georgiev.library.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.georgiev.library.entities.*;
import com.georgiev.library.pojo.*;
import com.georgiev.library.services.impl.AuthorServiceImpl;
import com.georgiev.library.services.impl.BookServiceImpl;
import com.georgiev.library.services.impl.UserServiceImpl;
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

    @GetMapping("/getAll")
    private List<Book> getAllBooks() throws SQLException {
        List<Book> books = bookService.getAllBooks();
        for (Book book:
                books) {
            for (Author author:
                    book.getAuthors()) {
                author.setBooks(null);
            }
            for (Quote quote:
                    book.getQuotes()) {
                quote.setBook(null);
                quote.getUser().setBookLists(null);
                quote.getUser().setQuotes(null);
                quote.getUser().setStartedBooks(null);
                quote.getUser().setFinishedBooks(null);
                quote.getUser().setDownloadedBooks(null);
            }
            for (BookList bookList:
                    book.getBookLists()) {
                bookList.getUser().setBookLists(null);
                bookList.getUser().setQuotes(null);
                bookList.getUser().setStartedBooks(null);
                bookList.getUser().setFinishedBooks(null);
                bookList.getUser().setDownloadedBooks(null);
                bookList.setBooks(null);
            }
        }
        return books;
    }

    @GetMapping("/get/{id}")
      public Book getBook(@PathVariable("id") String id){
        Book book = bookService.getBook(Integer.parseInt(id));
        book.setBookLists(null);
        for (Author author:
             book.getAuthors()) {
            author.setBooks(null);
        }
        for (Quote quote:
             book.getQuotes()) {
            quote.getUser().setBookLists(null);
            quote.getUser().setQuotes(null);
            quote.setBook(null);
        }
          return book;
      }

    @PostMapping("/add")
    public ResponseEntity<?> addBook(@ModelAttribute AddBook book) throws Exception {
        if(book.getImageFile()[0] == null || book.getBookFile()[0] == null){
            LOGGER.info("Files not found");
            return new ResponseEntity<>("Error: File(s) not found", HttpStatus.BAD_REQUEST);
        }
        saveUploadedFile(book.getImageFile()[0], book.getTitle()+book.getAuthors().get(0)+book.getImageFile()[0].getOriginalFilename(), "/images");
        saveUploadedFile(book.getBookFile()[0], book.getTitle()+book.getAuthors().get(0)+book.getBookFile()[0].getOriginalFilename(), "/epubs");
        bookService.createBook(book);
        Book b = bookService.findByTitle(book.getTitle());
        linkAuthors(book, b);
        LOGGER.info("Book " + book.getTitle() + " created");
        return new ResponseEntity<String>("Uploaded", HttpStatus.OK);
    }
    private String saveUploadedFile(MultipartFile file, String newName, String addition) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        uploadDir.mkdirs();
        StringBuilder sb = new StringBuilder();
        String uploadFilePath = UPLOAD_DIR + addition + "/" + newName;
        byte[] bytes = file.getBytes();
        Path path = Paths.get(uploadFilePath);
        Files.write(path, bytes);
        sb.append(uploadFilePath).append(", ");
        return sb.toString();
    }

    @PutMapping("/edit/{id}")
      public ResponseEntity<?> editBook(@ModelAttribute EditBook book, @PathVariable("id") String id) throws IOException {

          Book b = bookService.getBook(Integer.parseInt(id));
          if(book.getTitle() != null){b.setTitle(book.getTitle());}
          if(book.getDescription() != null){b.setDescription(book.getDescription());}
          if(book.getGenre() != null){
              b.setGenre(Genre.valueOf(book.getGenre().toUpperCase()));
          }
          if(book.getAuthors() != null && !new HashSet<>(book.getAuthors()).equals(b.getAuthors())){
              for (Author author:
                   b.getAuthors()) {
                  author.getBooks().remove(b);
                  b.getAuthors().remove(author);
                  authorService.editAuthor(author);
                  bookService.editBook(b);
              }
              AddBook temp = new AddBook(book.getTitle(), book.getDescription(), book.getAuthors(), book.getGenre(), new MultipartFile[1], new MultipartFile[1]);
              linkAuthors(temp, b);
          }
          if(bookService.editBook(b)){
              LOGGER.info(b.getTitle() + "'s info edited");
              return new ResponseEntity<String>("Edited", HttpStatus.OK);
          }
          LOGGER.info(b.getTitle() + "couldn't be edited");
          return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);
      }

    @PutMapping("/editPicture/{id}")
      public ResponseEntity<?> editImage(@RequestBody MultipartFile image, @PathVariable("id") String id) throws IOException {
        Book book = bookService.getBook(Integer.parseInt(id));
        if(image != null){
                File i = new File(UPLOAD_DIR + "/images/" + book.getImageName());
                i.delete();
                saveUploadedFile(image, book.getImageName(), "/images");
                LOGGER.info(book.getTitle() + "'s image edited");
                return new ResponseEntity<String>("Edited", HttpStatus.OK);
            }
          LOGGER.info(book.getTitle() + "'s image couldn't be edited");
          return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);

      }

    @PutMapping("/editFile/{id}")
    public ResponseEntity<?> editFile(@ModelAttribute MultipartFile file, @PathVariable("id") String id) throws IOException {
        Book book = bookService.getBook(Integer.parseInt(id));
        if(file != null){
            File bookFile = new File(UPLOAD_DIR + "/epubs/" + book.getFileName());
            bookFile.delete();
            saveUploadedFile(file, book.getFileName(), "/epubs");
            LOGGER.info(book.getTitle() + "'s file edited");
            return new ResponseEntity<String>("Edited", HttpStatus.OK);
        }
        LOGGER.info(book.getTitle() + "'s file couldn't be edited");
        return new ResponseEntity<>("Error", HttpStatus.BAD_REQUEST);

    }

    @GetMapping("/search/{title}/{authorName}/{genre}")
    private List<Book> searchBooks(@PathVariable("title") String title, @PathVariable("authorName") String authorName, @PathVariable("genre") String genre) throws SQLException {
        SearchQuery searchQuery = new SearchQuery(title, authorName, genre);
        if(title.equals("!!!")){
            searchQuery.setTitle("");
        }
        if(authorName.equals("!!!")){
            searchQuery.setAuthorName("");
        }
        if(genre.equals("!!!")){
            searchQuery.setGenre("");
        }
        List<Book> books = bookService.searchBooks(searchQuery);
        for (Book book:
                books) {
            for (Author author:
                    book.getAuthors()) {
                author.setBooks(null);
            }
            for (Quote quote:
                    book.getQuotes()) {
                quote.setBook(null);
                quote.getUser().setBookLists(null);
                quote.getUser().setQuotes(null);
                quote.getUser().setStartedBooks(null);
                quote.getUser().setFinishedBooks(null);
                quote.getUser().setDownloadedBooks(null);
            }
            for (BookList bookList:
                    book.getBookLists()) {
                bookList.getUser().setBookLists(null);
                bookList.getUser().setQuotes(null);
                bookList.getUser().setStartedBooks(null);
                bookList.getUser().setFinishedBooks(null);
                bookList.getUser().setDownloadedBooks(null);
                bookList.setBooks(null);
            }
        }
        LOGGER.info("Search query: Title: '" + searchQuery.getTitle() + "', Author Name: '" + searchQuery.getAuthorName() + "' , Genre: '" + searchQuery.getGenre() + "' completed");
        return books;
    }

    private List<String> getAuthorsAsList(List<Author> authors){
        List<String> result = new ArrayList<>();
        for(int i = 0; i < authors.size(); i++){
            result.add(authors.get(i).getName());
        }
        return result;
    }


    @GetMapping("/getBooksByAuthor/{name}")
    private Set<Book> getBooksByAuthor(@PathVariable("name") String name){
        Author author = authorService.getAuthorByName(name);
        return author.getBooks();
    }

    @DeleteMapping("/delete/{id}")
    private boolean deleteBook(@PathVariable("id") String id){
        Book b = bookService.getBook(Integer.parseInt(id));
        File image = new File(UPLOAD_DIR + "/images/" + b.getImageName());
        File bookFile = new File(UPLOAD_DIR + "/epubs/" + b.getFileName());
        image.delete();
        bookFile.delete();
        return bookService.deleteBook(Integer.parseInt(id));
    }
    @GetMapping("/getText/{id}")
    public List<String> getText(@PathVariable("id") String id) throws IOException {
        List<String> bookContent = new ArrayList<>();
        Book b = bookService.getBook(Integer.parseInt(id));
        EpubReader epubReader = new EpubReader();
        nl.siegmann.epublib.domain.Book book = epubReader.readEpub(new FileInputStream(UPLOAD_DIR+"/epubs/" + b.getFileName()));
        for(int i = 0; i < book.getContents().size(); i++){
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
            if(textContent.length() > 0){
            textContent += "\n\n";
            bookContent.add(textContent);}
        }
        return bookContent;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadBook(@PathVariable("id") String id) throws IOException {
        if(userService.getUser() != null){
            User user = userService.getUser();
        Book book = bookService.getBook(Integer.parseInt(id));
        File file = new File(UPLOAD_DIR + "/epubs/" + book.getFileName());
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+book.getTitle()+".epub");
        if(!user.getDownloadedBooks().contains(book)){
            user.getDownloadedBooks().add(book);
            userService.editUser(user);
            userService.setUser(user);
        }
        return ResponseEntity.ok()
                .headers(header)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);}
                return null;
    }

    private boolean linkAuthors(AddBook book, Book b){
        for(int i = 0; i < book.getAuthors().size(); i++){
            AddAuthor a = new AddAuthor(book.getAuthors().get(i), new ArrayList<>());
            a.getBooks().add(b);
            if(authorService.getAuthorByName(a.getName()) == null){
                authorService.createAuthor(a);
                b.getAuthors().add(authorService.getAuthorByName(a.getName()));

                bookService.editBook(b);}
            else{
                Author aa = authorService.getAuthorByName(a.getName());
                aa.getBooks().add(b);
                authorService.editAuthor(aa);
            }
        }

        bookService.editBook(b);
        return true;
    }

    @GetMapping("/wikipedia/{word}")
    public ModelAndView searchInWikipedia(@PathVariable("word") String word) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        String fooResourceUrl = "https://bg.wikipedia.org/w/api.php?action=opensearch&search="+word + "&limit=1&namespace=0&callback=JSON_CALLBACK";
        ResponseEntity<String> response
                = restTemplate.getForEntity(fooResourceUrl + "/1", String.class);
        ObjectMapper mapper = new ObjectMapper();
        String str = response.getBody().toString();
        Pattern p = Pattern.compile("(https(.*?)\\\")");
        Matcher m = p.matcher(str);
        while(m.find())
        {
            LOGGER.info(m.group(1).substring(0, m.group(1).length() - 1));
            return new ModelAndView("redirect:" +  (m.group(1).substring(0, m.group(1).length() - 1)));

        }
        return null;

    }
}
