import org.example.TravelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TravelRepositoryTest {
    @TempDir Path directory;

    @Test void opensDatabaseAndPersistsMeasurements() throws Exception {
        String url = "jdbc:h2:file:" + directory.resolve("travel").toAbsolutePath();
        try (TravelRepository repository = DatabaseFixture.open(url)) {
            repository.save("Test trip", 120, 2);
        }
        try (TravelRepository repository = DatabaseFixture.open(url)) {
            var rows = repository.findAll();
            assertEquals(1, rows.size());
            assertEquals("Test trip", rows.getFirst().trip());
            assertEquals(60.0, rows.getFirst().speed());
        }
    }
}
