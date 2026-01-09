package fr.uparis.liquidwar.view;

import fr.uparis.liquidwar.algorithm.AIController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 * Main menu window for the game.
 * Allows selecting game mode and AI difficulty.
 */
public class MenuWindow {
    
    private final Stage stage;
    private AIController.Difficulty selectedDifficulty = AIController.Difficulty.MEDIUM;
    
    /**
     * Creates the menu window.
     * 
     * @param stage JavaFX stage
     */
    public MenuWindow(Stage stage) {
        this.stage = stage;
        setupMenu();
    }
    
    /**
     * Sets up the main menu UI.
     */
    private void setupMenu() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");
        
        // Title
        Label title = new Label("LIQUID WAR");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.CYAN);
        shadow.setRadius(10);
        title.setEffect(shadow);
        
        // Subtitle
        Label subtitle = new Label("CPOO Project");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.LIGHTGRAY);
        subtitle.setAlignment(Pos.CENTER);
        
        // Spacer
        VBox spacer = new VBox();
        spacer.setMinHeight(30);
        
        // Play vs AI button
        Button playVsAIButton = createMenuButton("🤖  Jouer contre l'IA");
        playVsAIButton.setOnAction(e -> showDifficultySelection());
        
        // Multiplayer button
        Button multiplayerButton = createMenuButton("👥  Multijoueur Local");
        multiplayerButton.setStyle(multiplayerButton.getStyle().replace("#3498db", "#9b59b6"));
        multiplayerButton.setOnAction(e -> startMultiplayer());
        
        // Quit button
        Button quitButton = createMenuButton("❌  Quitter");
        quitButton.setStyle(quitButton.getStyle() + "-fx-background-color: #c0392b;");
        quitButton.setOnAction(e -> stage.close());
        
        // Credits
        Label credits = new Label("Appuyez sur ECHAP pour mettre en pause");
        credits.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        credits.setTextFill(Color.GRAY);
        credits.setAlignment(Pos.CENTER);
        credits.setTextAlignment(TextAlignment.CENTER);
        
        root.getChildren().addAll(title, subtitle, spacer, playVsAIButton, multiplayerButton, quitButton, credits);
        
        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
        stage.setTitle("Liquid War - Menu");
        stage.setResizable(false);
        stage.show();
    }
    
    /**
     * Starts local multiplayer mode.
     */
    private void startMultiplayer() {
        new MainWindow(stage, true);
    }
    
    /**
     * Shows the difficulty selection screen.
     */
    private void showDifficultySelection() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");
        
        // Title
        Label title = new Label("Choisir la difficulté");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);
        title.setAlignment(Pos.CENTER);
        
        // Description
        Label description = new Label("L'IA bleue s'adaptera à votre niveau");
        description.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        description.setTextFill(Color.LIGHTGRAY);
        description.setAlignment(Pos.CENTER);
        description.setTextAlignment(TextAlignment.CENTER);
        
        // Spacer
        VBox spacer = new VBox();
        spacer.setMinHeight(20);
        
        // Easy button
        Button easyButton = createDifficultyButton("🟢  Facile", "IA lente, stratégie basique");
        easyButton.setStyle(easyButton.getStyle() + "-fx-background-color: #27ae60;");
        easyButton.setOnAction(e -> startGame(AIController.Difficulty.EASY));
        
        // Medium button
        Button mediumButton = createDifficultyButton("🟡  Moyen", "IA équilibrée");
        mediumButton.setStyle(mediumButton.getStyle() + "-fx-background-color: #f39c12;");
        mediumButton.setOnAction(e -> startGame(AIController.Difficulty.MEDIUM));
        
        // Hard button
        Button hardButton = createDifficultyButton("🔴  Difficile", "IA rapide et agressive");
        hardButton.setStyle(hardButton.getStyle() + "-fx-background-color: #c0392b;");
        hardButton.setOnAction(e -> startGame(AIController.Difficulty.HARD));
        
        // Back button
        Button backButton = createMenuButton("⬅  Retour");
        backButton.setStyle(backButton.getStyle() + "-fx-background-color: #7f8c8d;");
        backButton.setOnAction(e -> setupMenu());
        
        root.getChildren().addAll(title, description, spacer, easyButton, mediumButton, hardButton, backButton);
        
        Scene scene = new Scene(root, 500, 500);
        stage.setScene(scene);
    }
    
    /**
     * Creates a styled menu button.
     */
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        button.setPrefWidth(300);
        button.setPrefHeight(50);
        button.setAlignment(Pos.CENTER);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-alignment: center;"
        );
        
        // Hover effects
        button.setOnMouseEntered(e -> {
            button.setStyle(
                button.getStyle().replace("-fx-background-color: #3498db;", "-fx-background-color: #2980b9;")
                    .replace("-fx-background-color: #27ae60;", "-fx-background-color: #1e8449;")
                    .replace("-fx-background-color: #f39c12;", "-fx-background-color: #d68910;")
                    .replace("-fx-background-color: #c0392b;", "-fx-background-color: #a93226;")
                    .replace("-fx-background-color: #7f8c8d;", "-fx-background-color: #5d6d7e;")
            );
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
        
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        
        return button;
    }
    
    /**
     * Creates a difficulty button with description.
     */
    private Button createDifficultyButton(String text, String description) {
        Button button = createMenuButton(text + "\n" + description);
        button.setPrefHeight(70);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        return button;
    }
    
    /**
     * Starts the game with selected difficulty.
     * 
     * @param difficulty selected AI difficulty
     */
    private void startGame(AIController.Difficulty difficulty) {
        this.selectedDifficulty = difficulty;
        new MainWindow(stage, difficulty);
    }
    
    /**
     * @return the selected difficulty
     */
    public AIController.Difficulty getSelectedDifficulty() {
        return selectedDifficulty;
    }
}
