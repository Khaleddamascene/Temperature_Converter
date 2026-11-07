package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.database.DBConnection;
import java.util.Locale;
import java.util.ResourceBundle;

public class LocalizationApp extends Application {
    private TravelRepository repository;
    @Override public void start(Stage stage) throws Exception {
        repository = new TravelRepository(DBConnection.open());
        ResourceBundle messages = ResourceBundle.getBundle("MessagesBundle", Locale.US);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org.example/main.fxml"), messages);
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.configure(repository, stage);
        stage.setScene(new Scene(root, 850, 720));
        stage.setMinWidth(700); stage.setMinHeight(650); stage.show();
    }
    @Override public void stop() throws Exception { if (repository != null) repository.close(); }
    public static void main(String[] args) { launch(args); }
}
