package com.georgiev.library.services.impl;

import com.georgiev.library.entities.User;
import com.georgiev.library.repositories.BookRepository;
import com.georgiev.library.repositories.UserRepository;
import com.georgiev.library.services.interfaces.IUserService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
public class UserServiceImpl implements IUserService {
    private UserRepository userRepository;
    private BookRepository bookRepository;
    public UserServiceImpl(UserRepository userRepository, BookRepository bookRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public boolean createUser(String username) {
        User u = new User();
        u.setUsername(username);
        u.setBookLists(new HashSet < > ());
        u.setQuotes(new HashSet < > ());
        u.setShareActivity(false);
        u.setDownloadedBooks(new HashSet < > ());
        u.setStartedBooks(new HashSet < > ());
        u.setFinishedBooks(new HashSet < > ());
        return userRepository.save(u) != null;
    }

    @Override
    public boolean editUser(User user) {
        return userRepository.save(user) != null;
    }

    @Override
    public User getUser(int id) {
        return userRepository.findById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        if (userRepository.existsByUsername(username)) {
            return userRepository.findByUsername(username);
        }
        return null;
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public List < User > getAllUsers() {
        return userRepository.findAll();
    }

}