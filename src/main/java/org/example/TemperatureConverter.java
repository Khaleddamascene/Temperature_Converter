package org.example;

public class TemperatureConverter {
    public double fahrenheitToCelsius(double fahrenheit) {
        return finite((finite(fahrenheit) - 32) / 9 * 5);
    }
    public double celsiusToFahrenheit(double celsius) {
        return finite(finite(celsius) / 5 * 9 + 32);
    }
    public double celsiusToKelvin(double value) { return finite(value + 273.15); }
    public double kelvinToCelsius(double value) { return finite(value - 273.15); }
    public boolean isExtremeTemperature(double celsius) {
        finite(celsius);
        return celsius < -40 || celsius > 50;
    }
    private double finite(double number) {
        if (!Double.isFinite(number)) throw new IllegalArgumentException("Non-finite temperature");
        return number;
    }
}
