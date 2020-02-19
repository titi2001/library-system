package com.georgiev.library.services.interfaces;

import com.georgiev.library.pojo.AddAuthor;
import com.georgiev.library.entities.Author;

import java.util.Optional;

public interface IAuthorService {
    boolean createAuthor(AddAuthor author);
    boolean editAuthor(Author author);
    boolean deleteAuthor(int id);
    Optional<Author> getAuthor(int id);
    Author getAuthorByName(String name);
}
