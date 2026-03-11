# ZK CRUD com Tomcat 10

Projeto de exemplo com ZK Framework + Maven + Tomcat 10 usando JDBC com MySQL.

## Requisitos

- Java 11+
- Maven 3.8+
- Tomcat 10
- MySQL 8+

## Banco de dados

1. Execute o script:

   ```bash
   mysql -u root -p < sql/schema.sql
   ```

2. Ajuste credenciais em `src/main/resources/db.properties` se necessario.

## Build

```bash
mvn clean package
```

O arquivo gerado sera `target/zkteste.war`.

## Deploy no Tomcat 10

1. Copie `target/zkteste.war` para `TOMCAT_HOME/webapps/`.
2. Inicie o Tomcat.
3. Acesse:

   - `http://localhost:8080/zkteste/`

## Fluxo CRUD

- `Adicionar`: cria novo usuario.
- `Atualizar`: selecione um usuario na lista e atualize os campos.
- `Excluir`: selecione um usuario e clique em excluir.
- `Limpar`: reseta selecao e formulario.
