package org.example;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController {
    @FXML private VBox root;
    @FXML private HBox headingRow;
    @FXML private Label headingPunctuation;
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
        operation.setOnAction(event -> { clearConversion(); updateDescriptions(); });
        temperature.setTextFormatter(new TextFormatter<String>(change -> {
            change.setText(localizeInput(change.getText()));
            return change;
        }));
        temperature.textProperty().addListener((observable, oldValue, newValue) -> clearConversion());
        changeLanguage(); refresh();
    }
    private String format(double number) {
        // Keep Arabic output consistent when JDK locale defaults use Latin digits.
        return localizeInput(String.format(locale, "%.2f", number));
    }
    private String localizeInput(String text) {
        boolean arabic = locale.getLanguage().equals("ar");
        StringBuilder result = new StringBuilder();
        for (char character : text.toCharArray()) {
            int digit = Character.digit(character, 10);
            if (digit >= 0) {
                result.append((char) ((arabic ? '٠' : '0') + digit));
            } else if (character == '.' || character == '٫') {
                result.append(arabic ? '٫' : '.');
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }
    private void clearConversion() { conversionResult.setText(""); warning.setText(""); }
    private void changeLanguage() {
        locale = locales[languages.getSelectionModel().getSelectedIndex()];
        temperature.setText(localizeInput(temperature.getText()));
        messages = ResourceBundle.getBundle("MessagesBundle", locale);
        root.setNodeOrientation(locale.getLanguage().equals("ar") ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
        stage.setTitle(messages.getString("title"));
        String welcome = messages.getString("welcome");
        boolean arabicHeading = locale.getLanguage().equals("ar") && welcome.endsWith("!");
        heading.setText(arabicHeading ? welcome.substring(0, welcome.length() - 1) : welcome);
        heading.setNodeOrientation(root.getNodeOrientation());
        headingRow.setAlignment(arabicHeading ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        headingPunctuation.setText(arabicHeading ? "!" : "");
        headingPunctuation.setVisible(arabicHeading);
        headingPunctuation.setManaged(arabicHeading);
        languageLabel.setText(messages.getString("language"));
        temperatureTab.setText(messages.getString("temperatureTab")); travelTab.setText(messages.getString("travelTab"));
        temperatureLabel.setText(messages.getString("temperature")); convertButton.setText(messages.getString("convert"));
        tripLabel.setText(messages.getString("trip")); distanceLabel.setText(messages.getString("distance")); timeLabel.setText(messages.getString("time"));
        saveButton.setText(messages.getString("save")); refreshButton.setText(messages.getString("refresh"));
        tripColumn.setText(messages.getString("trip")); distanceColumn.setText(messages.getString("distance")); timeColumn.setText(messages.getString("time")); speedColumn.setText(messages.getString("speed"));
        history.setPlaceholder(new Label(messages.getString("empty")));
        int selected = operation.getSelectionModel().getSelectedIndex();
        clearConversion();
        try {
            units = repository.units(locale.getLanguage());
            operation.getItems().clear();
            for (var pair : directions)
                operation.getItems().add(name(pair[0]) + " → " + name(pair[1]));
            operation.getSelectionModel().select(Math.max(0, selected));
            travelUnit(distanceLabel, distanceColumn, "distance", "km");
            travelUnit(timeLabel, timeColumn, "time", "hour");
            speedColumn.setText(name("kmh"));
            speedColumn.setGraphic(info("kmh"));
            updateDescriptions();
            loadConversions();
        } catch (SQLException e) {
            operation.getItems().clear();
            conversionResult.setText(messages.getString("databaseError"));
        }
        history.refresh();
    }
    private final String[][] directions = {{"fahrenheit","celsius"},{"celsius","fahrenheit"},
        {"celsius","kelvin"},{"kelvin","celsius"},{"fahrenheit","kelvin"},{"kelvin","fahrenheit"}};
    private java.util.Map<String, TravelRepository.UnitText> units;
    @FXML private Label descriptions, recentLabel;
    @FXML private ListView<String> recent;
    private String name(String key) { return units.get(key).name(); }
    private Label info(String key) {
        var icon = new Label("ⓘ");
        icon.setTooltip(new Tooltip(units.get(key).description()));
        return icon;
    }
    private void travelUnit(Label label, TableColumn<?,?> column, String key, String unit) {
        String caption = messages.getString(key) + " (" + name(unit) + ")";
        label.setText(caption);
        label.setTooltip(new Tooltip(units.get(unit).description()));
        column.setText(caption);
    }
    private void updateDescriptions() {
        int i = operation.getSelectionModel().getSelectedIndex();
        descriptions.setText(i < 0 ? "" : units.get(directions[i][0]).description() + " · " + units.get(directions[i][1]).description());
    }
    private void loadConversions() throws SQLException {
        recentLabel.setText(messages.getString("recent"));
        recent.getItems().setAll(repository.recentConversions().stream().map(c ->
            format(c.input()) + " " + name(c.source()) + " → " + format(c.result()) + " " + name(c.target())).toList());
    }
    @FXML private void convert() {
        try {
            int index = operation.getSelectionModel().getSelectedIndex();
            if (index < 0) return;
            var pair = directions[index];
            double value = NumberParser.parse(temperature.getText());
            double celsius = switch (pair[0]) {
                case "fahrenheit" -> converter.fahrenheitToCelsius(value);
                case "kelvin" -> converter.kelvinToCelsius(value);
                default -> value;
            };
            double result = switch (pair[1]) {
                case "fahrenheit" -> converter.celsiusToFahrenheit(celsius);
                case "kelvin" -> converter.celsiusToKelvin(celsius);
                default -> celsius;
            };
            repository.saveConversion(new TravelRepository.Conversion(value, pair[0], pair[1], result), locale.getLanguage());
            conversionResult.setText(format(result) + " " + name(pair[1]));
            warning.setText(converter.isExtremeTemperature(celsius) ? messages.getString("extreme") : "");
            loadConversions();
        } catch (SQLException e) {
            conversionResult.setText(messages.getString("databaseError"));
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
