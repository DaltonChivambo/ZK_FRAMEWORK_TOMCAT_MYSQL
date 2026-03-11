# ZK CRUD com Tomcat 10 e MySQL

Projeto web Java com ZK Framework (MVVM), Maven, Tomcat 10 e MySQL.

## Requisitos

- Java 11+
- Maven 3.8+
- Tomcat 10
- MySQL 8

## Configuracao do banco

### 1) Criar usuario, banco e permissoes

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

### 2) Criar tabela

Na pasta do projeto, rode:

```bash
mysql -u zkteste_user -pzkteste_user zkteste < sql/schema.sql
```

### 3) Credenciais da aplicacao

Arquivo: `src/main/resources/db.properties`

```properties
jdbc.url=jdbc:mysql://localhost:3306/zkteste?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
jdbc.user=zkteste_user
jdbc.password=zkteste_user
jdbc.driver=com.mysql.cj.jdbc.Driver
```

## Como rodar o programa

### 1) Build

```bash
mvn clean package -DskipTests
```

Gera o arquivo:

- `target/zkteste.war`

### 2) Deploy no Tomcat 10

```bash
sudo cp target/zkteste.war /var/lib/tomcat10/webapps/
sudo systemctl restart tomcat10
```

### 3) Verificar se subiu

```bash
sudo systemctl status tomcat10 --no-pager
```

### 4) Abrir no navegador

- `http://localhost:8080/zkteste/`

## Uso rapido do CRUD

- `Adicionar`: preencha nome/email e clique em adicionar
- `Atualizar`: selecione uma linha, altere os campos e clique em atualizar
- `Excluir`: selecione uma linha e clique em excluir
- `Limpar`: limpa selecao e formulario

## Problemas comuns

- `Access denied for user 'zkteste_user'@'localhost'`:
  - a senha no `db.properties` esta diferente da senha real do MySQL
  - para redefinir:
    - `ALTER USER 'zkteste_user'@'localhost' IDENTIFIED BY 'zkteste_user';`
    - `FLUSH PRIVILEGES;`

- `target/zkteste.war` nao existe:
  - rode `mvn clean package -DskipTests` antes do deploy
