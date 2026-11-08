package org.example.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectionTest {
    @TempDir Path directory;
    private static final String URL = "jdbc:h2:mem:connection_test";

    @Test void environmentCredentialsTakePrecedence() throws Exception {
        var environment = Map.of("DB_URL", URL, "DB_USER", "env_user", "DB_PASSWORD", "test");
        try (var db = DBConnection.open(environment, directory)) {
            assertEquals("ENV_USER", db.getMetaData().getUserName());
        }
    }

    @Test void readsLocalCredentialsWhenBothEnvironmentValuesAreAbsent() throws Exception {
        var file = directory.resolve("database.local.properties");
        Files.writeString(file, "DB_USER=file_user\nDB_PASSWORD=test\n");
        try (var db = DBConnection.open(Map.of("DB_URL", URL), file)) {
            assertEquals("FILE_USER", db.getMetaData().getUserName());
        }
        assertThrows(SQLException.class, () ->
                DBConnection.open(Map.of("DB_URL", URL, "DB_USER", "partial"), file));
    }

    @Test void reportsMissingCredentialsAndUnreadableSettings() {
        var environment = Map.of("DB_URL", URL);
        var missing = assertThrows(SQLException.class, () ->
                DBConnection.open(environment, directory.resolve("missing.properties")));
        assertTrue(missing.getMessage().contains("DB_USER"));
        var unreadable = assertThrows(SQLException.class, () -> DBConnection.open(environment, directory));
        assertInstanceOf(java.io.IOException.class, unreadable.getCause());
    }
}
