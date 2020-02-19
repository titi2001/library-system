package com.georgiev.library.services.impl;


import com.georgiev.library.pojo.AddAuthor;
import com.georgiev.library.entities.Author;
import com.georgiev.library.entities.Book;
import com.georgiev.library.repositories.AuthorRepository;
import com.georgiev.library.repositories.BookRepository;
import com.georgiev.library.services.interfaces.IAuthorService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AuthorServiceImpl implements IAuthorService {
    private AuthorRepository authorRepository;
    private BookRepository bookRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public boolean createAuthor(AddAuthor author) {
        Set<Book> books = new HashSet<>();
        for (Book book:
             author.getBooks()) {
            books.add(book);
        }
        Author a = new Author(author.getName(), books);
        return authorRepository.save(a) != null;
    }

    @Override
    public boolean editAuthor(Author author) {
        return authorRepository.save(author) != null;
    }

    @Override
    public boolean deleteAuthor(int id) {
        long test = authorRepository.count();
        authorRepository.deleteById(id);
        return authorRepository.count() == test - 1;
    }

    @Override
    public Optional<Author> getAuthor(int id) {
        return authorRepository.findById(id);
    }

    @Override
    public Author getAuthorByName(String name) {
        return authorRepository.findByName(name);
    }
}
