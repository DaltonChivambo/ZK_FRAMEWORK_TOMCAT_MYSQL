package org.example.repository;

import org.example.model.AppUser;
import org.example.model.Issue;
import org.example.model.IssuePriority;
import org.example.model.IssueStatus;
import org.example.model.Project;
import org.example.model.RoleType;
import org.example.model.StatusCount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppRepository {

    public AppUser authenticate(String email, String passwordHash) {
        String sql = "SELECT u.id, u.name, u.email, r.name role_name " +
                "FROM users u JOIN roles r ON r.id = u.role_id " +
                "WHERE u.email = ? AND u.password_hash = ? AND u.active = 1";
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setString(2, passwordHash);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new AppUser(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            RoleType.valueOf(rs.getString("role_name"))
                    );
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not authenticate user.", e);
        }
    }

    public List<AppUser> listUsers() {
        String sql = "SELECT u.id, u.name, u.email, r.name role_name " +
                "FROM users u JOIN roles r ON r.id = u.role_id ORDER BY u.id";
        List<AppUser> result = new ArrayList<>();
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(new AppUser(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        RoleType.valueOf(rs.getString("role_name"))
                ));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not list users.", e);
        }
    }

    public List<AppUser> listAssignableUsers() {
        String sql = "SELECT u.id, u.name, u.email, r.name role_name " +
                "FROM users u JOIN roles r ON r.id = u.role_id WHERE u.active = 1 " +
                "AND r.name IN ('USER', 'PROJECT_MANAGER') ORDER BY u.name";
        List<AppUser> result = new ArrayList<>();
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(new AppUser(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        RoleType.valueOf(rs.getString("role_name"))
                ));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not list assignable users.", e);
        }
    }

    public void createUser(String name, String email, String passwordHash, RoleType role) {
        String sql = "INSERT INTO users(name, email, password_hash, role_id) " +
                "VALUES (?, ?, ?, (SELECT id FROM roles WHERE name = ?))";
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, passwordHash);
            statement.setString(4, role.name());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create user.", e);
        }
    }

    public void updateUserRole(Long userId, RoleType role) {
        String sql = "UPDATE users SET role_id = (SELECT id FROM roles WHERE name = ?) WHERE id = ?";
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, role.name());
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update user role.", e);
        }
    }

    public List<Project> listProjectsForRole(AppUser user) {
        String sqlAll = "SELECT p.id, p.name, p.description, p.manager_id, u.name manager_name " +
                "FROM projects p JOIN users u ON u.id = p.manager_id ORDER BY p.id DESC";
        String sqlPm = "SELECT p.id, p.name, p.description, p.manager_id, u.name manager_name " +
                "FROM projects p JOIN users u ON u.id = p.manager_id " +
                "WHERE p.manager_id = ? ORDER BY p.id DESC";
        List<Project> result = new ArrayList<>();
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     user.getRole() == RoleType.PROJECT_MANAGER ? sqlPm : sqlAll
             )) {
            if (user.getRole() == RoleType.PROJECT_MANAGER) {
                statement.setLong(1, user.getId());
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new Project(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getLong("manager_id"),
                            rs.getString("manager_name")
                    ));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not list projects.", e);
        }
    }

    public void createProject(String name, String description, Long managerId) {
        String sql = "INSERT INTO projects(name, description, manager_id) VALUES(?, ?, ?)";
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, description);
            statement.setLong(3, managerId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create project.", e);
        }
    }

    public List<Issue> listIssuesForRole(AppUser user) {
        String base = "SELECT i.id, i.project_id, p.name project_name, i.title, i.description, " +
                "i.status, i.priority, i.assignee_id, a.name assignee_name " +
                "FROM issues i " +
                "JOIN projects p ON p.id = i.project_id " +
                "JOIN users a ON a.id = i.assignee_id ";
        String whereAdmin = "ORDER BY i.updated_at DESC";
        String wherePm = "WHERE p.manager_id = ? ORDER BY i.updated_at DESC";
        String whereUser = "WHERE i.assignee_id = ? ORDER BY i.updated_at DESC";

        String sql;
        if (user.getRole() == RoleType.ADMIN) {
            sql = base + whereAdmin;
        } else if (user.getRole() == RoleType.PROJECT_MANAGER) {
            sql = base + wherePm;
        } else {
            sql = base + whereUser;
        }

        List<Issue> result = new ArrayList<>();
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (user.getRole() != RoleType.ADMIN) {
                statement.setLong(1, user.getId());
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new Issue(
                            rs.getLong("id"),
                            rs.getLong("project_id"),
                            rs.getString("project_name"),
                            rs.getString("title"),
                            rs.getString("description"),
                            IssueStatus.valueOf(rs.getString("status")),
                            IssuePriority.valueOf(rs.getString("priority")),
                            rs.getLong("assignee_id"),
                            rs.getString("assignee_name")
                    ));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not list issues.", e);
        }
    }

    public void createIssue(Long projectId, String title, String description, IssuePriority priority,
                            Long assigneeId, Long reporterId) {
        String sql = "INSERT INTO issues(project_id, title, description, status, priority, assignee_id, reporter_id) " +
                "VALUES(?, ?, ?, 'TODO', ?, ?, ?)";
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            statement.setString(2, title);
            statement.setString(3, description);
            statement.setString(4, priority.name());
            statement.setLong(5, assigneeId);
            statement.setLong(6, reporterId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create issue.", e);
        }
    }

    public void updateIssueStatus(Long issueId, IssueStatus status, AppUser user) {
        String sqlAdmin = "UPDATE issues SET status = ? WHERE id = ?";
        String sqlPm = "UPDATE issues i JOIN projects p ON p.id = i.project_id " +
                "SET i.status = ? WHERE i.id = ? AND p.manager_id = ?";
        String sqlUser = "UPDATE issues SET status = ? WHERE id = ? AND assignee_id = ?";

        String sql;
        if (user.getRole() == RoleType.ADMIN) {
            sql = sqlAdmin;
        } else if (user.getRole() == RoleType.PROJECT_MANAGER) {
            sql = sqlPm;
        } else {
            sql = sqlUser;
        }

        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setLong(2, issueId);
            if (user.getRole() != RoleType.ADMIN) {
                statement.setLong(3, user.getId());
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not update issue status.", e);
        }
    }

    public List<StatusCount> statusDistributionForRole(AppUser user) {
        String base = "SELECT i.status, COUNT(*) total FROM issues i " +
                "JOIN projects p ON p.id = i.project_id ";
        String group = " GROUP BY i.status";
        String sqlAdmin = base + group;
        String sqlPm = base + "WHERE p.manager_id = ?" + group;
        String sqlUser = base + "WHERE i.assignee_id = ?" + group;

        String sql;
        if (user.getRole() == RoleType.ADMIN) {
            sql = sqlAdmin;
        } else if (user.getRole() == RoleType.PROJECT_MANAGER) {
            sql = sqlPm;
        } else {
            sql = sqlUser;
        }

        List<StatusCount> result = new ArrayList<>();
        try (Connection connection = ConnectionFactory.openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (user.getRole() != RoleType.ADMIN) {
                statement.setLong(1, user.getId());
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new StatusCount(rs.getString("status"), rs.getLong("total")));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not calculate status distribution.", e);
        }
    }
}
