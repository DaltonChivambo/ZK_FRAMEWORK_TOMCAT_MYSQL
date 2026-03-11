package org.example.vm;

import org.example.model.User;
import org.example.service.UserService;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.ListModelList;
import org.zkoss.zk.ui.util.Clients;

import java.util.regex.Pattern;

public class UserViewModel {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[\\p{L}][\\p{L} .'-]{1,79}$");

    private final UserService userService = new UserService();

    private final ListModelList<User> users = new ListModelList<>();
    private User selectedUser;
    private String formName;
    private String formEmail;

    @Init
    public void init() {
        refreshUsers();
    }

    @Command
    @NotifyChange({"selectedUser", "formName", "formEmail", "selectionHint", "actionButtonsDisabled"})
    public void selectUser(@BindingParam("user") User user) {
        this.selectedUser = user;
        this.formName = user.getName();
        this.formEmail = user.getEmail();
    }

    @Command
    @NotifyChange({"users", "selectedUser", "formName", "formEmail", "selectionHint", "actionButtonsDisabled"})
    public void addUser() {
        String validationError = validateForm();
        if (validationError != null) {
            Clients.showNotification(validationError);
            return;
        }

        userService.addUser(formName.trim(), formEmail.trim());
        refreshUsers();
        clearForm();
        Clients.showNotification("Usuario adicionado com sucesso.");
    }

    @Command
    @NotifyChange({"users", "selectedUser", "formName", "formEmail", "selectionHint", "actionButtonsDisabled"})
    public void updateUser() {
        if (selectedUser == null) {
            Clients.showNotification("Selecione um usuario para atualizar.");
            return;
        }
        String validationError = validateForm();
        if (validationError != null) {
            Clients.showNotification(validationError);
            return;
        }

        userService.updateUser(selectedUser.getId(), formName.trim(), formEmail.trim());
        refreshUsers();
        clearForm();
        Clients.showNotification("Usuario atualizado com sucesso.");
    }

    @Command
    @NotifyChange({"users", "selectedUser", "formName", "formEmail", "selectionHint", "actionButtonsDisabled"})
    public void deleteUser() {
        if (selectedUser == null) {
            Clients.showNotification("Selecione um usuario para excluir.");
            return;
        }

        userService.deleteUser(selectedUser.getId());
        refreshUsers();
        clearForm();
        Clients.showNotification("Usuario excluido com sucesso.");
    }

    @Command
    @NotifyChange({"selectedUser", "formName", "formEmail", "selectionHint", "actionButtonsDisabled"})
    public void clearForm() {
        selectedUser = null;
        formName = "";
        formEmail = "";
    }

    public ListModelList<User> getUsers() {
        return users;
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public String getSelectionHint() {
        if (selectedUser == null) {
            return "Selecione um registro para editar ou excluir.";
        }
        return "Registro selecionado: " + selectedUser.getName();
    }

    public boolean isActionButtonsDisabled() {
        return selectedUser == null;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormEmail() {
        return formEmail;
    }

    public void setFormEmail(String formEmail) {
        this.formEmail = formEmail;
    }

    private String validateForm() {
        if (formName == null || formName.trim().isEmpty()) {
            return "Informe o nome.";
        }
        if (formEmail == null || formEmail.trim().isEmpty()) {
            return "Informe o email.";
        }

        String normalizedName = formName.trim();
        String normalizedEmail = formEmail.trim();

        if (!NAME_PATTERN.matcher(normalizedName).matches()) {
            return "Nome invalido. Use ao menos 2 letras e apenas caracteres de nome.";
        }
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return "Email invalido. Exemplo: nome@dominio.com";
        }

        formName = normalizedName;
        formEmail = normalizedEmail;
        return null;
    }

    private void refreshUsers() {
        users.clear();
        users.addAll(userService.listUsers());
    }
}
