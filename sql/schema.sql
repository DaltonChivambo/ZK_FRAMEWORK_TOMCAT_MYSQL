CREATE DATABASE IF NOT EXISTS zkteste;
USE zkteste;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(40) NOT NULL UNIQUE,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    password_hash VARCHAR(128) NOT NULL,
    role_id BIGINT NOT NULL,
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(500),
    manager_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_projects_manager FOREIGN KEY (manager_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS issues (
    id BIGINT NOT NULL AUTO_INCREMENT,
    project_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    status VARCHAR(30) NOT NULL DEFAULT 'TODO',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    assignee_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_issues_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_issues_assignee FOREIGN KEY (assignee_id) REFERENCES users(id),
    CONSTRAINT fk_issues_reporter FOREIGN KEY (reporter_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS issue_comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    issue_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    comment_text VARCHAR(1200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_comments_issue FOREIGN KEY (issue_id) REFERENCES issues(id),
    CONSTRAINT fk_comments_author FOREIGN KEY (author_id) REFERENCES users(id)
);

INSERT INTO roles(name)
VALUES ('ADMIN'), ('PROJECT_MANAGER'), ('USER')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO users(name, email, password_hash, role_id)
SELECT 'Administrador', 'admin@zkteste.local', SHA2('admin123', 256), r.id
FROM roles r WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE name = VALUES(name), role_id = VALUES(role_id);

INSERT INTO users(name, email, password_hash, role_id)
SELECT 'Project Manager', 'pm@zkteste.local', SHA2('pm123', 256), r.id
FROM roles r WHERE r.name = 'PROJECT_MANAGER'
ON DUPLICATE KEY UPDATE name = VALUES(name), role_id = VALUES(role_id);

INSERT INTO users(name, email, password_hash, role_id)
SELECT 'Usuario Geral', 'user@zkteste.local', SHA2('user123', 256), r.id
FROM roles r WHERE r.name = 'USER'
ON DUPLICATE KEY UPDATE name = VALUES(name), role_id = VALUES(role_id);
