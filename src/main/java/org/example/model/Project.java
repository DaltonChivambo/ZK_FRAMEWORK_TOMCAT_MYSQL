package org.example.model;

public class Project {
    private Long id;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;

    public Project(Long id, String name, String description, Long managerId, String managerName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerId = managerId;
        this.managerName = managerName;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getManagerId() {
        return managerId;
    }

    public String getManagerName() {
        return managerName;
    }
}
