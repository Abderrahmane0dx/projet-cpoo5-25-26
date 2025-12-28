package fr.uparis.liquidwar;

import fr.uparis.liquidwar.view.WelcomeScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Liquid War");
        new WelcomeScreen(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
