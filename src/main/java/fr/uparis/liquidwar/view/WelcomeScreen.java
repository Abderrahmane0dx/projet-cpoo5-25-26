package fr.uparis.liquidwar.view;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Modern welcome screen with flowing pixel particles.
 */
public class WelcomeScreen {
    private Stage stage;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 600;
    private Random random = new Random();
    
    public WelcomeScreen(Stage stage) {
        this.stage = stage;
        show();
    }
    
    private void show() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #000000;");
        
        // Create flowing pixel particles in background
        List<Circle> particles = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            Circle particle = createFlowingParticle();
            particles.add(particle);
            root.getChildren().add(particle);
        }
        
        // Main content
        VBox content = new VBox(50);
        content.setAlignment(Pos.CENTER);
        
        // Title - softer white/gray, not cyan
        Text title = createPixelText("LIQUID WAR");
        title.setFill(Color.rgb(220, 220, 230)); // Soft white
        title.setFont(Font.font("Courier New", FontWeight.BOLD, 64));
        
        // Subtle glow
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(100, 200, 220));
        glow.setRadius(15);
        glow.setSpread(0.3);
        title.setEffect(glow);
        
        // Sliding animation from right to left
        title.setTranslateX(WIDTH);
        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(1.2), title);
        slideIn.setToX(0);
        slideIn.setInterpolator(Interpolator.EASE_OUT);
        
        // Subtitle
        Text subtitle = new Text("A MULTIPLAYER FLUID WARFARE");
        subtitle.setFont(Font.font("Courier New", FontWeight.NORMAL, 16));
        subtitle.setFill(Color.rgb(150, 150, 160));
        subtitle.setOpacity(0);
        
        // Modern buttons
        Button startButton = createModernButton("START GAME");
        Button quitButton = createModernButton("QUIT");
        
        startButton.setOnAction(e -> startGame());
        quitButton.setOnAction(e -> stage.close());
        
        VBox buttons = new VBox(20);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(startButton, quitButton);
        buttons.setOpacity(0);
        
        content.getChildren().addAll(title, subtitle, buttons);
        
        // Animation sequence
        slideIn.setOnFinished(e -> {
            FadeTransition fadeSubtitle = new FadeTransition(Duration.seconds(0.8), subtitle);
            fadeSubtitle.setFromValue(0);
            fadeSubtitle.setToValue(1);
            
            FadeTransition fadeButtons = new FadeTransition(Duration.seconds(1), buttons);
            fadeButtons.setFromValue(0);
            fadeButtons.setToValue(1);
            
            fadeSubtitle.play();
            fadeButtons.play();
        });
        slideIn.play();
        
        root.getChildren().add(content);
        
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    private Circle createFlowingParticle() {
        double size = 2 + random.nextDouble() * 4;
        Circle particle = new Circle(size);
        
        // Mix of cyan and blue particles
        if (random.nextBoolean()) {
            particle.setFill(Color.rgb(0, 200, 220, 0.6)); // Cyan
        } else {
            particle.setFill(Color.rgb(50, 100, 200, 0.6)); // Blue
        }
        
        // Random starting position
        double startX = random.nextDouble() * WIDTH;
        double startY = random.nextDouble() * HEIGHT;
        particle.setTranslateX(startX);
        particle.setTranslateY(startY);
        
        // Flowing animation
        double duration = 5 + random.nextDouble() * 5;
        double endX = startX + (random.nextDouble() - 0.5) * 300;
        double endY = startY + (random.nextDouble() - 0.5) * 300;
        
        TranslateTransition flow = new TranslateTransition(Duration.seconds(duration), particle);
        flow.setToX(endX);
        flow.setToY(endY);
        flow.setCycleCount(TranslateTransition.INDEFINITE);
        flow.setAutoReverse(true);
        flow.setInterpolator(Interpolator.EASE_BOTH);
        flow.play();
        
        // Fade in/out
        FadeTransition fade = new FadeTransition(Duration.seconds(3 + random.nextDouble() * 3), particle);
        fade.setFromValue(0.3);
        fade.setToValue(0.8);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
        
        return particle;
    }
    
    private Text createPixelText(String text) {
        Text pixelText = new Text(text);
        pixelText.setStyle("-fx-font-family: 'Courier New'; -fx-font-weight: bold;");
        return pixelText;
    }
    
    private Button createModernButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Courier New", FontWeight.BOLD, 16));
        button.setPrefWidth(280);
        button.setPrefHeight(50);
        
        // Black/gray with cyan accent
        button.setStyle(
            "-fx-background-color: #1a1a1a;" +
            "-fx-text-fill: #a0a0a0;" +
            "-fx-border-color: #00b8cc;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Courier New';"
        );
        
        // Subtle hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: #2a2a2a;" +
                "-fx-text-fill: #00d4ff;" +
                "-fx-border-color: #00d4ff;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;" +
                "-fx-font-family: 'Courier New';"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: #1a1a1a;" +
                "-fx-text-fill: #a0a0a0;" +
                "-fx-border-color: #00b8cc;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 5;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;" +
                "-fx-font-family: 'Courier New';"
            );
        });
        
        return button;
    }
    
    private void startGame() {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), stage.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> new MainWindow(stage));
        fadeOut.play();
    }
}