
import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

            try {

                // Test JavaFX application startup.
                new LocalizationApp().start(stage);

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

                // Return to English.
                languages.getSelectionModel().select(0);

                // Test Fahrenheit to Celsius.
                operation.getSelectionModel().select(0);
                temperature.setText("32");
                convertButton.fire();

                assertEquals(
                        "Temperature in Celsius: 0.00 °C",
                        conversionResult.getText()
                );

                // Test Celsius to Fahrenheit.
                operation.getSelectionModel().select(1);
                temperature.setText("0");
                convertButton.fire();

                assertEquals(
                        "Temperature in Fahrenheit: 32.00 °F",
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
                        "Temperature in Celsius: 100.00 °C",
                        conversionResult.getText()
                );

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
