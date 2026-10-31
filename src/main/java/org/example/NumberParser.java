package org.example;

/** Decimal point, decimal comma and Arabic decimal separator; grouping is unsupported. */
public final class NumberParser {
    private NumberParser() { }
    public static double parse(String text) {
        if (text == null) throw new IllegalArgumentException("Missing number");
        StringBuilder normalized = new StringBuilder();
        for (char c : text.trim().toCharArray()) {
            int digit = Character.digit(c, 10);
            normalized.append(digit >= 0 ? (char) ('0' + digit) : c);
        }
        String value = normalized.toString().replace(',', '.').replace('٫', '.').replace('−', '-');
        if (!value.matches("[+-]?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?"))
            throw new IllegalArgumentException("Invalid number");
        double number = Double.parseDouble(value);
        if (!Double.isFinite(number)) throw new IllegalArgumentException("Non-finite number");
        return number;
    }
}
