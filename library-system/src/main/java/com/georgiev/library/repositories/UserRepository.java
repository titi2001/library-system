package com.georgiev.library.repositories;

import com.georgiev.library.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    User findByUsername(String username);
    User findById(int id);
    boolean existsByUsername(String username);
}
