package com.georgiev.library.services.impl;

import com.georgiev.library.entities.*;
import com.georgiev.library.pojo.*;
import com.georgiev.library.repositories.BookRepository;
import com.georgiev.library.services.interfaces.IBookService;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.*;

@Service
public class BookServiceImpl implements IBookService {
    private BookRepository bookRepository;
    private AuthorServiceImpl authorService;
    private QuoteServiceImpl quoteService;
    private BookListServiceImpl bookListService;
    private UserServiceImpl userService;

    public BookServiceImpl(BookRepository bookRepository, AuthorServiceImpl authorService, QuoteServiceImpl quoteService, BookListServiceImpl bookListService, UserServiceImpl userService) {
        this.bookRepository = bookRepository;
        this.authorService = authorService;
        this.quoteService = quoteService;
        this.bookListService = bookListService;
        this.userService = userService;
    }

    @Override
    public boolean createBook(AddBook book) {
        Book b = new Book();
        b.setTitle(book.getTitle());
        b.setDescription(book.getDescription());
        Set<Author> a = new HashSet<>();
        for (String name:
             book.getAuthors()) {
            if(authorService.getAuthorByName(name) == null){
                authorService.createAuthor(new AddAuthor(name, new ArrayList<>()));
            }
            a.add(authorService.getAuthorByName(name));

        }
        b.setAuthors(a);
        b.setGenre(Genre.valueOf(book.getGenre().toUpperCase()));
        b.setImageName(book.getTitle()+book.getAuthors().get(0)+book.getImageFile()[0].getOriginalFilename());
        b.setFileName(book.getTitle()+book.getAuthors().get(0)+book.getBookFile()[0].getOriginalFilename());
        return bookRepository.save(b) != null;
    }

    @Override
    public boolean editBook(Book book) {
        return bookRepository.save(book) != null;
    }

    @Override
    public boolean deleteBook(int id) {
        long check = bookRepository.count();
        Book book = this.getBook(id);
        for (Author author:
             book.getAuthors()) {
            author.getBooks().remove(book);
            authorService.editAuthor(author);
        }
        for (Quote quote:
             book.getQuotes()) {
            quoteService.deleteQuote(quote.getId());
        }
        for (BookList bookList:
             book.getBookLists()) {
            bookList.getBooks().remove(book);
            bookListService.editBookList(bookList);
        }
        book.setQuotes(null);
        book.setAuthors(null);
        for (User user:
             userService.getAllUsers()) {
            if(user.getStartedBooks().contains(book)){
                user.getStartedBooks().remove(book);
            }
            if(user.getFinishedBooks().contains(book)){
                user.getFinishedBooks().remove(book);
            }
            if(user.getDownloadedBooks().contains(book)){
                user.getDownloadedBooks().remove(book);
            }
            userService.editUser(user);
        }
        this.editBook(book);
        bookRepository.deleteById(id);
        return bookRepository.count() == check - 1;
    }

    @Override
    public Book getBook(int id) {
        return bookRepository.findById(id);
    }

    @Override
    public Book findByTitle(String title) {
        return bookRepository.findByTitle(title);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> searchBooks(SearchQuery searchQuery) throws SQLException {
        List<Book> result = new ArrayList<>();
        List<Book> booksByTitle = new ArrayList<>();
        List<Book> booksByAuthor = new ArrayList<>();
        List<Book> booksByGenre = new ArrayList<>();
        String title = searchQuery.getTitle();
        if(searchQuery.getTitle().length() > 0){
            booksByTitle.addAll(bookRepository.searchBooksByTitle(searchQuery.getTitle()));
            if(searchQuery.getAuthorName().length() > 0){
                booksByAuthor.addAll(authorService.getAuthorByName(searchQuery.getAuthorName()).getBooks());
                if(searchQuery.getGenre().length() > 0){
                    booksByGenre.addAll(bookRepository.findByGenre(Genre.valueOf(searchQuery.getGenre().toUpperCase())));
                    booksByTitle.retainAll(booksByAuthor);
                    booksByTitle.retainAll(booksByGenre);
                }
                else{
                    booksByTitle.retainAll(booksByAuthor);
                }
                result = booksByTitle;
            }
            else if(searchQuery.getGenre().length() > 0){
                booksByGenre.addAll(bookRepository.findByGenre(Genre.valueOf(searchQuery.getGenre().toUpperCase())));
                booksByTitle.retainAll(booksByGenre);
            }
            result = booksByTitle;
        }
        else if(searchQuery.getAuthorName().length() > 0 && authorService.getAuthorByName(searchQuery.getAuthorName()) != null){
            booksByAuthor.addAll(authorService.getAuthorByName(searchQuery.getAuthorName()).getBooks());
            if(searchQuery.getGenre().length() > 0){
                booksByGenre.addAll(bookRepository.findByGenre(Genre.valueOf(searchQuery.getGenre().toUpperCase())));
                booksByAuthor.retainAll(booksByGenre);
            }
            result = booksByAuthor;
        }
        else if(searchQuery.getGenre().length() > 0){
            booksByGenre.addAll(bookRepository.findByGenre(Genre.valueOf(searchQuery.getGenre().toUpperCase())));
            result = booksByGenre;
        }
        return result;
    }
}
