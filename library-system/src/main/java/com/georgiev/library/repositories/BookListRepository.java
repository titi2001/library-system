package com.georgiev.library.repositories;

import com.georgiev.library.entities.BookList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookListRepository extends JpaRepository < BookList, Integer > {
    BookList findById(int id);
    @Query(
            value = "SELECT * FROM book_lists INNER JOIN book_list_book ON book_list_book.book_id = :id AND book_list_book.book_list_id = id",
            nativeQuery = true
    )
    List < BookList > findBookListsByBook(@Param("id") int id);
}