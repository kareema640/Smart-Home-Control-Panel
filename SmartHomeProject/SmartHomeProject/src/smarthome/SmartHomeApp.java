package smarthome;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import smarthome.controller.HomeController;
import smarthome.view.MainView;

public class SmartHomeApp extends Application {

    private HomeController controller;

    @Override
    public void start(Stage stage) {
        controller = new HomeController();
        MainView mainView = new MainView(controller);

        Scene scene = new Scene(mainView.getRoot(), 1100, 750);
        scene.getStylesheets().add(getClass().getResource("/smarthome/style.css").toExternalForm());

        stage.setTitle("Smart Home Control Panel — ESO");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) controller.stopTimer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
