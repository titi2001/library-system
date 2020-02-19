package com.georgiev.library.repositories;

import com.georgiev.library.entities.BookList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookListRepository extends JpaRepository<BookList,Integer> {
    BookList findById(int id);
}
