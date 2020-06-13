package com.georgiev.library.services.impl;

import com.georgiev.library.pojo.AddQuote;
import com.georgiev.library.entities.Book;
import com.georgiev.library.entities.Quote;
import com.georgiev.library.entities.User;
import com.georgiev.library.repositories.BookRepository;
import com.georgiev.library.repositories.QuoteRepository;
import com.georgiev.library.repositories.UserRepository;
import com.georgiev.library.services.interfaces.IQuoteService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuoteServiceImpl implements IQuoteService {
    private QuoteRepository quoteRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;

    public QuoteServiceImpl(QuoteRepository quoteRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.quoteRepository = quoteRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean createQuote(AddQuote quote) {
        Quote q = new Quote();
        q.setContent(quote.getContent());
        q.setBook(bookRepository.findById(quote.getBookId()));
        q.setUser(userRepository.findById(quote.getUserId()));
        return quoteRepository.save(q) != null;
    }

    @Override
    public boolean deleteQuote(int id) {
        long test = quoteRepository.count();
        Quote quote = quoteRepository.findById(id);
        User user = quote.getUser();
        user.getQuotes().remove(quote);
        userRepository.save(user);
        quoteRepository.deleteById(id);
        return quoteRepository.count() == test - 1;
    }

    @Override
    public Quote getQuote(int id) {
        return quoteRepository.findById(id);
    }

    @Override
    public List < Quote > getAllQuotes() {
        return quoteRepository.findAll();
    }

    @Override
    public List<Quote> getQuotesFromBook(int bookId) {
        return quoteRepository.findByBookId(bookId);
    }


}