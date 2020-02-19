package com.georgiev.library.repositories;

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
    List<Book> findByGenre(Genre genre);
    List<Book> findBooksByTitle(String title);
    @Query(
            value = "SELECT * FROM librarysystem.books WHERE books.title LIKE CONCAT('%',:title , '%')",
            nativeQuery = true
    )
    List<Book> searchBooksByTitle(@Param("title") String title);
}
