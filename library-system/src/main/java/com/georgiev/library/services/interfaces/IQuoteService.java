package com.georgiev.library.services.interfaces;

import com.georgiev.library.pojo.AddQuote;
import com.georgiev.library.entities.Quote;

import java.util.List;

public interface IQuoteService {
    boolean createQuote(AddQuote quote);
    boolean deleteQuote(int id);
    Quote getQuote(int id);
    List<Quote> getAllQuotes();
    List<Quote> getQuotesFromBook(int bookId);

}
