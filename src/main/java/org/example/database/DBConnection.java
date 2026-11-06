package org.example.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {
    private DBConnection() {}

    public static Connection open() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mariadb://localhost:3306/temperature_converter");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (user == null && password == null) {
            Path file = Path.of("database.local.properties");
            if (Files.exists(file)) {
                Properties settings = new Properties();
                try (var reader = Files.newBufferedReader(file)) {
                    settings.load(reader);
                } catch (IOException e) {
                    throw new SQLException("Could not read " + file.toAbsolutePath(), e);
                }
                user = settings.getProperty("DB_USER");
                password = settings.getProperty("DB_PASSWORD");
            }
        }
        if (user == null || user.isBlank() || password == null) {
            throw new SQLException("Set DB_USER and DB_PASSWORD together or launch from the project folder with database.local.properties.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
