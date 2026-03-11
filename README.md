# ZK CRUD com Tomcat 10 + MySQL

Projeto web Java com ZK Framework (MVVM), Maven, Tomcat 10 e MySQL.

## Requisitos

- Java 11 ou superior
- Maven 3.8 ou superior
- Tomcat 10
- MySQL 8

## Estrutura do projeto

- UI ZK: `src/main/webapp/index.zul`
- Config web: `src/main/webapp/WEB-INF/web.xml` e `src/main/webapp/WEB-INF/zk.xml`
- Config banco: `src/main/resources/db.properties`
- Script SQL: `sql/schema.sql`

## Credenciais do banco (como configurar)

Edite o arquivo `src/main/resources/db.properties`:

```properties
jdbc.url=jdbc:mysql://localhost:3306/zkteste?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
jdbc.user=root
jdbc.password=root
jdbc.driver=com.mysql.cj.jdbc.Driver
```

Se sua senha do MySQL root nao for `root`, altere somente `jdbc.password`.

## Criar banco e tabela

No terminal, dentro da pasta do projeto:

```bash
mysql -u root -p < sql/schema.sql
```

Esse script cria:

- banco `zkteste`
- tabela `users`

## Inserir dados no banco manualmente (opcional)

Entre no MySQL:

```bash
mysql -u root -p
```

Depois rode:

```sql
USE zkteste;

INSERT INTO users (name, email) VALUES
('Ana Silva', 'ana@example.com'),
('Carlos Santos', 'carlos@example.com');

SELECT * FROM users;
```

## Build do projeto

Gere o WAR:

```bash
mvn clean package -DskipTests
```

Arquivo gerado:

- `target/zkteste.war`

## Deploy no Tomcat 10 (Ubuntu/Debian)

No seu ambiente, o deploy e feito em ` /var/lib/tomcat10/webapps/ `.

```bash
sudo cp target/zkteste.war /var/lib/tomcat10/webapps/
sudo systemctl restart tomcat10
```

## Acesso da aplicacao

Abra no navegador:

- `http://localhost:8080/zkteste/`

## Como usar o CRUD

- `Adicionar`: preencha nome/email e clique em adicionar
- `Atualizar`: clique em uma linha da lista, edite os campos e clique em atualizar
- `Excluir`: selecione uma linha e clique em excluir
- `Limpar`: limpa selecao e formulario

## Problemas comuns

- Erro `Access denied for user 'root'@'localhost'`:
  - senha em `db.properties` diferente da senha real do MySQL
  - ajuste `jdbc.password` e gere/deploy novamente

- WAR nao encontrado:
  - rode `mvn clean package -DskipTests` antes do `cp`
