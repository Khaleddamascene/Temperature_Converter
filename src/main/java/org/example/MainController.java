package org.example;

import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController {
    @FXML private VBox root;
    @FXML private Label heading, languageLabel, temperatureLabel, conversionResult, warning;
    @FXML private ComboBox<String> languages, operation;
    @FXML private TextField temperature;
    @FXML private Button convertButton;
    private final TemperatureConverter converter = new TemperatureConverter();
    private final Locale[] locales = {Locale.US, Locale.forLanguageTag("fi-FI"), Locale.forLanguageTag("sv-SE"), Locale.JAPAN, Locale.forLanguageTag("ar-AR")};
    private Locale locale = Locale.US;
    private ResourceBundle messages;
    private Stage stage;

    public void configure(Stage stage) {
        this.stage = stage;
        languages.getItems().setAll("English", "Suomi", "Svenska", "日本語", "العربية");
        languages.getSelectionModel().select(0);
        languages.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() >= 0) changeLanguage();
        });
        operation.setOnAction(event -> clearConversion());
        temperature.textProperty().addListener((observable, oldValue, newValue) -> clearConversion());
        changeLanguage();
    }
    private void clearConversion() { conversionResult.setText(""); warning.setText(""); }
    private void changeLanguage() {
        locale = locales[languages.getSelectionModel().getSelectedIndex()];
        messages = ResourceBundle.getBundle("MessagesBundle", locale);
        root.setNodeOrientation(locale.getLanguage().equals("ar") ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        stage.setTitle(messages.getString("title"));
        heading.setText(messages.getString("welcome")); languageLabel.setText(messages.getString("language"));
        temperatureLabel.setText(messages.getString("temperature")); convertButton.setText(messages.getString("convert"));
        int selected = operation.getSelectionModel().getSelectedIndex();
        operation.getItems().setAll(messages.getString("fToC"), messages.getString("cToF"));
        operation.getSelectionModel().select(Math.max(0, selected));
        clearConversion();
    }
    @FXML private void convert() {
        try {
            double value = NumberParser.parse(temperature.getText());
            boolean toCelsius = operation.getSelectionModel().getSelectedIndex() == 0;
            double result = toCelsius ? converter.fahrenheitToCelsius(value) : converter.celsiusToFahrenheit(value);
            conversionResult.setText(String.format(locale, messages.getString(toCelsius ? "resultCelsius" : "resultFahrenheit"), result));
            warning.setText(converter.isExtremeTemperature(toCelsius ? result : value) ? messages.getString("extreme") : "");
        } catch (IllegalArgumentException e) {
            conversionResult.setText(messages.getString("invalidNumber")); warning.setText("");
        }
    }
}
