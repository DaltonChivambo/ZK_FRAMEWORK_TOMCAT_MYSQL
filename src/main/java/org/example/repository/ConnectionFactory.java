package org.example.repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionFactory {
    private static final Properties PROPERTIES = loadProperties();

    private ConnectionFactory() {
    }

    public static Connection openConnection() throws SQLException {
        try {
            Class.forName(PROPERTIES.getProperty("jdbc.driver"));
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found.", e);
        }

        return DriverManager.getConnection(
                PROPERTIES.getProperty("jdbc.url"),
                PROPERTIES.getProperty("jdbc.user"),
                PROPERTIES.getProperty("jdbc.password")
        );
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new IllegalStateException("db.properties not found in classpath.");
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Could not load database properties.", e);
        }
    }
}
