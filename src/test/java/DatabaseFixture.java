import org.example.TravelRepository;

import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;

final class DatabaseFixture {
    static TravelRepository open(String url) throws Exception {
        String testUrl = url + ";MODE=MariaDB";
        try (var db = DriverManager.getConnection(testUrl, "sa", "");
             var query = db.createStatement();
             var script = DatabaseFixture.class.getResourceAsStream("/init.sql")) {
            String sql = new String(script.readAllBytes(), StandardCharsets.UTF_8);
            // H2 already has a database. Run only the table definitions and seed data.
            for (String command : sql.substring(sql.indexOf("CREATE TABLE")).split(";")) {
                if (!command.isBlank()) query.execute(command);
            }
        }
        return new TravelRepository(testUrl, "sa", "");
    }
}
