package org.example.vm;

import org.example.model.AppUser;
import org.example.model.Issue;
import org.example.model.IssuePriority;
import org.example.model.IssueStatus;
import org.example.model.Project;
import org.example.model.RoleType;
import org.example.model.StatusCount;
import org.example.security.SessionManager;
import org.example.service.MiniJiraService;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.ListModelList;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DashboardViewModel {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[\\p{L}][\\p{L} .'-]{1,79}$");

    private final MiniJiraService service = new MiniJiraService();

    private AppUser currentUser;

    private final ListModelList<AppUser> users = new ListModelList<>();
    private final ListModelList<AppUser> assignableUsers = new ListModelList<>();
    private final ListModelList<Project> projects = new ListModelList<>();
    private final ListModelList<Issue> issues = new ListModelList<>();

    private AppUser selectedUser;
    private Issue selectedIssue;
    private Project selectedProject;
    private AppUser selectedAssignee;

    private String newUserName;
    private String newUserEmail;
    private String newUserPassword;
    private RoleType newUserRole = RoleType.USER;

    private String newProjectName;
    private String newProjectDescription;

    private String newIssueTitle;
    private String newIssueDescription;
    private IssuePriority newIssuePriority = IssuePriority.MEDIUM;
    private IssueStatus selectedIssueStatus = IssueStatus.TODO;

    private long todoCount;
    private long inProgressCount;
    private long doneCount;
    private long totalIssuesCount;
    private String page = "dashboard";

    @Init
    @NotifyChange("*")
    public void init(@ExecutionArgParam("page") String pageArg) {
        if (pageArg != null && !pageArg.trim().isEmpty()) {
            page = pageArg.trim();
        }
        SessionManager.ensureAuthenticatedOrRedirect();
        currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        if ("admin-users".equals(page) && !hasRole(RoleType.ADMIN)) {
            Executions.sendRedirect("/dashboard.zul");
            return;
        }
        refreshAll();
    }

    @Command
    public void logout() {
        SessionManager.logout();
        Executions.sendRedirect("/login.zul");
    }

    @Command
    @NotifyChange("*")
    public void refresh() {
        refreshAll();
    }

    @Command
    @NotifyChange("*")
    public void selectIssue(@BindingParam("issue") Issue issue) {
        selectedIssue = issue;
        selectedIssueStatus = issue.getStatus();
    }

    @Command
    @NotifyChange("*")
    public void createUser() {
        ensureRole(RoleType.ADMIN);

        String validationError = validationForUserForm();
        if (validationError != null) {
            Clients.showNotification(validationError);
            return;
        }

        service.createUser(newUserName.trim(), newUserEmail.trim(), newUserPassword, newUserRole);
        clearUserForm();
        refreshAll();
        Clients.showNotification("Usuario criado com sucesso.");
    }

    @Command
    @NotifyChange("*")
    public void changeRole() {
        ensureRole(RoleType.ADMIN);
        if (selectedUser == null) {
            Clients.showNotification("Selecione um usuario.");
            return;
        }
        service.updateUserRole(selectedUser.getId(), newUserRole);
        refreshAll();
        Clients.showNotification("Role atualizada com sucesso.");
    }

    @Command
    @NotifyChange("*")
    public void createProject() {
        ensurePmOrAdmin();
        if (newProjectName == null || newProjectName.trim().length() < 3) {
            Clients.showNotification("Nome do projeto invalido.");
            return;
        }
        Long managerId = currentUser.getId();
        service.createProject(newProjectName.trim(), safeTrim(newProjectDescription), managerId);
        newProjectName = "";
        newProjectDescription = "";
        refreshAll();
        Clients.showNotification("Projeto criado.");
    }

    @Command
    @NotifyChange("*")
    public void createIssue() {
        ensurePmOrAdmin();
        if (selectedProject == null) {
            Clients.showNotification("Selecione um projeto.");
            return;
        }
        if (selectedAssignee == null) {
            Clients.showNotification("Selecione um responsavel.");
            return;
        }
        if (newIssueTitle == null || newIssueTitle.trim().length() < 4) {
            Clients.showNotification("Titulo da issue invalido.");
            return;
        }
        service.createIssue(
                selectedProject.getId(),
                newIssueTitle.trim(),
                safeTrim(newIssueDescription),
                newIssuePriority,
                selectedAssignee.getId(),
                currentUser.getId()
        );
        newIssueTitle = "";
        newIssueDescription = "";
        newIssuePriority = IssuePriority.MEDIUM;
        refreshAll();
        Clients.showNotification("Issue criada.");
    }

    @Command
    @NotifyChange("*")
    public void updateIssueStatus() {
        if (selectedIssue == null) {
            Clients.showNotification("Selecione uma issue.");
            return;
        }
        service.updateIssueStatus(selectedIssue.getId(), selectedIssueStatus, currentUser);
        refreshAll();
        Clients.showNotification("Status atualizado.");
    }

    private void refreshAll() {
        users.clear();
        users.addAll(service.listUsers());

        assignableUsers.clear();
        assignableUsers.addAll(service.listAssignableUsers());

        projects.clear();
        projects.addAll(service.listProjectsForRole(currentUser));

        issues.clear();
        issues.addAll(service.listIssuesForRole(currentUser));

        selectedIssue = null;
        selectedIssueStatus = IssueStatus.TODO;
        rebuildStatusSummary();
    }

    private void rebuildStatusSummary() {
        todoCount = 0;
        inProgressCount = 0;
        doneCount = 0;
        for (StatusCount each : service.statusDistributionForRole(currentUser)) {
            if ("TODO".equals(each.getStatus())) {
                todoCount = each.getTotal();
            } else if ("IN_PROGRESS".equals(each.getStatus())) {
                inProgressCount = each.getTotal();
            } else if ("DONE".equals(each.getStatus())) {
                doneCount = each.getTotal();
            }
        }
        totalIssuesCount = todoCount + inProgressCount + doneCount;
    }

    private String validationForUserForm() {
        if (newUserName == null || !NAME_PATTERN.matcher(newUserName.trim()).matches()) {
            return "Nome do usuario invalido.";
        }
        if (newUserEmail == null || !EMAIL_PATTERN.matcher(newUserEmail.trim()).matches()) {
            return "Email do usuario invalido.";
        }
        if (newUserPassword == null || newUserPassword.length() < 6) {
            return "Senha precisa ter pelo menos 6 caracteres.";
        }
        return null;
    }

    private void clearUserForm() {
        newUserName = "";
        newUserEmail = "";
        newUserPassword = "";
        newUserRole = RoleType.USER;
    }

    private void ensureRole(RoleType role) {
        if (!hasRole(role)) {
            throw new IllegalStateException("Acesso negado.");
        }
    }

    private void ensurePmOrAdmin() {
        if (!hasRole(RoleType.ADMIN) && !hasRole(RoleType.PROJECT_MANAGER)) {
            throw new IllegalStateException("Acesso negado.");
        }
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    public AppUser getCurrentUser() {
        return currentUser;
    }

    public String getRoleDisplay() {
        if (currentUser == null) {
            return "ANON";
        }
        return currentUser.getRole().name().replace('_', ' ');
    }

    public String getWelcomeText() {
        if (currentUser == null) {
            return "Bem-vindo";
        }
        return "Bem-vindo, " + currentUser.getName();
    }

    public boolean isAdmin() {
        return hasRole(RoleType.ADMIN);
    }

    public boolean isProjectManager() {
        return hasRole(RoleType.PROJECT_MANAGER);
    }

    public boolean isUserRole() {
        return hasRole(RoleType.USER);
    }

    public boolean isCanManageWork() {
        return hasRole(RoleType.ADMIN) || hasRole(RoleType.PROJECT_MANAGER);
    }

    public boolean isDashboardPage() {
        return "dashboard".equals(page);
    }

    public boolean isProjectsIssuesPage() {
        return "projects-issues".equals(page);
    }

    public boolean isAdminUsersPage() {
        return "admin-users".equals(page);
    }

    private boolean hasRole(RoleType role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public ListModelList<AppUser> getUsers() {
        return users;
    }

    public ListModelList<AppUser> getAssignableUsers() {
        return assignableUsers;
    }

    public ListModelList<Project> getProjects() {
        return projects;
    }

    public ListModelList<Issue> getIssues() {
        return issues;
    }

    public AppUser getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(AppUser selectedUser) {
        this.selectedUser = selectedUser;
    }

    public Issue getSelectedIssue() {
        return selectedIssue;
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(Project selectedProject) {
        this.selectedProject = selectedProject;
    }

    public AppUser getSelectedAssignee() {
        return selectedAssignee;
    }

    public void setSelectedAssignee(AppUser selectedAssignee) {
        this.selectedAssignee = selectedAssignee;
    }

    public String getNewUserName() {
        return newUserName;
    }

    public void setNewUserName(String newUserName) {
        this.newUserName = newUserName;
    }

    public String getNewUserEmail() {
        return newUserEmail;
    }

    public void setNewUserEmail(String newUserEmail) {
        this.newUserEmail = newUserEmail;
    }

    public String getNewUserPassword() {
        return newUserPassword;
    }

    public void setNewUserPassword(String newUserPassword) {
        this.newUserPassword = newUserPassword;
    }

    public RoleType getNewUserRole() {
        return newUserRole;
    }

    public void setNewUserRole(RoleType newUserRole) {
        this.newUserRole = newUserRole;
    }

    public List<RoleType> getRoleOptions() {
        return Arrays.asList(RoleType.values());
    }

    public String getNewProjectName() {
        return newProjectName;
    }

    public void setNewProjectName(String newProjectName) {
        this.newProjectName = newProjectName;
    }

    public String getNewProjectDescription() {
        return newProjectDescription;
    }

    public void setNewProjectDescription(String newProjectDescription) {
        this.newProjectDescription = newProjectDescription;
    }

    public String getNewIssueTitle() {
        return newIssueTitle;
    }

    public void setNewIssueTitle(String newIssueTitle) {
        this.newIssueTitle = newIssueTitle;
    }

    public String getNewIssueDescription() {
        return newIssueDescription;
    }

    public void setNewIssueDescription(String newIssueDescription) {
        this.newIssueDescription = newIssueDescription;
    }

    public IssuePriority getNewIssuePriority() {
        return newIssuePriority;
    }

    public void setNewIssuePriority(IssuePriority newIssuePriority) {
        this.newIssuePriority = newIssuePriority;
    }

    public List<IssuePriority> getPriorityOptions() {
        return Arrays.asList(IssuePriority.values());
    }

    public IssueStatus getSelectedIssueStatus() {
        return selectedIssueStatus;
    }

    public void setSelectedIssueStatus(IssueStatus selectedIssueStatus) {
        this.selectedIssueStatus = selectedIssueStatus;
    }

    public List<IssueStatus> getIssueStatusOptions() {
        return Arrays.asList(IssueStatus.values());
    }

    public long getTodoCount() {
        return todoCount;
    }

    public long getInProgressCount() {
        return inProgressCount;
    }

    public long getDoneCount() {
        return doneCount;
    }

    public long getTotalIssuesCount() {
        return totalIssuesCount;
    }

    public int getTodoPercent() {
        return toPercent(todoCount);
    }

    public int getInProgressPercent() {
        return toPercent(inProgressCount);
    }

    public int getDonePercent() {
        return toPercent(doneCount);
    }

    public String getTodoPercentLabel() {
        return getTodoPercent() + "%";
    }

    public String getInProgressPercentLabel() {
        return getInProgressPercent() + "%";
    }

    public String getDonePercentLabel() {
        return getDonePercent() + "%";
    }

    public String getTotalIssuesLabel() {
        return "Total de issues: " + totalIssuesCount;
    }

    public String getTodoProgressLabel() {
        return "TODO - " + getTodoPercent() + "%";
    }

    public String getInProgressProgressLabel() {
        return "IN_PROGRESS - " + getInProgressPercent() + "%";
    }

    public String getDoneProgressLabel() {
        return "DONE - " + getDonePercent() + "%";
    }

    private int toPercent(long part) {
        if (totalIssuesCount == 0) {
            return 0;
        }
        return (int) Math.round((part * 100.0) / totalIssuesCount);
    }
}
