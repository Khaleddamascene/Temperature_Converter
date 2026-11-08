import org.example.TravelRepository;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalizationDaoTest {
    @Test void unitsAndHistory() throws Exception {
        try (var db = DatabaseFixture.open("jdbc:h2:mem:localization;DB_CLOSE_DELAY=-1")) {
            var english = db.units("en");
            assertEquals(6, english.size());
            assertEquals(english, db.units("zz"));
            for (String language : new String[]{"fi","sv","ja","ar"}) {
                var localized = db.units(language);
                assertEquals(english.keySet(), localized.keySet());
                for (var key : english.keySet())
                    assertNotEquals(english.get(key).description(), localized.get(key).description());
            }
            for (int i = 0; i < 7; i++)
                db.saveConversion(new TravelRepository.Conversion(i,"celsius","kelvin",i+273.15),"ja");
            var rows = db.recentConversions();
            assertEquals(5, rows.size());
            assertEquals(6, rows.getFirst().input());
            assertEquals(2, rows.getLast().input());
        }
    }
    @Test void missingFinnishUnitFallsBackToEnglishIndividually() throws Exception {
        String url = "jdbc:h2:mem:partialFallback;DB_CLOSE_DELAY=-1";
        try (var db = DatabaseFixture.open(url);
             var connection = DriverManager.getConnection(url + ";MODE=MariaDB", "sa", "");
             var statement = connection.createStatement()) {
            var finnish = db.units("fi");
            var english = db.units("en");
            assertNotEquals(english.get("celsius"), finnish.get("celsius"));
            assertNotEquals(english.get("hour"), finnish.get("hour"));
            assertEquals(1, statement.executeUpdate(
                    "DELETE FROM unit_translation WHERE unit_id='celsius' AND lang='fi'"));
            var result = db.units("fi");
            assertEquals(english.keySet(), result.keySet());
            assertEquals(english.get("celsius"), result.get("celsius"));
            assertEquals(finnish.get("hour"), result.get("hour"));
        }
    }
}
