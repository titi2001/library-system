package com.georgiev.library.repositories;

import com.georgiev.library.entities.Author;
import com.georgiev.library.entities.Book;
import com.georgiev.library.pojo.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book,Integer> {
    Book findByTitle(String title);
    Book findById(int id);
    @Query(
            value = "SELECT * FROM books INNER JOIN book_author ON book_author.author_id = :id AND book_id = id",
            nativeQuery = true
    )
    List<Book> findBooksByAuthor(@Param("id") int id);
    List<Book> findByGenre(Genre genre);
    List<Book> findBooksByTitleContaining(String title);
}
