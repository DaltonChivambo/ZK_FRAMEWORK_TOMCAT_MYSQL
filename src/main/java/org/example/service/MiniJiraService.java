package org.example.service;

import org.example.model.AppUser;
import org.example.model.Issue;
import org.example.model.IssuePriority;
import org.example.model.IssueStatus;
import org.example.model.Project;
import org.example.model.RoleType;
import org.example.model.StatusCount;
import org.example.repository.AppRepository;
import org.example.security.PasswordUtil;

import java.util.List;

public class MiniJiraService {
    private final AppRepository repository = new AppRepository();

    public List<AppUser> listUsers() {
        return repository.listUsers();
    }

    public List<AppUser> listAssignableUsers() {
        return repository.listAssignableUsers();
    }

    public void createUser(String name, String email, String rawPassword, RoleType role) {
        repository.createUser(name, email, PasswordUtil.sha256(rawPassword), role);
    }

    public void updateUserRole(Long userId, RoleType role) {
        repository.updateUserRole(userId, role);
    }

    public List<Project> listProjectsForRole(AppUser user) {
        return repository.listProjectsForRole(user);
    }

    public void createProject(String name, String description, Long managerId) {
        repository.createProject(name, description, managerId);
    }

    public List<Issue> listIssuesForRole(AppUser user) {
        return repository.listIssuesForRole(user);
    }

    public void createIssue(Long projectId, String title, String description, IssuePriority priority,
                            Long assigneeId, Long reporterId) {
        repository.createIssue(projectId, title, description, priority, assigneeId, reporterId);
    }

    public void updateIssueStatus(Long issueId, IssueStatus status, AppUser user) {
        repository.updateIssueStatus(issueId, status, user);
    }

    public List<StatusCount> statusDistributionForRole(AppUser user) {
        return repository.statusDistributionForRole(user);
    }
}
