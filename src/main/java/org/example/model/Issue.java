package org.example.model;

public class Issue {
    private Long id;
    private Long projectId;
    private String projectName;
    private String title;
    private String description;
    private IssueStatus status;
    private IssuePriority priority;
    private Long assigneeId;
    private String assigneeName;

    public Issue(Long id, Long projectId, String projectName, String title, String description,
                 IssueStatus status, IssuePriority priority, Long assigneeId, String assigneeName) {
        this.id = id;
        this.projectId = projectId;
        this.projectName = projectName;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assigneeId = assigneeId;
        this.assigneeName = assigneeName;
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public IssuePriority getPriority() {
        return priority;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public String getAssigneeName() {
        return assigneeName;
    }
}
