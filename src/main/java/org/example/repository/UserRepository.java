package org.example.repository;

import org.example.model.User;

import java.util.List;

public interface UserRepository {
    List<User> findAll();

    void create(User user);

    void update(User user);

    void delete(Long id);
}
