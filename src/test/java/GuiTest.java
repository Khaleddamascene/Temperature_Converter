import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.MainController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "guiTests", matches = "true")
class GuiTest {

    @Test
    void fxmlLanguageSwitchingAndConversion() throws Exception {
        Platform.startup(() -> { });

        FutureTask<Void> task = new FutureTask<>(() -> {
            var fxml = GuiTest.class.getResource("/org.example/main.fxml");
            assertNotNull(fxml, "Missing resource: /org.example/main.fxml");

            FXMLLoader loader = new FXMLLoader(
                    fxml,
                    ResourceBundle.getBundle("MessagesBundle", Locale.US)
            );

            Parent root = loader.load();
            Stage stage = new Stage();

            try {
                MainController controller = loader.getController();
                controller.configure(stage);

                @SuppressWarnings("unchecked")
                ComboBox<String> languages =
                        (ComboBox<String>) loader.getNamespace().get("languages");

                assertNotNull(languages, "Missing fx:id: languages");
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
                        (TextField) loader.getNamespace().get("temperature");
                Button convertButton =
                        (Button) loader.getNamespace().get("convertButton");
                Label conversionResult =
                        (Label) loader.getNamespace().get("conversionResult");

                assertNotNull(temperature, "Missing fx:id: temperature");
                assertNotNull(convertButton, "Missing fx:id: convertButton");
                assertNotNull(conversionResult, "Missing fx:id: conversionResult");

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