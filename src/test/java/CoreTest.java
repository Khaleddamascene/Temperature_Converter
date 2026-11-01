import org.example.NumberParser;
import org.example.TemperatureConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class CoreTest {
    private final TemperatureConverter converter = new TemperatureConverter();
    @ParameterizedTest @CsvSource({"32,0", "212,100", "-40,-40", "98.6,37"})
    void conversions(double f, double c) {
        assertEquals(c, converter.fahrenheitToCelsius(f), 1e-9);
        assertEquals(f, converter.celsiusToFahrenheit(c), 1e-9);
    }
    @Test void extremeBoundaries() {
        assertTrue(converter.isExtremeTemperature(-40.01));
        assertFalse(converter.isExtremeTemperature(-40));
        assertFalse(converter.isExtremeTemperature(50));
        assertTrue(converter.isExtremeTemperature(50.01));
    }
    @Test void rejectsNonFiniteTemperaturesAndOverflow() {
        for (double value : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> converter.fahrenheitToCelsius(value));
            assertThrows(IllegalArgumentException.class, () -> converter.celsiusToFahrenheit(value));
            assertThrows(IllegalArgumentException.class, () -> converter.isExtremeTemperature(value));
        }
        assertThrows(IllegalArgumentException.class, () -> converter.celsiusToFahrenheit(Double.MAX_VALUE));
    }
    @ParameterizedTest @CsvSource({"' 12.5 ',12.5", "'12,5',12.5", "١٢٫٥,12.5", "−2,-2", "1e2,100", ".5,0.5"})
    void parsesNumbers(String input, double expected) { Assertions.assertEquals(expected, NumberParser.parse(input)); }
    @ParameterizedTest @ValueSource(strings={"", "abc", "NaN", "Infinity", "1e999", "1,2.3", "1 000", "0x10", "12d"})
    void rejectsBadNumbers(String input) { assertThrows(IllegalArgumentException.class, () -> NumberParser.parse(input)); }
    @Test void nullNumber() { assertThrows(IllegalArgumentException.class, () -> NumberParser.parse(null)); }
}
