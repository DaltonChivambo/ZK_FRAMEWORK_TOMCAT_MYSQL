package org.example.model;

public class AppUser {
    private Long id;
    private String name;
    private String email;
    private RoleType role;

    public AppUser(Long id, String name, String email, RoleType role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public RoleType getRole() {
        return role;
    }
}
