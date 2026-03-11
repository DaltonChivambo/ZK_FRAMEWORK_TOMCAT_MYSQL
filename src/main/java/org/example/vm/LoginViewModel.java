package org.example.vm;

import org.example.model.AppUser;
import org.example.security.SessionManager;
import org.example.service.AuthService;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;

public class LoginViewModel {
    private final AuthService authService = new AuthService();

    private String email;
    private String password;

    @Init
    public void init() {
        if (SessionManager.getCurrentUser() != null) {
            Executions.sendRedirect("/index.zul");
        }
    }

    @Command
    public void login() {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            Clients.showNotification("Informe email e senha.");
            return;
        }

        AppUser user = authService.login(email.trim(), password);
        if (user == null) {
            Clients.showNotification("Credenciais invalidas.");
            return;
        }

        SessionManager.setCurrentUser(user);
        Executions.sendRedirect("/index.zul");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
