package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationApp extends Application {
    @Override public void start(Stage stage) throws Exception {
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", Locale.US);
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org.example/main.fxml"),
                messages
        );
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.configure(stage);
        stage.setScene(new Scene(root, 720, 480));
        stage.setMinWidth(600); stage.setMinHeight(420); stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
