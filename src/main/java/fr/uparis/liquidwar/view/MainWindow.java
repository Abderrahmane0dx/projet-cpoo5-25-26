package fr.uparis.liquidwar.view;

import fr.uparis.liquidwar.algorithm.GradientCalculator;
import fr.uparis.liquidwar.algorithm.ParticleMovement;
import fr.uparis.liquidwar.model.Board;
import fr.uparis.liquidwar.model.Gradient;
import fr.uparis.liquidwar.model.Particle;
import fr.uparis.liquidwar.model.Position;
import fr.uparis.liquidwar.model.Team;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modern game window - Cyan vs Blue teams with organic obstacles.
 */
public class MainWindow {
    private static final int BOARD_WIDTH = 200;
    private static final int BOARD_HEIGHT = 150;
    private static final int PARTICLE_COUNT_PER_TEAM = 800;
    
    private Stage stage;
    private GamePanel gamePanel;
    private Board board;
    private List<Team> teams;
    private Map<Team, Gradient> gradients;
    
    private GradientCalculator gradientCalculator;
    private ParticleMovement particleMovement;
    
    private AnimationTimer gameLoop;
    private boolean running = false;
    
    private Rectangle team1Bar;
    private Rectangle team2Bar;
    private Label team1CountLabel;
    private Label team2CountLabel;
    private Label fpsLabel;
    private Button startPauseButton;
    
    private long lastTime = 0;
    private int frameCount = 0;
    
    public MainWindow(Stage stage) {
        this.stage = stage;
        initializeGame();
        setupUI();
    }
    
    private void initializeGame() {
        board = new Board(BOARD_WIDTH, BOARD_HEIGHT);
        board.createBorderWalls();
        
        // Create ORGANIC shaped obstacles!
        createOrganicObstacles();
        
        // CYAN vs BLUE teams
        Team team1 = new Team(1, Color.rgb(0, 220, 230), new Position(50, 75)); // Cyan
        Team team2 = new Team(2, Color.rgb(50, 120, 220), new Position(150, 75)); // Blue
        teams = List.of(team1, team2);
        
        createCircularSpawn(team1, 40, 75, 25);
        createCircularSpawn(team2, 160, 75, 25);
        
        gradientCalculator = new GradientCalculator(board);
        particleMovement = new ParticleMovement(board);
        gradients = new HashMap<>();
        
        updateGradients();
    }
    
    private void createOrganicObstacles() {
        // Large organic blob in center
        createOrganicBlob(100, 75, 15, 12);
        
        // Smaller organic blobs scattered
        createOrganicBlob(60, 40, 10, 8);
        createOrganicBlob(140, 40, 10, 8);
        createOrganicBlob(60, 110, 10, 8);
        createOrganicBlob(140, 110, 10, 8);
        
        // Wavy walls
        createWavyWall(30, 50, 40, true);
        createWavyWall(130, 50, 40, true);
        createWavyWall(60, 20, 80, false);
        createWavyWall(60, 130, 80, false);
        
        // Organic clusters
        createOrganicCluster(80, 30, 8, 5);
        createOrganicCluster(120, 120, 8, 5);
        
        // Random organic scattered
        for (int i = 0; i < 30; i++) {
            int cx = 20 + (int)(Math.random() * 160);
            int cy = 20 + (int)(Math.random() * 110);
            if (board.isFree(cx, cy)) {
                createTinyBlob(cx, cy, 2 + (int)(Math.random() * 3));
            }
        }
    }
    
    private void createOrganicBlob(int cx, int cy, int radiusX, int radiusY) {
        for (int y = cy - radiusY; y <= cy + radiusY; y++) {
            for (int x = cx - radiusX; x <= cx + radiusX; x++) {
                double normalizedX = (double)(x - cx) / radiusX;
                double normalizedY = (double)(y - cy) / radiusY;
                double dist = normalizedX * normalizedX + normalizedY * normalizedY;
                
                // Add organic randomness
                if (dist <= 1.0 + Math.random() * 0.3) {
                    if (board.isInBounds(x, y)) {
                        board.setObstacle(x, y);
                    }
                }
            }
        }
    }
    
    private void createWavyWall(int startX, int startY, int length, boolean vertical) {
        for (int i = 0; i < length; i++) {
            double wave = Math.sin(i * 0.5) * 3;
            int x, y;
            
            if (vertical) {
                x = startX + (int)wave;
                y = startY + i;
            } else {
                x = startX + i;
                y = startY + (int)wave;
            }
            
            if (board.isInBounds(x, y)) {
                board.setObstacle(x, y);
                // Thicken the wall slightly
                if (board.isInBounds(x + 1, y)) board.setObstacle(x + 1, y);
            }
        }
    }
    
    private void createOrganicCluster(int cx, int cy, int count, int spread) {
        for (int i = 0; i < count; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = Math.random() * spread;
            int x = cx + (int)(Math.cos(angle) * distance);
            int y = cy + (int)(Math.sin(angle) * distance);
            
            createTinyBlob(x, y, 2 + (int)(Math.random() * 2));
        }
    }
    
    private void createTinyBlob(int cx, int cy, int size) {
        for (int dy = -size; dy <= size; dy++) {
            for (int dx = -size; dx <= size; dx++) {
                if (dx * dx + dy * dy <= size * size) {
                    int x = cx + dx;
                    int y = cy + dy;
                    if (board.isInBounds(x, y)) {
                        board.setObstacle(x, y);
                    }
                }
            }
        }
    }
    
    private void createCircularSpawn(Team team, int cx, int cy, int radius) {
        int count = 0;
        int attempts = 0;
        
        while (count < PARTICLE_COUNT_PER_TEAM && attempts < PARTICLE_COUNT_PER_TEAM * 10) {
            attempts++;
            double angle = Math.random() * 2 * Math.PI;
            double r = Math.random() * radius;
            int x = cx + (int)(r * Math.cos(angle));
            int y = cy + (int)(r * Math.sin(angle));
            
            if (board.isFree(x, y)) {
                Particle p = new Particle(new Position(x, y), team);
                board.setParticle(p, x, y);
                team.addParticle(p);
                count++;
            }
        }
    }
    
    private void setupUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #000000;");
        
        gamePanel = new GamePanel(board);
        gamePanel.setGradients(gradients);
        
        gamePanel.setOnMouseMoved(event -> {
            Position pos = gamePanel.screenToBoard(event.getX(), event.getY());
            if (board.isInBounds(pos)) {
                teams.get(0).setTargetPosition(pos);
            }
        });
        
        StackPane centerPanel = new StackPane(gamePanel);
        centerPanel.setStyle("-fx-background-color: #000000; -fx-padding: 10;");
        root.setCenter(centerPanel);
        
        VBox rightPanel = createRightPanel();
        root.setRight(rightPanel);
        
        HBox bottomBar = createBottomBar();
        root.setBottom(bottomBar);
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        
        setupGameLoop();
    }
    
    private VBox createRightPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(180);
        panel.setStyle("-fx-background-color: #000000;");
        
        // Vertical score bars
        HBox barsContainer = new HBox(30);
        barsContainer.setAlignment(Pos.CENTER);
        barsContainer.setPrefHeight(400);
        
        VBox team1Container = createScoreBar(teams.get(0), "CYAN");
        VBox team2Container = createScoreBar(teams.get(1), "BLUE");
        
        barsContainer.getChildren().addAll(team1Container, team2Container);
        
        // Modern control buttons
        VBox controls = new VBox(15);
        controls.setAlignment(Pos.CENTER);
        
        startPauseButton = createModernButton("▶ START");
        startPauseButton.setOnAction(e -> toggleStartPause());
        
        Button resetButton = createModernButton("↻ RESET");
        resetButton.setOnAction(e -> reset());
        
        Button quitButton = createModernButton("✖ QUIT");
        quitButton.setOnAction(e -> stage.close());
        
        controls.getChildren().addAll(startPauseButton, resetButton, quitButton);
        
        panel.getChildren().addAll(barsContainer, controls);
        return panel;
    }
    
    private VBox createScoreBar(Team team, String name) {
        VBox container = new VBox(10);
        container.setAlignment(Pos.CENTER);
        
        // Team label
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        nameLabel.setTextFill(team.getBaseColor());
        
        // Vertical bar
        StackPane barContainer = new StackPane();
        barContainer.setPrefSize(35, 350);
        
        Rectangle bgBar = new Rectangle(35, 350);
        bgBar.setFill(Color.rgb(20, 20, 25));
        bgBar.setStroke(Color.rgb(40, 40, 50));
        bgBar.setStrokeWidth(1);
        bgBar.setArcWidth(8);
        bgBar.setArcHeight(8);
        
        Rectangle scoreBar = new Rectangle(35, 350);
        scoreBar.setFill(team.getBaseColor());
        scoreBar.setArcWidth(8);
        scoreBar.setArcHeight(8);
        
        DropShadow glow = new DropShadow();
        glow.setColor(team.getBaseColor());
        glow.setRadius(8);
        scoreBar.setEffect(glow);
        
        barContainer.getChildren().addAll(bgBar, scoreBar);
        StackPane.setAlignment(scoreBar, Pos.BOTTOM_CENTER);
        
        Label countLabel = new Label(String.valueOf(team.getParticleCount()));
        countLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 14));
        countLabel.setTextFill(Color.rgb(180, 180, 190));
        
        if (name.equals("CYAN")) {
            team1Bar = scoreBar;
            team1CountLabel = countLabel;
        } else {
            team2Bar = scoreBar;
            team2CountLabel = countLabel;
        }
        
        container.getChildren().addAll(nameLabel, barContainer, countLabel);
        return container;
    }
    
    private Button createModernButton(String text) {
        Button button = new Button(text);
        button.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        button.setPrefWidth(150);
        button.setPrefHeight(40);
        
        // Modern black/gray/cyan theme
        button.setStyle(
            "-fx-background-color: #1a1a1a;" +
            "-fx-text-fill: #909090;" +
            "-fx-border-color: #00b8cc;" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Courier New';"
        );
        
        button.setOnMouseEntered(e -> {
            button.setStyle(
                "-fx-background-color: #252525;" +
                "-fx-text-fill: #00d4ff;" +
                "-fx-border-color: #00d4ff;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-font-family: 'Courier New';"
            );
        });
        
        button.setOnMouseExited(e -> {
            button.setStyle(
                "-fx-background-color: #1a1a1a;" +
                "-fx-text-fill: #909090;" +
                "-fx-border-color: #00b8cc;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-font-family: 'Courier New';"
            );
        });
        
        return button;
    }
    
    private HBox createBottomBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(10));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #000000; -fx-border-color: #00b8cc; -fx-border-width: 1 0 0 0;");
        
        fpsLabel = new Label("FPS: 0");
        fpsLabel.setFont(Font.font("Courier New", FontWeight.BOLD, 12));
        fpsLabel.setTextFill(Color.rgb(0, 200, 220));
        
        Label controlsLabel = new Label("🖱 Move mouse to control CYAN team");
        controlsLabel.setFont(Font.font("Courier New", FontWeight.NORMAL, 11));
        controlsLabel.setTextFill(Color.rgb(120, 120, 130));
        
        bar.getChildren().addAll(fpsLabel, controlsLabel);
        return bar;
    }
    
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (running) {
                    update();
                    render();
                    updateFPS(now);
                    updateUI();
                }
            }
        };
    }
    
    private void update() {
        updateGradients();
        
        for (Particle particle : board.getAllParticles()) {
            Gradient gradient = gradients.get(particle.getTeam());
            if (gradient != null) {
                particleMovement.moveParticle(particle, gradient);
            }
        }
        
        checkWinCondition();
    }
    
    private void updateGradients() {
        for (Team team : teams) {
            gradients.put(team, gradientCalculator.calculate(team));
        }
    }
    
    private void render() {
        gamePanel.render();
    }
    
    private void updateFPS(long now) {
        if (lastTime == 0) {
            lastTime = now;
            return;
        }
        
        frameCount++;
        long elapsed = now - lastTime;
        
        if (elapsed >= 1_000_000_000) {
            int fps = (int) ((frameCount * 1_000_000_000.0) / elapsed);
            Platform.runLater(() -> fpsLabel.setText("FPS: " + fps));
            frameCount = 0;
            lastTime = now;
        }
    }
    
    private void updateUI() {
        Platform.runLater(() -> {
            int total = PARTICLE_COUNT_PER_TEAM * 2;
            int team1Count = teams.get(0).getParticleCount();
            int team2Count = teams.get(1).getParticleCount();
            
            team1CountLabel.setText(String.valueOf(team1Count));
            team2CountLabel.setText(String.valueOf(team2Count));
            
            double barHeight = 350.0;
            team1Bar.setHeight((team1Count / (double) total) * barHeight);
            team2Bar.setHeight((team2Count / (double) total) * barHeight);
        });
    }
    
    private void checkWinCondition() {
        if (teams.get(0).getParticleCount() == 0) {
            Platform.runLater(() -> {
                pause();
                startPauseButton.setText("🏆 BLUE WINS");
            });
        } else if (teams.get(1).getParticleCount() == 0) {
            Platform.runLater(() -> {
                pause();
                startPauseButton.setText("🏆 CYAN WINS");
            });
        }
    }
    
    private void toggleStartPause() {
        if (running) {
            pause();
        } else {
            start();
        }
    }
    
    private void start() {
        running = true;
        gameLoop.start();
        startPauseButton.setText("⏸ PAUSE");
    }
    
    private void pause() {
        running = false;
        startPauseButton.setText("▶ START");
    }
    
    private void reset() {
        pause();
        initializeGame();
        gamePanel.setBoard(board);
        gamePanel.setGradients(gradients);
        startPauseButton.setText("▶ START");
        updateUI();
    }
}