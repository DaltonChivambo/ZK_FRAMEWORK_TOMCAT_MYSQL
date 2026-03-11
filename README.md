# Mini Jira com ZK + Tomcat 10

Sistema estilo mini Jira com:

- login/logout
- roles (`ADMIN`, `PROJECT_MANAGER`, `USER`)
- projetos e issues com atribuicao
- dashboard com grafico de status (ZK nativo)
- interface inspirada em ferramentas de issue tracking

## Requisitos

- Java 11+
- Maven 3.8+
- Tomcat 10
- MySQL 8

## Configuracao do banco

### 1) Credenciais da aplicacao

Arquivo `src/main/resources/db.properties`:

```properties
jdbc.url=jdbc:mysql://localhost:3306/zkteste?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
jdbc.user=zkteste_user
jdbc.password=zkteste_user
jdbc.driver=com.mysql.cj.jdbc.Driver
```

### 2) Criar usuario do banco

Entre no MySQL como `root`:

```bash
mysql -u root -p
```

Execute:

```sql
CREATE DATABASE IF NOT EXISTS zkteste;
CREATE USER IF NOT EXISTS 'zkteste_user'@'localhost' IDENTIFIED BY 'zkteste_user';
GRANT ALL PRIVILEGES ON zkteste.* TO 'zkteste_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3) Criar schema completo do sistema

```bash
mysql -u zkteste_user -pzkteste_user zkteste < sql/schema.sql
```

## Como rodar

### 1) Build

```bash
mvn clean package -DskipTests
```

Gera: `target/zkteste.war`

### 2) Deploy no Tomcat 10

```bash
sudo cp target/zkteste.war /var/lib/tomcat10/webapps/
sudo systemctl restart tomcat10
```

### 3) Acessar no navegador

- `http://localhost:8080/zkteste/`
- tela inicial: `login.zul`

## Estrutura de telas (UX)

- `login.zul`: autenticação
- `dashboard.zul`: visão executiva com resumo de status
- `projects-issues.zul`: gestão de projetos e fluxo de issues
- `admin-users.zul`: administração de usuários e roles (somente ADMIN)

## Usuarios seed para login

- `admin@zkteste.local` / `admin123`
- `pm@zkteste.local` / `pm123`
- `user@zkteste.local` / `user123`

## Permissoes por role

- **ADMIN**
  - acesso total
  - gestao de usuarios e roles
  - visao global de projetos/issues
  - dashboard global de status

- **PROJECT_MANAGER**
  - cria projetos
  - cria issues e atribui responsavel
  - atualiza status de issues dos projetos que gerencia
  - dashboard de status no contexto dos seus projetos

- **USER**
  - visualiza issues atribuidas
  - atualiza status das proprias issues
  - dashboard de status das proprias tarefas

## Troubleshooting rapido

- `Access denied for user 'zkteste_user'@'localhost'`:
  - senha no MySQL nao bate com `db.properties`
  - ajuste com:
    - `ALTER USER 'zkteste_user'@'localhost' IDENTIFIED BY 'zkteste_user';`
    - `FLUSH PRIVILEGES;`

- WAR nao aparece:
  - rode `mvn clean package -DskipTests` antes do deploy

- Alteracoes nao refletidas no Tomcat:
  - remova app expandido e recopie war:
    - `sudo rm -rf /var/lib/tomcat10/webapps/zkteste /var/lib/tomcat10/webapps/zkteste.war`
    - `sudo cp target/zkteste.war /var/lib/tomcat10/webapps/`
    - `sudo systemctl restart tomcat10`
