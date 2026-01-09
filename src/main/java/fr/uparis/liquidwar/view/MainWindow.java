package fr.uparis.liquidwar.view;

import fr.uparis.liquidwar.algorithm.AIController;
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
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main game window with pause menu overlay.
 */
public class MainWindow {
    private static final int BOARD_WIDTH = 200;
    private static final int BOARD_HEIGHT = 150;
    private static final int PARTICLE_COUNT_PER_TEAM = 500;
    private static final int CURSOR_SPEED = 3;
    
    private Stage stage;
    private GamePanel gamePanel;
    private Board board;
    private List<Team> teams;
    private Map<Team, Gradient> gradients;
    
    private GradientCalculator gradientCalculator;
    private ParticleMovement particleMovement;
    private AIController aiController;
    
    private AnimationTimer gameLoop;
    private boolean running = false;
    private boolean paused = false;
    
    // Game mode
    private boolean isMultiplayer;
    private AIController.Difficulty aiDifficulty;
    
    // Pause menu
    private StackPane rootPane;
    private VBox pauseMenu;
    private Rectangle overlay;
    
    // Keyboard state for player 2
    private Set<KeyCode> pressedKeys = new HashSet<>();
    
    // Score labels
    private Label scoreLabel;
    
    /**
     * Creates the main game window for VS AI mode.
     * 
     * @param stage JavaFX stage
     * @param difficulty AI difficulty level
     */
    public MainWindow(Stage stage, AIController.Difficulty difficulty) {
        this.stage = stage;
        this.aiDifficulty = difficulty;
        this.isMultiplayer = false;
        initializeGame();
        setupUI();
        start(); // Auto-start
    }
    
    /**
     * Creates the main game window for multiplayer mode.
     * 
     * @param stage JavaFX stage
     * @param multiplayer true for local multiplayer
     */
    public MainWindow(Stage stage, boolean multiplayer) {
        this.stage = stage;
        this.isMultiplayer = multiplayer;
        this.aiDifficulty = null;
        initializeGame();
        setupUI();
        start(); // Auto-start
    }
    
    /**
     * Initializes the game state.
     */
    private void initializeGame() {
        // Create board
        board = new Board(BOARD_WIDTH, BOARD_HEIGHT);
        board.createBorderWalls();
        board.createRandomObstacles(0.05); // 5% obstacles
        
        // Create teams
        Team team1 = new Team(1, Color.RED, new Position(50, 75));
        Team team2 = new Team(2, Color.BLUE, new Position(150, 75));
        teams = List.of(team1, team2);
        
        // Create particles
        createParticles(team1, 30, 50, 60, 100);
        createParticles(team2, 140, 160, 60, 100);
        
        // Initialize algorithms
        gradientCalculator = new GradientCalculator(board);
        particleMovement = new ParticleMovement(board);
        gradients = new HashMap<>();
        
        // Initialize AI for team 2 (only in VS AI mode)
        if (!isMultiplayer && aiDifficulty != null) {
            aiController = new AIController(board, team2, team1, aiDifficulty);
        } else {
            aiController = null;
        }
        
        // Calculate initial gradients
        updateGradients();
    }
    
    /**
     * Creates particles for a team in a rectangular area.
     */
    private void createParticles(Team team, int x1, int x2, int y1, int y2) {
        int count = 0;
        while (count < PARTICLE_COUNT_PER_TEAM) {
            int x = x1 + (int)(Math.random() * (x2 - x1));
            int y = y1 + (int)(Math.random() * (y2 - y1));
            
            if (board.isFree(x, y)) {
                Particle p = new Particle(new Position(x, y), team);
                board.setParticle(p, x, y);
                team.addParticle(p);
                count++;
            }
        }
    }
    
    /**
     * Sets up the user interface.
     */
    private void setupUI() {
        rootPane = new StackPane();
        
        // Game content
        StackPane gameContent = new StackPane();
        
        // Create game panel
        gamePanel = new GamePanel(board);
        gamePanel.setGradients(gradients);
        
        // Score overlay at the top
        scoreLabel = new Label();
        updateScoreLabel();
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5 15; -fx-background-radius: 5;");
        StackPane.setAlignment(scoreLabel, Pos.TOP_CENTER);
        StackPane.setMargin(scoreLabel, new Insets(10, 0, 0, 0));
        
        gameContent.getChildren().addAll(gamePanel, scoreLabel);
        
        // Create pause menu overlay (hidden initially)
        createPauseMenu();
        
        rootPane.getChildren().addAll(gameContent, overlay, pauseMenu);
        overlay.setVisible(false);
        pauseMenu.setVisible(false);
        
        // Create scene
        Scene scene = new Scene(rootPane);
        
        // Mouse handler for player 1 (red team)
        gamePanel.setOnMouseMoved(event -> {
            if (running && !paused && teams.size() > 0) {
                Position pos = gamePanel.screenToBoard(event.getX(), event.getY());
                if (board.isInBounds(pos)) {
                    teams.get(0).setTargetPosition(pos);
                }
            }
        });
        
        // Keyboard handlers
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE && running) {
                togglePause();
            } else {
                pressedKeys.add(event.getCode());
            }
        });
        
        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
        });
        
        stage.setScene(scene);
        stage.setTitle("Liquid War - " + (isMultiplayer ? "Multijoueur Local" : "VS IA"));
        stage.setResizable(false);
        stage.show();
        
        // Setup game loop
        setupGameLoop();
    }
    
    /**
     * Creates the pause menu overlay.
     */
    private void createPauseMenu() {
        // Semi-transparent overlay
        overlay = new Rectangle(BOARD_WIDTH * 4, BOARD_HEIGHT * 4);
        overlay.setFill(Color.rgb(0, 0, 0, 0.7));
        
        // Pause menu container
        pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setPadding(new Insets(30));
        pauseMenu.setMaxWidth(300);
        pauseMenu.setMaxHeight(350);
        pauseMenu.setStyle("-fx-background-color: rgba(30, 30, 50, 0.95); -fx-background-radius: 15;");
        
        // Title
        Label pauseTitle = new Label("⏸ PAUSE");
        pauseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        pauseTitle.setTextFill(Color.WHITE);
        
        // Resume button
        Button resumeButton = createPauseButton("▶ Reprendre", "#27ae60");
        resumeButton.setOnAction(e -> togglePause());
        
        // Restart button
        Button restartButton = createPauseButton("🔄 Recommencer", "#3498db");
        restartButton.setOnAction(e -> {
            togglePause();
            reset();
        });
        
        // Menu button
        Button menuButton = createPauseButton("🏠 Menu Principal", "#e74c3c");
        menuButton.setOnAction(e -> returnToMenu());
        
        // Controls info
        Label controlsInfo = new Label(isMultiplayer ? 
            "🔴 Rouge: Souris\n🔵 Bleu: Flèches directionnelles" :
            "🔴 Rouge: Souris\n🔵 Bleu: IA");
        controlsInfo.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        controlsInfo.setTextFill(Color.LIGHTGRAY);
        controlsInfo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        controlsInfo.setAlignment(Pos.CENTER);
        
        pauseMenu.getChildren().addAll(pauseTitle, resumeButton, restartButton, menuButton, controlsInfo);
    }
    
    /**
     * Creates a styled button for the pause menu.
     */
    private Button createPauseButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setPrefWidth(220);
        button.setPrefHeight(45);
        button.setAlignment(Pos.CENTER);
        button.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-alignment: center;"
        );
        
        button.setOnMouseEntered(e -> {
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
     * Toggles the pause state.
     */
    private void togglePause() {
        paused = !paused;
        overlay.setVisible(paused);
        pauseMenu.setVisible(paused);
        
        if (paused) {
            // Apply blur to game panel
            gamePanel.setEffect(new GaussianBlur(5));
        } else {
            gamePanel.setEffect(null);
        }
    }
    
    /**
     * Updates the score label.
     */
    private void updateScoreLabel() {
        int redCount = teams.get(0).getParticleCount();
        int blueCount = teams.get(1).getParticleCount();
        String modeText = isMultiplayer ? "LOCAL" : "VS IA";
        scoreLabel.setText("🔴 " + redCount + "  |  " + modeText + "  |  " + blueCount + " 🔵");
    }
    
    /**
     * Sets up the game loop.
     */
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (running && !paused) {
                    update();
                    render();
                }
            }
        };
    }
    
    /**
     * Updates the game state.
     */
    private void update() {
        // Update player 2 based on mode
        if (isMultiplayer) {
            updatePlayer2Keyboard();
        } else if (aiController != null) {
            aiController.update();
            aiController.adaptStrategy();
        }
        
        // Update gradients
        updateGradients();
        
        // Move all particles
        for (Particle particle : board.getAllParticles()) {
            Gradient gradient = gradients.get(particle.getTeam());
            if (gradient != null) {
                particleMovement.moveParticle(particle, gradient);
            }
        }
        
        // Update score
        Platform.runLater(this::updateScoreLabel);
        
        // Check win condition
        checkWinCondition();
    }
    
    /**
     * Updates player 2 cursor based on keyboard input.
     */
    private void updatePlayer2Keyboard() {
        if (teams.size() < 2) return;
        
        Team team2 = teams.get(1);
        Position current = team2.getTargetPosition();
        int newX = current.x();
        int newY = current.y();
        
        if (pressedKeys.contains(KeyCode.UP)) {
            newY -= CURSOR_SPEED;
        }
        if (pressedKeys.contains(KeyCode.DOWN)) {
            newY += CURSOR_SPEED;
        }
        if (pressedKeys.contains(KeyCode.LEFT)) {
            newX -= CURSOR_SPEED;
        }
        if (pressedKeys.contains(KeyCode.RIGHT)) {
            newX += CURSOR_SPEED;
        }
        
        // Clamp to board bounds
        newX = Math.max(1, Math.min(board.getWidth() - 2, newX));
        newY = Math.max(1, Math.min(board.getHeight() - 2, newY));
        
        Position newPos = new Position(newX, newY);
        if (board.isInBounds(newPos)) {
            team2.setTargetPosition(newPos);
        }
    }
    
    /**
     * Updates gradients for all teams.
     */
    private void updateGradients() {
        for (Team team : teams) {
            Gradient gradient = gradientCalculator.calculate(team);
            gradients.put(team, gradient);
        }
    }
    
    /**
     * Renders the game.
     */
    private void render() {
        gamePanel.render();
    }
    
    /**
     * Checks if a team has won.
     */
    private void checkWinCondition() {
        int team1Count = teams.get(0).getParticleCount();
        int team2Count = teams.get(1).getParticleCount();
        
        if (team1Count == 0 || team2Count == 0) {
            running = false;
            String winner = team1Count == 0 ? "🔵 BLEU GAGNE!" : "🔴 ROUGE GAGNE!";
            Platform.runLater(() -> showGameOver(winner));
        }
    }
    
    /**
     * Shows game over screen.
     */
    private void showGameOver(String winner) {
        overlay.setVisible(true);
        pauseMenu.setVisible(true);
        gamePanel.setEffect(new GaussianBlur(5));
        
        // Update pause menu for game over
        pauseMenu.getChildren().clear();
        
        Label gameOverTitle = new Label("🏆 FIN DE PARTIE");
        gameOverTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gameOverTitle.setTextFill(Color.GOLD);
        gameOverTitle.setAlignment(Pos.CENTER);
        
        Label winnerLabel = new Label(winner);
        winnerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        winnerLabel.setTextFill(Color.WHITE);
        winnerLabel.setAlignment(Pos.CENTER);
        
        Button restartButton = createPauseButton("🔄 Rejouer", "#27ae60");
        restartButton.setOnAction(e -> {
            // Hide overlay and menu
            overlay.setVisible(false);
            pauseMenu.setVisible(false);
            gamePanel.setEffect(null);
            
            // Recreate pause menu with correct content
            pauseMenu.getChildren().clear();
            rebuildPauseMenuContent();
            
            // Reset and start game
            reset();
        });
        
        Button menuButton = createPauseButton("🏠 Menu Principal", "#3498db");
        menuButton.setOnAction(e -> returnToMenu());
        
        pauseMenu.getChildren().addAll(gameOverTitle, winnerLabel, restartButton, menuButton);
    }
    
    /**
     * Rebuilds the pause menu content (without recreating overlay).
     */
    private void rebuildPauseMenuContent() {
        // Title
        Label pauseTitle = new Label("⏸ PAUSE");
        pauseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        pauseTitle.setTextFill(Color.WHITE);
        pauseTitle.setAlignment(Pos.CENTER);
        
        // Resume button
        Button resumeButton = createPauseButton("▶ Reprendre", "#27ae60");
        resumeButton.setOnAction(e -> togglePause());
        
        // Restart button
        Button restartBtn = createPauseButton("🔄 Recommencer", "#3498db");
        restartBtn.setOnAction(e -> {
            togglePause();
            reset();
        });
        
        // Menu button
        Button menuBtn = createPauseButton("🏠 Menu Principal", "#e74c3c");
        menuBtn.setOnAction(e -> returnToMenu());
        
        // Controls info
        Label controlsInfo = new Label(isMultiplayer ? 
            "🔴 Rouge: Souris\n🔵 Bleu: Flèches directionnelles" :
            "🔴 Rouge: Souris\n🔵 Bleu: IA");
        controlsInfo.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        controlsInfo.setTextFill(Color.LIGHTGRAY);
        controlsInfo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        controlsInfo.setAlignment(Pos.CENTER);
        
        pauseMenu.getChildren().addAll(pauseTitle, resumeButton, restartBtn, menuBtn, controlsInfo);
    }
    
    /**
     * Starts the game.
     */
    public void start() {
        running = true;
        paused = false;
        gameLoop.start();
    }
    
    /**
     * Resets the game.
     */
    public void reset() {
        running = false;
        paused = false;
        initializeGame();
        gamePanel.setBoard(board);
        gamePanel.setGradients(gradients);
        updateScoreLabel();
        start();
    }
    
    /**
     * Returns to the main menu.
     */
    public void returnToMenu() {
        running = false;
        gameLoop.stop();
        new MenuWindow(stage);
    }
}
