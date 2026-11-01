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
    void applicationStartupLanguageSwitchingAndConversion() throws Exception {
        Platform.startup(() -> { });

        FutureTask<Void> task = new FutureTask<>(() -> {
            Stage stage = new Stage();

            try {
                // Execute LocalizationApp.start()
                new LocalizationApp().start(stage);

                assertNotNull(stage.getScene());

                Parent root = stage.getScene().getRoot();
                assertNotNull(root);

                root.applyCss();
                root.layout();

                @SuppressWarnings("unchecked")
                ComboBox<String> languages =
                        (ComboBox<String>) root.lookup("#languages");

                assertNotNull(languages);
                assertEquals(5, languages.getItems().size());

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

                languages.getSelectionModel().select(0);

                TextField temperature =
                        (TextField) root.lookup("#temperature");

                Button convertButton =
                        (Button) root.lookup("#convertButton");

                Label conversionResult =
                        (Label) root.lookup("#conversionResult");

                assertNotNull(temperature);
                assertNotNull(convertButton);
                assertNotNull(conversionResult);

                temperature.setText("32");
                convertButton.fire();

                assertEquals(
                        "Temperature in Celsius: 0.00 °C",
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