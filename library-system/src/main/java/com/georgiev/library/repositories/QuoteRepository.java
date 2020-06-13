package com.georgiev.library.repositories;

import com.georgiev.library.entities.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote,Integer> {
    Quote findById(int id);
    List<Quote> findByBookId(int bookId);
}
