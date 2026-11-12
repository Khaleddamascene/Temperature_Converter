
import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import org.example.LocalizationApp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "guiTests", matches = "true")
class GuiTest {

    @Test
    void applicationStartupLanguageSwitchingAndConversion()
            throws Exception {

        Platform.startup(() -> { });

        FutureTask<Void> task = new FutureTask<>(() -> {

            Stage stage = new Stage();
            var app = new LocalizationApp();

            try (var repository = DatabaseFixture.open("jdbc:h2:mem:gui;DB_CLOSE_DELAY=-1")) {
                assertEquals("jdbc:h2:mem:gui;MODE=MariaDB", System.getenv("DB_URL"),
                        "Run the GUI test with Maven's isolated database settings");
                app.start(stage);

                assertNotNull(stage.getScene());

                Parent root = stage.getScene().getRoot();

                assertNotNull(root);

                root.applyCss();
                root.layout();

                @SuppressWarnings("unchecked")
                ComboBox<String> languages =
                        (ComboBox<String>) root.lookup("#languages");

                @SuppressWarnings("unchecked")
                ComboBox<String> operation =
                        (ComboBox<String>) root.lookup("#operation");

                TextField temperature =
                        (TextField) root.lookup("#temperature");

                Button convertButton =
                        (Button) root.lookup("#convertButton");

                Label conversionResult =
                        (Label) root.lookup("#conversionResult");

                Label warning =
                        (Label) root.lookup("#warning");

                assertNotNull(languages);
                assertNotNull(operation);
                assertNotNull(temperature);
                assertNotNull(convertButton);
                assertNotNull(conversionResult);
                assertNotNull(warning);

                assertEquals(5, languages.getItems().size());

                // Test all five languages.
                for (int i = 0; i < 5; i++) {

                    languages.getSelectionModel().select(i);

                    assertFalse(stage.getTitle().isBlank());

                    assertEquals(
                            i == 4
                                    ? NodeOrientation.RIGHT_TO_LEFT
                                    : NodeOrientation.LEFT_TO_RIGHT,
                            root.getNodeOrientation()
                    );
                }

                root.applyCss();
                root.layout();
                var heading = (Label) root.lookup("#heading");
                var punctuation = (Label) root.lookup("#headingPunctuation");
                assertEquals("مرحبًا بك في محول درجات الحرارة", heading.getText());
                assertEquals("!", punctuation.getText());
                assertTrue(punctuation.isVisible());
                assertTrue(punctuation.localToScene(punctuation.getBoundsInLocal()).getMaxX()
                        <= heading.localToScene(heading.getBoundsInLocal()).getMinX() + 1);

                temperature.setText("19.5");
                assertEquals("١٩٫٥", temperature.getText());
                operation.getSelectionModel().select(1);
                convertButton.fire();
                assertEquals("٦٧٫١٠ درجة فهرنهايت", conversionResult.getText(),
                        "Arabic conversion results must use Arabic digits and decimal separator");
                temperature.setText("١٢");
                assertEquals("١٢", temperature.getText());

                // Return to English.
                languages.getSelectionModel().select(0);
                assertFalse(punctuation.isManaged());
                assertEquals("12", temperature.getText());
                operation.getSelectionModel().select(0);

                // Test Fahrenheit to Celsius.
                operation.getSelectionModel().select(0);
                temperature.setText("32");
                convertButton.fire();

                assertEquals(
                        "0.00 Celsius",
                        conversionResult.getText()
                );

                // Test Celsius to Fahrenheit.
                operation.getSelectionModel().select(1);
                temperature.setText("0");
                convertButton.fire();

                assertEquals(
                        "32.00 Fahrenheit",
                        conversionResult.getText()
                );

                // Test extreme temperature warning.
                temperature.setText("100");
                convertButton.fire();

                assertEquals(
                        "Warning: Extreme temperature!",
                        warning.getText()
                );

                // Test invalid input.
                temperature.setText("abc");
                convertButton.fire();

                assertEquals(
                        "Invalid temperature. Enter a finite number.",
                        conversionResult.getText()
                );

                assertEquals("", warning.getText());

                // Test clearing results when input changes.
                temperature.setText("25");

                assertEquals("", conversionResult.getText());
                assertEquals("", warning.getText());

                // Test clearing results when conversion direction changes.
                temperature.setText("0");
                convertButton.fire();

                operation.getSelectionModel().select(0);

                assertEquals("", conversionResult.getText());
                assertEquals("", warning.getText());

                // Test another valid conversion after clearing.
                temperature.setText("212");
                convertButton.fire();

                assertEquals(
                        "100.00 Celsius",
                        conversionResult.getText()
                );

                operation.getSelectionModel().select("Celsius → Kelvin");
                temperature.setText("0");
                convertButton.fire();
                assertEquals("273.15 Kelvin", conversionResult.getText());

                operation.getSelectionModel().select("Kelvin → Celsius");
                temperature.setText("273.15");
                convertButton.fire();
                assertEquals("0.00 Celsius", conversionResult.getText());

                var tabs = (TabPane) root.lookup(".tab-pane");
                tabs.getSelectionModel().select(1);
                root.applyCss();
                root.layout();
                var trip = (TextField) root.lookup("#trip");
                var distance = (TextField) root.lookup("#distance");
                var hours = (TextField) root.lookup("#hours");
                var save = (Button) root.lookup("#saveButton");
                var refresh = (Button) root.lookup("#refreshButton");
                var status = (Label) root.lookup("#databaseStatus");
                var history = (TableView<?>) root.lookup("#history");

                trip.setText("Helsinki");
                distance.setText("120");
                hours.setText("0");
                save.fire();
                assertEquals("Enter a trip name, a nonnegative distance and a positive duration.", status.getText());
                assertTrue(history.getItems().isEmpty());

                hours.setText("2");
                save.fire();
                assertEquals("Measurement saved.", status.getText());
                assertEquals(repository.findAll(), history.getItems());
                repository.save("Tampere", 90, 1.5);
                refresh.fire();
                assertEquals("", status.getText());
                assertEquals(2, history.getItems().size());
                assertEquals(repository.findAll(), history.getItems());

                assertFalse(operation.getItems().isEmpty());
                app.stop();
                save.fire();
                assertEquals("Database operation failed.", status.getText());
                hours.setText("invalid");
                save.fire();
                assertNotEquals("Database operation failed.", status.getText());
                refresh.fire();
                assertEquals("Database operation failed.", status.getText());

                tabs.getSelectionModel().select(0);
                temperature.setText("300");
                convertButton.fire();
                assertEquals("Database operation failed.", conversionResult.getText());

                languages.getSelectionModel().select("Suomi");
                assertTrue(operation.getItems().isEmpty());
                var finnish = java.util.ResourceBundle.getBundle(
                        "MessagesBundle", java.util.Locale.forLanguageTag("fi-FI"));
                assertEquals(finnish.getString("databaseError"), conversionResult.getText());

            } finally {
                stage.close();
            }

            return null;
        });

        try {
            Platform.runLater(task);
            task.get(30, TimeUnit.SECONDS);

        } finally {
            Platform.exit();
        }
    }
}
