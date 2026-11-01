import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class LocalizationTest {

    @Test
    void allBundlesContainEveryKeyAndValidFormats() throws Exception {
        Properties base = read("");

        for (String language : List.of("", "fi_FI", "sv_SE", "ja_JP", "ar_AR")) {
            Properties translated = read(language);

            assertEquals(base.keySet(), translated.keySet(), language);
            translated.forEach((key, value) ->
                    assertFalse(value.toString().isBlank(),
                            "Blank translation: " + language + " / " + key)
            );

            Locale locale = language.isEmpty()
                    ? Locale.US
                    : Locale.forLanguageTag(language.replace('_', '-'));

            ResourceBundle bundle =
                    ResourceBundle.getBundle("MessagesBundle", locale);

            for (String key : List.of("resultCelsius", "resultFahrenheit")) {
                assertFalse(
                        String.format(locale, bundle.getString(key), 12.5).isBlank()
                );
            }
        }

        assertTrue(read("ja_JP").getProperty("welcome").contains("温度"));
        assertTrue(read("ar_AR").getProperty("language").contains("اللغة"));
    }

    private Properties read(String language) throws Exception {
        String name = "/MessagesBundle"
                + (language.isEmpty() ? "" : "_" + language)
                + ".properties";

        Properties properties = new Properties();

        try (var stream = getClass().getResourceAsStream(name)) {
            assertNotNull(stream, "Missing resource: " + name);

            try (var reader =
                         new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        }

        return properties;
    }
}