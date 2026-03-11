package org.example.service;

import org.example.model.User;
import org.example.repository.JdbcUserRepository;
import org.example.repository.UserRepository;

import java.util.List;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new JdbcUserRepository();
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public void addUser(String name, String email) {
        userRepository.create(new User(null, name, email));
    }

    public void updateUser(Long id, String name, String email) {
        userRepository.update(new User(id, name, email));
    }

    public void deleteUser(Long id) {
        userRepository.delete(id);
    }
}
