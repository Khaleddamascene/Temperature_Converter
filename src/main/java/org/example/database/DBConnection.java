package org.example.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public final class DBConnection {
    private DBConnection() {}

    public static Connection open() throws SQLException {
        return open(System.getenv(), Path.of("database.local.properties"));
    }

    static Connection open(Map<String, String> environment, Path file) throws SQLException {
        String url = environment.getOrDefault("DB_URL", "jdbc:mariadb://localhost:3306/temperature_converter");
        String user = environment.get("DB_USER");
        String password = environment.get("DB_PASSWORD");
        if (user == null && password == null) {
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
