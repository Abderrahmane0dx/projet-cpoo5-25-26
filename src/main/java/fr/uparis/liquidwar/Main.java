package fr.uparis.liquidwar;

import fr.uparis.liquidwar.view.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main entry point for Liquid War game.
 * 
 * To run:
 * - From Gradle: ./gradlew run
 * - From IDE: Run this class
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        new MainWindow(primaryStage);
    }
    
    /**
     * Main method - launches the JavaFX application.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}