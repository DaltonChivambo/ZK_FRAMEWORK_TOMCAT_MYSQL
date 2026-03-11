package org.example.service;

import org.example.model.AppUser;
import org.example.repository.AppRepository;
import org.example.security.PasswordUtil;

public class AuthService {
    private final AppRepository appRepository = new AppRepository();

    public AppUser login(String email, String rawPassword) {
        return appRepository.authenticate(email, PasswordUtil.sha256(rawPassword));
    }
}
