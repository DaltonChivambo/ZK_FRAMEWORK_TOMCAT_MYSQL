package org.example.vm;

import org.example.model.User;
import org.example.service.UserService;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.ListModelList;
import org.zkoss.zk.ui.util.Clients;

public class UserViewModel {
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
    @NotifyChange({"selectedUser", "formName", "formEmail"})
    public void selectUser(@BindingParam("user") User user) {
        this.selectedUser = user;
        this.formName = user.getName();
        this.formEmail = user.getEmail();
    }

    @Command
    @NotifyChange({"users", "selectedUser", "formName", "formEmail"})
    public void addUser() {
        if (!isValidForm()) {
            Clients.showNotification("Preencha nome e email.");
            return;
        }

        userService.addUser(formName.trim(), formEmail.trim());
        refreshUsers();
        clearForm();
        Clients.showNotification("Usuario adicionado com sucesso.");
    }

    @Command
    @NotifyChange({"users", "selectedUser", "formName", "formEmail"})
    public void updateUser() {
        if (selectedUser == null) {
            Clients.showNotification("Selecione um usuario para atualizar.");
            return;
        }
        if (!isValidForm()) {
            Clients.showNotification("Preencha nome e email.");
            return;
        }

        userService.updateUser(selectedUser.getId(), formName.trim(), formEmail.trim());
        refreshUsers();
        clearForm();
        Clients.showNotification("Usuario atualizado com sucesso.");
    }

    @Command
    @NotifyChange({"users", "selectedUser", "formName", "formEmail"})
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
    @NotifyChange({"selectedUser", "formName", "formEmail"})
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

    private boolean isValidForm() {
        return formName != null && !formName.trim().isEmpty()
                && formEmail != null && !formEmail.trim().isEmpty();
    }

    private void refreshUsers() {
        users.clear();
        users.addAll(userService.listUsers());
    }
}
