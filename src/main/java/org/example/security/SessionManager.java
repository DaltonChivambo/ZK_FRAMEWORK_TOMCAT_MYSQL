package org.example.security;

import org.example.model.AppUser;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

public final class SessionManager {
    private static final String SESSION_USER_KEY = "AUTH_USER";

    private SessionManager() {
    }

    public static void setCurrentUser(AppUser user) {
        Sessions.getCurrent().setAttribute(SESSION_USER_KEY, user);
    }

    public static AppUser getCurrentUser() {
        Session session = Sessions.getCurrent(false);
        if (session == null) {
            return null;
        }
        return (AppUser) session.getAttribute(SESSION_USER_KEY);
    }

    public static void logout() {
        Session session = Sessions.getCurrent(false);
        if (session != null) {
            session.removeAttribute(SESSION_USER_KEY);
        }
    }

    public static void ensureAuthenticatedOrRedirect() {
        if (getCurrentUser() == null) {
            Executions.sendRedirect("/login.zul");
        }
    }
}
