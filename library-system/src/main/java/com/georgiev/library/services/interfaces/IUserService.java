package com.georgiev.library.services.interfaces;

import com.georgiev.library.entities.User;

import java.util.List;

public interface IUserService {

    boolean createUser(String username);
    boolean editUser(User user);
    User getUser(int id);
    User getUserByUsername(String username);
    boolean existsByUsername(String username);
    List<User> getAllUsers();
}
