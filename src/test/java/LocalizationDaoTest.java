import org.example.TravelRepository;
import org.example.TemperatureConverter;
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
    @Test void kelvinScale() {
        var converter = new TemperatureConverter();
        assertEquals(273.15, converter.celsiusToKelvin(0));
        assertEquals(-273.15, converter.kelvinToCelsius(0));
    }
}
