package org.example;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController {
    @FXML private VBox root;
    @FXML private Label heading, languageLabel, temperatureLabel, conversionResult, warning, tripLabel, distanceLabel, timeLabel, databaseStatus;
    @FXML private Tab temperatureTab, travelTab;
    @FXML private ComboBox<String> languages, operation;
    @FXML private TextField temperature, trip, distance, hours;
    @FXML private Button convertButton, saveButton, refreshButton;
    @FXML private TableView<TravelRepository.Measurement> history;
    @FXML private TableColumn<TravelRepository.Measurement, String> tripColumn, distanceColumn, timeColumn, speedColumn;
    private final TemperatureConverter converter = new TemperatureConverter();
    private final Locale[] locales = {Locale.US, Locale.forLanguageTag("fi-FI"), Locale.forLanguageTag("sv-SE"), Locale.JAPAN, Locale.forLanguageTag("ar-AR")};
    private Locale locale = Locale.US;
    private ResourceBundle messages;
    private TravelRepository repository;
    private Stage stage;

    public void configure(TravelRepository repository, Stage stage) {
        this.repository = repository; this.stage = stage;
        languages.getItems().setAll("English", "Suomi", "Svenska", "日本語", "العربية");
        languages.getSelectionModel().select(0);
        tripColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().trip()));
        distanceColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(format(c.getValue().distance())));
        timeColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(format(c.getValue().hours())));
        speedColumn.setCellValueFactory(c -> new ReadOnlyStringWrapper(format(c.getValue().speed())));
        languages.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() >= 0) changeLanguage();
        });
        operation.setOnAction(event -> clearConversion());
        temperature.textProperty().addListener((observable, oldValue, newValue) -> clearConversion());
        changeLanguage(); refresh();
    }
    private String format(double number) { return String.format(locale, "%.2f", number); }
    private void clearConversion() { conversionResult.setText(""); warning.setText(""); }
    private void changeLanguage() {
        locale = locales[languages.getSelectionModel().getSelectedIndex()];
        messages = ResourceBundle.getBundle("MessagesBundle", locale);
        root.setNodeOrientation(locale.getLanguage().equals("ar") ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        stage.setTitle(messages.getString("title"));
        heading.setText(messages.getString("welcome")); languageLabel.setText(messages.getString("language"));
        temperatureTab.setText(messages.getString("temperatureTab")); travelTab.setText(messages.getString("travelTab"));
        temperatureLabel.setText(messages.getString("temperature")); convertButton.setText(messages.getString("convert"));
        tripLabel.setText(messages.getString("trip")); distanceLabel.setText(messages.getString("distance")); timeLabel.setText(messages.getString("time"));
        saveButton.setText(messages.getString("save")); refreshButton.setText(messages.getString("refresh"));
        tripColumn.setText(messages.getString("trip")); distanceColumn.setText(messages.getString("distance")); timeColumn.setText(messages.getString("time")); speedColumn.setText(messages.getString("speed"));
        history.setPlaceholder(new Label(messages.getString("empty")));
        int selected = operation.getSelectionModel().getSelectedIndex();
        operation.getItems().setAll(messages.getString("fToC"), messages.getString("cToF"));
        operation.getSelectionModel().select(Math.max(0, selected));
        clearConversion(); databaseStatus.setText(""); history.refresh();
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
    @FXML private void save() {
        try {
            repository.save(trip.getText(), NumberParser.parse(distance.getText()), NumberParser.parse(hours.getText()));
            loadHistory(); databaseStatus.setText(messages.getString("saved"));
        } catch (IllegalArgumentException e) { databaseStatus.setText(messages.getString("invalidTravel")); }
        catch (SQLException e) { databaseFailure(e); }
    }
    @FXML private void refresh() {
        try { loadHistory(); databaseStatus.setText(""); }
        catch (SQLException e) { databaseFailure(e); }
    }
    private void loadHistory() throws SQLException { history.getItems().setAll(repository.findAll()); }
    private void databaseFailure(SQLException e) {
        databaseStatus.setText(messages.getString("databaseError"));
        System.err.println("Database operation failed: " + e.getMessage());
    }
}
